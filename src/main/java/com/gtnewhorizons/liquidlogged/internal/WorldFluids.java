package com.gtnewhorizons.liquidlogged.internal;

import javax.annotation.Nullable;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedBlockAccess;
import com.gtnewhorizons.liquidlogged.internal.light.LightHooks;
import com.gtnewhorizons.liquidlogged.internal.storage.ChunkFluids;
import com.gtnewhorizons.liquidlogged.internal.tick.WorldFluidTicks;

/** Writes a block's fluid into the world without any checks; API, command and rules go through it. */
public final class WorldFluids {

    private WorldFluids() {}

    public static int level(IBlockAccess world, int x, int y, int z) {
        return world instanceof LiquidLoggedBlockAccess
            ? ((LiquidLoggedBlockAccess) world).liquidlogged$getFluidLevel(x, y, z)
            : 0;
    }

    /** Sets the fluid and sends the change to clients; false if the chunk is not loaded or the palette is full. */
    public static boolean setUnchecked(World world, int x, int y, int z, @Nullable Fluid fluid) {
        return setUnchecked(world, x, y, z, fluid, 0);
    }

    public static boolean setUnchecked(World world, int x, int y, int z, @Nullable Fluid fluid, int level) {
        if (y < 0 || y > 255 || !world.blockExists(x, y, z)) {
            return false;
        }
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        Fluid old = ChunkFluids.get(chunk, x & 15, y, z & 15);
        if (old == fluid && ChunkFluids.level(chunk, x & 15, y, z & 15) == level) {
            return true;
        }
        if (!ChunkFluids.set(chunk, x & 15, y, z & 15, fluid, level)) {
            return false;
        }
        if (old != fluid) {
            LightHooks.onFluidChanged(world, chunk, x & 15, y, z & 15, old, fluid);
        }
        world.markBlockForUpdate(x, y, z);
        // Settled water re-checks only on a neighbour change; else it keeps flowing from a drained block
        world.notifyBlocksOfNeighborChange(x, y, z, world.getBlock(x, y, z));
        if (fluid != null) {
            WorldFluidTicks.schedule(world, x, y, z, fluid);
        }
        return true;
    }
}
