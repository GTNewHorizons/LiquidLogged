package com.gtnewhorizons.waterlogged.internal.chunkapi;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;

import com.falsepattern.chunk.api.DataManager;
import com.gtnewhorizons.waterlogged.Tags;
import com.gtnewhorizons.waterlogged.Waterlogged;
import com.gtnewhorizons.waterlogged.internal.tick.FluidTickNbt;
import com.gtnewhorizons.waterlogged.internal.tick.WorldFluidTicks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** ChunkAPI manager that saves pending stored-fluid ticks per chunk and drops them from memory on the unload save. */
public final class TicksDataManager implements DataManager.ChunkDataManager {

    public static final TicksDataManager INSTANCE = new TicksDataManager();

    private TicksDataManager() {}

    @Override
    public String domain() {
        return Waterlogged.MODID;
    }

    @Override
    public String id() {
        return "ticks";
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
        return "Pending flow of fluids stored in blocks will be lost.";
    }

    @Override
    public @Nullable String versionChangeMessage(String priorVersion) {
        return null;
    }

    @Override
    public void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt) {
        if (!(chunk.worldObj instanceof WorldServer)) {
            return;
        }
        // ChunkProviderServer marks a chunk unloaded before saving it, so this is the last save: drop its ticks here
        boolean unloading = !chunk.isChunkLoaded;
        FluidTickNbt.write(
            ((WorldFluidTicks) chunk.worldObj).waterlogged$ticks()
                .extractChunk(chunk.xPosition, chunk.zPosition, unloading),
            chunk.worldObj.getTotalWorldTime(),
            nbt);
    }

    @Override
    public void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt) {
        if (nbt == null || !(chunk.worldObj instanceof WorldServer)) {
            return;
        }
        FluidTickNbt.read(
            nbt,
            chunk.xPosition,
            chunk.zPosition,
            chunk.worldObj.getTotalWorldTime(),
            ((WorldFluidTicks) chunk.worldObj).waterlogged$ticks());
    }

    @Override
    public void cloneChunk(Chunk from, Chunk to) {}

    // Forge reads chunk NBT on its IO thread before the chunk is in the world; schedule its ticks once it is
    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        Chunk chunk = event.getChunk();
        if (chunk.worldObj instanceof WorldServer) {
            ((WorldFluidTicks) chunk.worldObj).waterlogged$ticks()
                .commitChunk(chunk.xPosition, chunk.zPosition);
        }
    }
}
