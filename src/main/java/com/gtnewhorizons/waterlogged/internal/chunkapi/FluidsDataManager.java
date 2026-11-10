package com.gtnewhorizons.waterlogged.internal.chunkapi;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.falsepattern.chunk.api.DataManager;
import com.gtnewhorizons.waterlogged.Tags;
import com.gtnewhorizons.waterlogged.Waterlogged;
import com.gtnewhorizons.waterlogged.internal.light.LightHooks;
import com.gtnewhorizons.waterlogged.internal.storage.ChunkFluids;
import com.gtnewhorizons.waterlogged.internal.storage.SectionFluids;
import com.gtnewhorizons.waterlogged.internal.storage.SectionFluidsNbt;
import com.gtnewhorizons.waterlogged.internal.storage.SectionFluidsPacket;
import com.gtnewhorizons.waterlogged.internal.storage.WaterloggedBlockPacket;
import com.gtnewhorizons.waterlogged.internal.storage.WaterloggedSection;

/** ChunkAPI manager that saves section fluids and sends them to clients; register it once in the init phase. */
public final class FluidsDataManager
    implements DataManager.SubChunkDataManager, DataManager.PacketDataManager, DataManager.BlockPacketDataManager {

    public static final FluidsDataManager INSTANCE = new FluidsDataManager();
    public static final int ORDERING = 1000;
    private static final int SECTIONS = 16;

    private final Set<String> warned = Collections.synchronizedSet(new HashSet<>());

    private FluidsDataManager() {}

    @Override
    public String domain() {
        return Waterlogged.MODID;
    }

    @Override
    public String id() {
        return "fluids";
    }

    @Override
    public String version() {
        return Tags.VERSION;
    }

    @Override
    public @Nullable String newInstallDescription() {
        return null;
    }

    @Override
    public String uninstallMessage() {
        return "Blocks will lose the fluid stored in them.";
    }

    @Override
    public @Nullable String versionChangeMessage(String priorVersion) {
        return null;
    }

    @Override
    public void writeSubChunkToNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        WaterloggedSection section = (WaterloggedSection) subChunk;
        NBTTagCompound preserved = section.waterlogged$getPreserved();
        if (preserved != null) {
            for (String key : preserved.func_150296_c()) {
                nbt.setTag(
                    key,
                    preserved.getTag(key)
                        .copy());
            }
            return;
        }
        SectionFluidsNbt.write(section.waterlogged$get(), nbt);
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        WaterloggedSection section = (WaterloggedSection) subChunk;
        SectionFluidsNbt.Result result = SectionFluidsNbt.read(nbt, this::lookupFluid);
        section.waterlogged$set(result.fluids);
        section.waterlogged$setPreserved(result.preservedRaw);
        if (result.preservedRaw != null && warned.add("\0newer-version")) {
            Waterlogged.LOG
                .error("Section fluids were saved by a newer Waterlogged version; kept on disk but not loaded");
        }
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        WaterloggedSection src = (WaterloggedSection) from;
        WaterloggedSection dst = (WaterloggedSection) to;
        SectionFluids fluids = src.waterlogged$get();
        dst.waterlogged$set(fluids == null ? null : fluids.copy());
        NBTTagCompound preserved = src.waterlogged$getPreserved();
        dst.waterlogged$setPreserved(preserved == null ? null : (NBTTagCompound) preserved.copy());
    }

    @Override
    public int maxPacketSize() {
        return SECTIONS * SectionFluidsPacket.MAX_SECTION_BYTES;
    }

    @Override
    public void writeToBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        for (int i = 0; i < SECTIONS; i++) {
            if ((subChunkMask & (1 << i)) == 0) {
                continue;
            }
            // One entry per masked section even when it is null, so both sides stay aligned
            SectionFluids fluids = sections[i] == null ? null : ((WaterloggedSection) sections[i]).waterlogged$get();
            SectionFluidsPacket.write(fluids, buffer, Fluid::getID);
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        for (int i = 0; i < SECTIONS; i++) {
            if ((subChunkMask & (1 << i)) == 0) {
                continue;
            }
            SectionFluids fluids = SectionFluidsPacket.read(buffer, FluidRegistry::getFluid);
            if (sections[i] != null) {
                ((WaterloggedSection) sections[i]).waterlogged$set(fluids);
            }
        }
    }

    @Override
    public void writeBlockToPacket(Chunk chunk, int x, int y, int z, S23PacketBlockChange packet) {
        Fluid fluid = ChunkFluids.get(chunk, x, y, z);
        ((WaterloggedBlockPacket) packet).waterlogged$setFluidId(fluid == null ? -1 : fluid.getID());
    }

    @Override
    public void readBlockFromPacket(Chunk chunk, int x, int y, int z, S23PacketBlockChange packet) {
        int id = ((WaterloggedBlockPacket) packet).waterlogged$getFluidId();
        Fluid fluid = id < 0 ? null : FluidRegistry.getFluid(id);
        Fluid old = ChunkFluids.get(chunk, x, y, z);
        if (old != fluid) {
            ChunkFluids.set(chunk, x, y, z, fluid);
            LightHooks.onFluidChanged(chunk.worldObj, chunk, x, y, z, old, fluid);
            int wx = chunk.xPosition << 4 | x;
            int wz = chunk.zPosition << 4 | z;
            chunk.worldObj.markBlockRangeForRenderUpdate(wx, y, wz, wx, y, wz);
        }
    }

    @Override
    public void writeBlockPacketToBuffer(S23PacketBlockChange packet, PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(((WaterloggedBlockPacket) packet).waterlogged$getFluidId() + 1);
    }

    @Override
    public void readBlockPacketFromBuffer(S23PacketBlockChange packet, PacketBuffer buffer) {
        ((WaterloggedBlockPacket) packet).waterlogged$setFluidId(buffer.readVarIntFromBuffer() - 1);
    }

    private @Nullable Fluid lookupFluid(String name) {
        Fluid fluid = FluidRegistry.getFluid(name);
        if (fluid == null && warned.add(name)) {
            Waterlogged.LOG.warn("Fluid '{}' no longer exists; blocks that held it lose it", name);
        }
        return fluid;
    }
}
