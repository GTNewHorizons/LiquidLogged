package com.gtnewhorizons.waterlogged.internal.fill;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.waterlogged.api.WaterloggedAPI;
import com.gtnewhorizons.waterlogged.internal.storage.ChunkFluids;

/** Moves a block's stored fluid together with the block when a piston pushes or pulls it (design §7.5). */
public final class PistonHooks {

    private PistonHooks() {}

    /** Added to {@code TileEntityPiston} by mixin: the fluid travelling with the moved block. */
    public interface MovingFluid {

        @Nullable
        Fluid waterlogged$getFluid();

        void waterlogged$setFluid(@Nullable Fluid fluid);
    }

    /** Takes the fluid out of the cell the moving block leaves; the piston TE at (x, y, z) carries it. */
    public static void capture(World world, int x, int y, int z, TileEntityPiston piston) {
        int side = piston.getPistonOrientation();
        // Pushed blocks come from behind the destination, pulled blocks from in front of it
        int sign = piston.isExtending() ? -1 : 1;
        int sx = x + sign * Facing.offsetsXForSide[side];
        int sy = y + sign * Facing.offsetsYForSide[side];
        int sz = z + sign * Facing.offsetsZForSide[side];
        Fluid fluid = WaterloggedAPI.getFluid(world, sx, sy, sz);
        if (fluid == null) {
            return;
        }
        ((MovingFluid) piston).waterlogged$setFluid(fluid);
        // Silent clear: the cell is overwritten right after, and a source left behind would be wrong
        ChunkFluids.set(world.getChunkFromBlockCoords(sx, sz), sx & 15, sy, sz & 15, null);
    }

    /** Puts the carried fluid into the block the piston just placed at (x, y, z). */
    public static void release(World world, int x, int y, int z, TileEntityPiston piston) {
        MovingFluid moving = (MovingFluid) piston;
        Fluid fluid = moving.waterlogged$getFluid();
        if (fluid == null || world.isRemote) {
            return;
        }
        moving.waterlogged$setFluid(null);
        WaterloggedAPI.setFluid(world, x, y, z, fluid);
    }
}
