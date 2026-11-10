package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.gtnewhorizons.liquidlogged.internal.light.LightHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(World.class)
public abstract class MixinWorld_Light {

    @WrapOperation(
        method = "computeLightValue",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getLightValue(Lnet/minecraft/world/IBlockAccess;III)I",
            remap = false))
    private int liquidlogged$lightValue(Block block, IBlockAccess world, int x, int y, int z,
        Operation<Integer> original) {
        return Math
            .max(original.call(block, world, x, y, z), LightHooks.fluidLight(LiquidLoggedAPI.getFluid(world, x, y, z)));
    }

    @WrapOperation(
        method = "computeLightValue",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getLightOpacity(Lnet/minecraft/world/IBlockAccess;III)I",
            remap = false))
    private int liquidlogged$lightOpacity(Block block, IBlockAccess world, int x, int y, int z,
        Operation<Integer> original) {
        return Math.max(
            original.call(block, world, x, y, z),
            LightHooks.fluidOpacity(LiquidLoggedAPI.getFluid(world, x, y, z)));
    }
}
