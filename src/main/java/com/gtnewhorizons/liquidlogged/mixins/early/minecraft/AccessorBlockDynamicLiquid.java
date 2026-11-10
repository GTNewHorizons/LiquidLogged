package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Invokers for the private spreading helpers of BlockDynamicLiquid. */
@Mixin(BlockDynamicLiquid.class)
public interface AccessorBlockDynamicLiquid {

    @Invoker("func_149808_o")
    boolean[] liquidlogged$optimalFlowDirections(World world, int x, int y, int z);

    @Invoker("func_149813_h")
    void liquidlogged$flowInto(World world, int x, int y, int z, int meta);

    @Invoker("func_149809_q")
    boolean liquidlogged$canFlowInto(World world, int x, int y, int z);
}
