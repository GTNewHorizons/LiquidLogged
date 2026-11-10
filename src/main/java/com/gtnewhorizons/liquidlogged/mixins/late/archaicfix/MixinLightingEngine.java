package com.gtnewhorizons.liquidlogged.mixins.late.archaicfix;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.gtnewhorizons.liquidlogged.internal.light.LightHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

// ArchaicFix replaces vanilla light propagation (World.updateLightByType), so the vanilla hooks never run with it
@Mixin(targets = "org.embeddedt.archaicfix.lighting.world.lighting.LightingEngine", remap = false)
public abstract class MixinLightingEngine {

    @WrapOperation(
        method = "getPosOpacity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getLightOpacity(Lnet/minecraft/world/IBlockAccess;III)I"))
    private int liquidlogged$opacity(Block block, IBlockAccess world, int x, int y, int z,
        Operation<Integer> original) {
        return Math.max(
            original.call(block, world, x, y, z),
            LightHooks.fluidOpacity(LiquidLoggedAPI.getFluid(world, x, y, z)));
    }

    @WrapOperation(
        method = "getCursorLuminosity",
        at = @At(
            value = "INVOKE",
            target = "Lorg/embeddedt/archaicfix/lighting/world/lighting/LightingEngineHelpers;getLightValueForState(Lnet/minecraft/block/Block;Lnet/minecraft/world/IBlockAccess;III)I"))
    private int liquidlogged$lightValue(Block block, IBlockAccess world, int x, int y, int z,
        Operation<Integer> original) {
        return Math
            .max(original.call(block, world, x, y, z), LightHooks.fluidLight(LiquidLoggedAPI.getFluid(world, x, y, z)));
    }
}
