package com.gtnewhorizons.waterlogged.internal.fill;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.waterlogged.api.WaterloggedAPI;

/** Bucket use on blocks that hold or may hold a fluid (design §7.2.2–3); null means "let vanilla handle it". */
public final class BucketHooks {

    private BucketHooks() {}

    /** Full bucket aimed at a block that may hold its fluid: store the fluid in that block. */
    public static @Nullable ItemStack pour(ItemStack stack, World world, EntityPlayer player,
        @Nullable MovingObjectPosition hit) {
        if (!isBlockHit(hit)) {
            return null;
        }
        FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(stack);
        Fluid fluid = contained == null ? null : contained.getFluid();
        int x = hit.blockX, y = hit.blockY, z = hit.blockZ;
        if (fluid == null || WaterloggedAPI.getFluid(world, x, y, z) != null
            || !WaterloggedAPI.canHold(world, x, y, z, fluid)) {
            return null;
        }
        if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, hit.sideHit, stack)) {
            return stack;
        }
        if (!world.isRemote && !WaterloggedAPI.setFluid(world, x, y, z, fluid)) {
            return stack;
        }
        if (player.capabilities.isCreativeMode) {
            return stack;
        }
        ItemStack emptied = FluidContainerRegistry.drainFluidContainer(stack);
        return emptied != null ? emptied : stack;
    }

    /** Empty bucket aimed at a block holding a fluid: take the fluid out. */
    public static @Nullable ItemStack pickUp(ItemStack stack, World world, EntityPlayer player,
        @Nullable MovingObjectPosition hit) {
        if (!isBlockHit(hit)) {
            return null;
        }
        int x = hit.blockX, y = hit.blockY, z = hit.blockZ;
        Fluid fluid = WaterloggedAPI.getFluid(world, x, y, z);
        if (fluid == null) {
            return null;
        }
        ItemStack filled = FluidContainerRegistry
            .fillFluidContainer(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME), stack);
        if (filled == null) {
            return null;
        }
        if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, hit.sideHit, stack)) {
            return stack;
        }
        if (!world.isRemote && !WaterloggedAPI.setFluid(world, x, y, z, null)) {
            return stack;
        }
        if (player.capabilities.isCreativeMode) {
            return stack;
        }
        if (--stack.stackSize <= 0) {
            return filled;
        }
        if (!player.inventory.addItemStackToInventory(filled)) {
            player.dropPlayerItemWithRandomChoice(filled, false);
        }
        return stack;
    }

    /** Stores the bucket's fluid in the block at the target cell if it may hold it; true when done. */
    public static boolean pourInto(World world, int x, int y, int z, Block bucketContent) {
        Fluid fluid = PlacementHandler.sourceFluid(bucketContent, 0);
        if (fluid == null || WaterloggedAPI.getFluid(world, x, y, z) != null
            || !WaterloggedAPI.canHold(world, x, y, z, fluid)) {
            return false;
        }
        return world.isRemote || WaterloggedAPI.setFluid(world, x, y, z, fluid);
    }

    private static boolean isBlockHit(@Nullable MovingObjectPosition hit) {
        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }
}
