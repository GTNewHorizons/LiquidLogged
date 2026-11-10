package com.gtnewhorizons.waterlogged.api;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;

/** Decides for blocks whose code you cannot change; register with {@link WaterloggedAPI#registerPredicate}. */
@FunctionalInterface
public interface FluidHolderPredicate {

    /** Returns TRUE or FALSE to decide, or null to leave the decision to the next rule. */
    @Nullable
    Boolean canHoldFluid(IBlockAccess world, int x, int y, int z, Block block, int meta, Fluid fluid);
}
