package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(World.class)
public abstract class MixinWorld_RayTrace {

    // A ray that stops on liquids hits a block holding a fluid anywhere in the cell, not only on the block's shape
    @WrapOperation(
        method = "func_147447_a",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;collisionRayTrace(Lnet/minecraft/world/World;IIILnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;"))
    private MovingObjectPosition liquidlogged$hitStoredFluid(Block block, World world, int x, int y, int z, Vec3 start,
        Vec3 end, Operation<MovingObjectPosition> original, @Local(argsOnly = true, ordinal = 0) boolean stopOnLiquid) {
        MovingObjectPosition hit = original.call(block, world, x, y, z, start, end);
        if (hit != null || !stopOnLiquid) {
            return hit;
        }
        Fluid fluid = LiquidLoggedAPI.getFluid(world, x, y, z);
        Block fluidBlock = fluid == null ? null : fluid.getBlock();
        return fluidBlock == null ? null : fluidBlock.collisionRayTrace(world, x, y, z, start, end);
    }
}
