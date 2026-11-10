package com.gtnewhorizons.liquidlogged.internal.chunkapi;

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
import com.gtnewhorizons.liquidlogged.LiquidLogged;
import com.gtnewhorizons.liquidlogged.Tags;
import com.gtnewhorizons.liquidlogged.internal.light.LightHooks;
import com.gtnewhorizons.liquidlogged.internal.storage.ChunkFluids;
import com.gtnewhorizons.liquidlogged.internal.storage.LiquidLoggedBlockPacket;
import com.gtnewhorizons.liquidlogged.internal.storage.LiquidLoggedSection;
import com.gtnewhorizons.liquidlogged.internal.storage.SectionFluids;
import com.gtnewhorizons.liquidlogged.internal.storage.SectionFluidsNbt;
import com.gtnewhorizons.liquidlogged.internal.storage.SectionFluidsPacket;

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
        return LiquidLogged.MODID;
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
        LiquidLoggedSection section = (LiquidLoggedSection) subChunk;
        NBTTagCompound preserved = section.liquidlogged$getPreserved();
        if (preserved != null) {
            for (String key : preserved.func_150296_c()) {
                nbt.setTag(
                    key,
                    preserved.getTag(key)
                        .copy());
            }
            return;
        }
        SectionFluidsNbt.write(section.liquidlogged$get(), nbt);
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        LiquidLoggedSection section = (LiquidLoggedSection) subChunk;
        SectionFluidsNbt.Result result = SectionFluidsNbt.read(nbt, this::lookupFluid);
        section.liquidlogged$set(result.fluids);
        section.liquidlogged$setPreserved(result.preservedRaw);
        if (result.preservedRaw != null && warned.add("\0newer-version")) {
            LiquidLogged.LOG
                .error("Section fluids were saved by a newer LiquidLogged version; kept on disk but not loaded");
        }
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        LiquidLoggedSection src = (LiquidLoggedSection) from;
        LiquidLoggedSection dst = (LiquidLoggedSection) to;
        SectionFluids fluids = src.liquidlogged$get();
        dst.liquidlogged$set(fluids == null ? null : fluids.copy());
        NBTTagCompound preserved = src.liquidlogged$getPreserved();
        dst.liquidlogged$setPreserved(preserved == null ? null : (NBTTagCompound) preserved.copy());
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
            // Write an entry for a null section too, or the reader falls out of step with the buffer
            SectionFluids fluids = sections[i] == null ? null : ((LiquidLoggedSection) sections[i]).liquidlogged$get();
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
                ((LiquidLoggedSection) sections[i]).liquidlogged$set(fluids);
            }
        }
    }

    @Override
    public void writeBlockToPacket(Chunk chunk, int x, int y, int z, S23PacketBlockChange packet) {
        Fluid fluid = ChunkFluids.get(chunk, x, y, z);
        ((LiquidLoggedBlockPacket) packet).liquidlogged$setFluidId(fluid == null ? -1 : fluid.getID());
    }

    @Override
    public void readBlockFromPacket(Chunk chunk, int x, int y, int z, S23PacketBlockChange packet) {
        int id = ((LiquidLoggedBlockPacket) packet).liquidlogged$getFluidId();
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
        buffer.writeVarIntToBuffer(((LiquidLoggedBlockPacket) packet).liquidlogged$getFluidId() + 1);
    }

    @Override
    public void readBlockPacketFromBuffer(S23PacketBlockChange packet, PacketBuffer buffer) {
        ((LiquidLoggedBlockPacket) packet).liquidlogged$setFluidId(buffer.readVarIntFromBuffer() - 1);
    }

    private @Nullable Fluid lookupFluid(String name) {
        Fluid fluid = FluidRegistry.getFluid(name);
        if (fluid == null && warned.add(name)) {
            LiquidLogged.LOG.warn("Fluid '{}' no longer exists; blocks that held it lose it", name);
        }
        return fluid;
    }
}
