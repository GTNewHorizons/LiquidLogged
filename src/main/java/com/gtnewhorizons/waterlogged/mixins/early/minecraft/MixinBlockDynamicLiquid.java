package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.waterlogged.internal.flow.LoggedFlow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid {

    // As in 26.2, flowing liquid does not wash away blocks that may hold a fluid (rails, signs, ladders...)
    @Inject(method = "func_149807_p", at = @At("HEAD"), cancellable = true)
    private void waterlogged$holdersBlockFlow(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (LoggedFlow.isHolder(world.getBlock(x, y, z))) {
            cir.setReturnValue(true);
        }
    }

    // A stored fluid behind a closed face (stair back, slab bottom) must not feed this cell or count as its source
    @WrapOperation(
        method = "updateTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockDynamicLiquid;func_149810_a(Lnet/minecraft/world/World;IIII)I"))
    private int waterlogged$sideNeighbour(BlockDynamicLiquid self, World world, int nx, int ny, int nz, int smallest,
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
    private int waterlogged$neighbourAbove(BlockDynamicLiquid self, World world, int nx, int ny, int nz,
        Operation<Integer> original, @Local(argsOnly = true, ordinal = 0) int x,
        @Local(argsOnly = true, ordinal = 1) int y, @Local(argsOnly = true, ordinal = 2) int z) {
        if (LoggedFlow.isClosedStoredNeighbour(world, x, y, z, nx, ny, nz)) {
            return -1;
        }
        return original.call(self, world, nx, ny, nz);
    }
}
