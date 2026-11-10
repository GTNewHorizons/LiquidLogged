package com.gtnewhorizons.waterlogged.mixins.early.minecraft.client;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

/** Client only: shouldSideBeRendered may be stripped on a dedicated server. */
@Mixin(BlockFluidBase.class)
public abstract class MixinBlockFluidBase {

    // No face between a Forge fluid and a neighbouring cell that holds the same fluid
    @WrapOperation(
        method = "shouldSideBeRendered",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block waterlogged$fluidAsBlock(IBlockAccess world, int x, int y, int z, Operation<Block> original) {
        return RenderHooks.blockOrFluid(world, x, y, z, original.call(world, x, y, z));
    }
}
