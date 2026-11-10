package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.BlockLiquid;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Invoker for the fizz sound and smoke vanilla plays when lava meets water. */
@Mixin(BlockLiquid.class)
public interface AccessorBlockLiquid {

    @Invoker("func_149799_m")
    void liquidlogged$fizz(World world, int x, int y, int z);
}
