package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Vanilla spreading helpers, reused so a stored source spreads exactly like a vanilla source. */
@Mixin(BlockDynamicLiquid.class)
public interface AccessorBlockDynamicLiquid {

    @Invoker("func_149808_o")
    boolean[] waterlogged$optimalFlowDirections(World world, int x, int y, int z);

    @Invoker("func_149813_h")
    void waterlogged$flowInto(World world, int x, int y, int z, int meta);

    @Invoker("func_149809_q")
    boolean waterlogged$canFlowInto(World world, int x, int y, int z);
}
