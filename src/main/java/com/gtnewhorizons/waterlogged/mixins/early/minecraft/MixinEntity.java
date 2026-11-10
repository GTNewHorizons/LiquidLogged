package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.flow.MaterialHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(Entity.class)
public abstract class MixinEntity {

    // Eyes inside a block holding water count as underwater: air bubbles and drowning
    @WrapOperation(
        method = "isInsideOfMaterial",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block waterlogged$eyeBlock(World world, int x, int y, int z, Operation<Block> original,
        @Local(argsOnly = true) Material material) {
        return MaterialHooks.blockFor(world, x, y, z, original.call(world, x, y, z), material);
    }
}
