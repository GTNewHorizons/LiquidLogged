package com.gtnewhorizons.liquidlogged.api;

import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

/** Implement on a Block to decide which fluids it may hold; called only when the block and its TE are in the world. */
public interface IFluidHolder {

    /** Returns true if the block at this position may hold the fluid. */
    boolean canHoldFluid(IBlockAccess world, int x, int y, int z, int meta, Fluid fluid);

    /** Returns true if a stored fluid may flow through this side of the block. */
    default boolean isFluidPassable(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return !world.getBlock(x, y, z)
            .isSideSolid(world, x, y, z, side);
    }
}
