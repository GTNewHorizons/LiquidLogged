package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.internal.flow.MaterialHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(World.class)
public abstract class MixinWorld_Materials {

    @WrapOperation(
        method = { "handleMaterialAcceleration", "isMaterialInBB" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block liquidlogged$materialBlock(World world, int x, int y, int z, Operation<Block> original,
        @Local(argsOnly = true) Material material) {
        return MaterialHooks.blockFor(world, x, y, z, original.call(world, x, y, z), material);
    }

    @WrapOperation(
        method = "handleMaterialAcceleration",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockMetadata(III)I"))
    private int liquidlogged$materialMeta(World world, int x, int y, int z, Operation<Integer> original,
        @Local(argsOnly = true) Material material) {
        return MaterialHooks.metaFor(world, x, y, z, original.call(world, x, y, z), material);
    }

    @WrapOperation(
        method = "isAnyLiquid",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block liquidlogged$liquidBlock(World world, int x, int y, int z, Operation<Block> original) {
        return MaterialHooks.liquidFor(world, x, y, z, original.call(world, x, y, z));
    }
}
