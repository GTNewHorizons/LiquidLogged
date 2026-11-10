package com.gtnewhorizons.liquidlogged.mixins.early.minecraft.client;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.RenderBlockFluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(value = RenderBlockFluid.class, remap = false)
public abstract class MixinRenderBlockFluid {

    // Forge fluids: a cell holding the fluid counts as its source for heights and neighbour checks
    @WrapOperation(
        method = { "getFluidHeightForRender", "renderWorldBlock" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;",
            remap = true))
    private Block liquidlogged$fluidAsBlock(IBlockAccess world, int x, int y, int z, Operation<Block> original) {
        return RenderHooks.blockOrFluid(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = { "getFluidHeightForRender", "renderWorldBlock" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockAccess;getBlockMetadata(III)I", remap = true))
    private int liquidlogged$fluidAsSource(IBlockAccess world, int x, int y, int z, Operation<Integer> original) {
        return RenderHooks.metaOrSource(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = "renderWorldBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;shouldSideBeRendered(Lnet/minecraft/world/IBlockAccess;IIII)Z",
            remap = true))
    private boolean liquidlogged$hideCoveredFaces(Block fluid, IBlockAccess world, int nx, int ny, int nz, int side,
        Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (RenderHooks.isRenderingStored() && RenderHooks.ownSideCovered(world, x, y, z, side)) {
            return false;
        }
        return original.call(fluid, world, nx, ny, nz, side);
    }
}
