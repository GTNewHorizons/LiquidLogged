package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(BlockLiquid.class)
public abstract class MixinBlockLiquid {

    // Neighbouring cells holding a fluid count as that fluid's source: flow points away from them
    @WrapOperation(
        method = "getEffectiveFlowDecay",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block waterlogged$fluidAsBlock(IBlockAccess world, int x, int y, int z, Operation<Block> original) {
        return RenderHooks.blockOrFluid(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = "getEffectiveFlowDecay",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockAccess;getBlockMetadata(III)I"))
    private int waterlogged$fluidAsSource(IBlockAccess world, int x, int y, int z, Operation<Integer> original) {
        return RenderHooks.metaOrSource(world, x, y, z, original.call(world, x, y, z));
    }

    // World variant used by BlockDynamicLiquid: neighbouring stored fluid feeds flow and counts as a source
    @WrapOperation(
        method = "func_149804_e",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block waterlogged$worldFluidAsBlock(World world, int x, int y, int z, Operation<Block> original) {
        return RenderHooks.blockOrFluid(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = "func_149804_e",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockMetadata(III)I"))
    private int waterlogged$worldFluidAsSource(World world, int x, int y, int z, Operation<Integer> original) {
        return RenderHooks.metaOrSource(world, x, y, z, original.call(world, x, y, z));
    }
}
