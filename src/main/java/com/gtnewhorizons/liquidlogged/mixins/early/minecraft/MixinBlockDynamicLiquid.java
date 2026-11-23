package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.liquidlogged.internal.flow.LoggedFlow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid {

    // As in 26.2, flowing liquid does not wash away blocks that may hold a fluid (rails, signs, ladders...)
    @Inject(method = "func_149807_p", at = @At("HEAD"), cancellable = true)
    private void liquidlogged$holdersBlockFlow(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (LoggedFlow.isHolder(world, x, y, z)) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(
        method = { "func_149808_o", "func_149812_c", "updateTick" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDynamicLiquid;func_149807_p(Lnet/minecraft/world/World;III)Z"))
    private boolean liquidlogged$routeBlocked(BlockDynamicLiquid liquid, World world, int nx, int ny, int nz,
        Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (ny != y) {
            x = nx;
            z = nz;
        }
        if (LoggedFlow.isHolder(world, nx, ny, nz)) {
            return liquid.getMaterial() != Material.water || !LoggedFlow.canEnterHolder(world, x, y, z, nx, ny, nz);
        }
        return LoggedFlow.isHolder(world, x, y, z) && !LoggedFlow.canPassBetween(world, x, y, z, nx, ny, nz)
            || original.call(liquid, world, nx, ny, nz);
    }

    @WrapOperation(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDynamicLiquid;func_149809_q(Lnet/minecraft/world/World;III)Z"))
    private boolean liquidlogged$canFlowDown(BlockDynamicLiquid liquid, World world, int nx, int ny, int nz,
        Operation<Boolean> original) {
        return original.call(liquid, world, nx, ny, nz) || liquid.getMaterial() == Material.water
            && world.getBlock(nx, ny + 1, nz)
                .getMaterial() == Material.water
            && LoggedFlow.canEnterHolder(world, nx, ny + 1, nz, nx, ny, nz)
            && LoggedFlow.canStrengthenWater(world, nx, ny, nz, world.getBlockMetadata(nx, ny + 1, nz) | 8);
    }

    @WrapOperation(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDynamicLiquid;func_149813_h(Lnet/minecraft/world/World;IIII)V"))
    private void liquidlogged$flowIntoHolder(BlockDynamicLiquid liquid, World world, int nx, int ny, int nz, int level,
        Operation<Void> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (liquid.getMaterial() == Material.water && LoggedFlow.isHolder(world, nx, ny, nz)) {
            if (world.getBlock(x, y, z)
                .getMaterial() == Material.water && LoggedFlow.canEnterHolder(world, x, y, z, nx, ny, nz)) {
                LoggedFlow.flowIntoHolder(world, nx, ny, nz, level);
            }
            return;
        }
        original.call(liquid, world, nx, ny, nz, level);
    }

    // A stored fluid behind a closed face (stair back, slab bottom) must not feed this cell or count as its source
    @WrapOperation(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDynamicLiquid;func_149810_a(Lnet/minecraft/world/World;IIII)I"))
    private int liquidlogged$sideNeighbour(BlockDynamicLiquid self, World world, int nx, int ny, int nz, int smallest,
        Operation<Integer> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (LoggedFlow.isClosedStoredNeighbour(world, x, y, z, nx, ny, nz)) {
            return smallest;
        }
        return original.call(self, world, nx, ny, nz, smallest);
    }

    @WrapOperation(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDynamicLiquid;func_149804_e(Lnet/minecraft/world/World;III)I"))
    private int liquidlogged$neighbourAbove(BlockDynamicLiquid self, World world, int nx, int ny, int nz,
        Operation<Integer> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (LoggedFlow.isClosedStoredNeighbour(world, x, y, z, nx, ny, nz)) {
            return -1;
        }
        return original.call(self, world, nx, ny, nz);
    }
}
