package com.gtnewhorizons.waterlogged.mixins.early.minecraft.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(RenderBlocks.class)
public abstract class MixinRenderBlocks {

    @Shadow
    public IBlockAccess blockAccess;

    @WrapOperation(
        method = { "renderBlockLiquid", "getLiquidHeight" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block waterlogged$fluidAsBlock(IBlockAccess world, int x, int y, int z, Operation<Block> original) {
        return RenderHooks.blockOrFluid(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = { "renderBlockLiquid", "getLiquidHeight" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockAccess;getBlockMetadata(III)I"))
    private int waterlogged$fluidAsSource(IBlockAccess world, int x, int y, int z, Operation<Integer> original) {
        return RenderHooks.metaOrSource(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = "renderBlockLiquid",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;IIII)Z"))
    private boolean waterlogged$hideCoveredFaces(Block fluid, IBlockAccess world, int nx, int ny, int nz, int side,
        Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (RenderHooks.isRenderingStored() && RenderHooks.ownSideCovered(world, x, y, z, side)) {
            return false;
        }
        return original.call(fluid, world, nx, ny, nz, side);
    }
}
