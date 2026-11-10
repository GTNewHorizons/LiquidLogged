package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.liquidlogged.internal.fill.BucketHooks;

@Mixin(ItemBucket.class)
public abstract class MixinItemBucket extends Item {

    @Shadow
    private Block isFull;

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    private void liquidlogged$useOnFluidHolder(ItemStack stack, World world, EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir) {
        boolean empty = isFull == Blocks.air;
        MovingObjectPosition hit = getMovingObjectPositionFromPlayer(world, player, empty);
        ItemStack result = empty ? BucketHooks.pickUp(stack, world, player, hit)
            : BucketHooks.pour(stack, world, player, hit);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    // Covers dispensers and a player pouring next to a block that may hold the fluid
    @Inject(method = "tryPlaceContainedLiquid", at = @At("HEAD"), cancellable = true)
    private void liquidlogged$pourIntoHolder(World world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        if (BucketHooks.pourInto(world, x, y, z, isFull)) {
            cir.setReturnValue(true);
        }
    }
}
