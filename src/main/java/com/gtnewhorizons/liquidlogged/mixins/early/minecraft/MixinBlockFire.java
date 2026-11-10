package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

// As in 26.2, a block holding water neither burns nor spreads fire. All targets are Forge methods, hence remap = false
@Mixin(BlockFire.class)
public abstract class MixinBlockFire {

    // The Forge overload with ForgeDirection; the vanilla tryCatchFire only delegates to it
    @WrapOperation(
        method = "tryCatchFire(Lnet/minecraft/world/World;IIIILjava/util/Random;ILnet/minecraftforge/common/util/ForgeDirection;)V",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getFlammability(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)I",
            remap = false))
    private int liquidlogged$flammability(Block block, IBlockAccess world, int x, int y, int z, ForgeDirection face,
        Operation<Integer> original) {
        return liquidlogged$holdsWater(world, x, y, z) ? 0 : original.call(block, world, x, y, z, face);
    }

    @WrapOperation(
        method = "getChanceToEncourageFire",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;getFireSpreadSpeed(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)I",
            remap = false))
    private int liquidlogged$spreadSpeed(Block block, IBlockAccess world, int x, int y, int z, ForgeDirection face,
        Operation<Integer> original) {
        return liquidlogged$holdsWater(world, x, y, z) ? 0 : original.call(block, world, x, y, z, face);
    }

    @WrapOperation(
        method = "canCatchFire",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;isFlammable(Lnet/minecraft/world/IBlockAccess;IIILnet/minecraftforge/common/util/ForgeDirection;)Z",
            remap = false))
    private boolean liquidlogged$flammable(Block block, IBlockAccess world, int x, int y, int z, ForgeDirection face,
        Operation<Boolean> original) {
        return !liquidlogged$holdsWater(world, x, y, z) && original.call(block, world, x, y, z, face);
    }

    @Unique
    private static boolean liquidlogged$holdsWater(IBlockAccess world, int x, int y, int z) {
        Block fluidBlock = RenderHooks.storedFluidBlock(world, x, y, z);
        return fluidBlock != null && fluidBlock.getMaterial() == Material.water;
    }
}
