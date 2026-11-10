package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.waterlogged.internal.flow.LoggedFlow;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid {

    // As in 26.2, flowing liquid does not wash away blocks that may hold a fluid (rails, signs, ladders...)
    @Inject(method = "func_149807_p", at = @At("HEAD"), cancellable = true)
    private void waterlogged$holdersBlockFlow(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (LoggedFlow.isHolder(world.getBlock(x, y, z))) {
            cir.setReturnValue(true);
        }
    }
}
