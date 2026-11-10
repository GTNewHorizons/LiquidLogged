package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.fill.PistonHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(BlockPistonBase.class)
public abstract class MixinBlockPistonBase {

    @WrapOperation(
        method = { "tryExtend", "onBlockEventReceived" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setTileEntity(IIILnet/minecraft/tileentity/TileEntity;)V"))
    private void waterlogged$carryFluid(World world, int x, int y, int z, TileEntity te, Operation<Void> original) {
        if (!world.isRemote && te instanceof TileEntityPiston) {
            PistonHooks.capture(world, x, y, z, (TileEntityPiston) te);
        }
        original.call(world, x, y, z, te);
    }
}
