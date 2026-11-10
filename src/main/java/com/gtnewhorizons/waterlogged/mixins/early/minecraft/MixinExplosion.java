package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(Explosion.class)
public abstract class MixinExplosion {

    // As in 26.2: a cell resists an explosion as much as the stronger of its block and its stored fluid
    @WrapOperation(
        method = "doExplosionA",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getExplosionResistance(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;IIIDDD)F",
            remap = false))
    private float waterlogged$blockResistance(Block block, Entity exploder, World world, int x, int y, int z, double ex,
        double ey, double ez, Operation<Float> original) {
        float resistance = original.call(block, exploder, world, x, y, z, ex, ey, ez);
        Block fluidBlock = RenderHooks.storedFluidBlock(world, x, y, z);
        return fluidBlock == null ? resistance
            : Math.max(resistance, original.call(fluidBlock, exploder, world, x, y, z, ex, ey, ez));
    }

    @WrapOperation(
        method = "doExplosionA",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;func_145772_a(Lnet/minecraft/world/Explosion;Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)F"))
    private float waterlogged$entityResistance(Entity exploder, Explosion explosion, World world, int x, int y, int z,
        Block block, Operation<Float> original) {
        float resistance = original.call(exploder, explosion, world, x, y, z, block);
        Block fluidBlock = RenderHooks.storedFluidBlock(world, x, y, z);
        return fluidBlock == null ? resistance
            : Math.max(resistance, original.call(exploder, explosion, world, x, y, z, fluidBlock));
    }
}
