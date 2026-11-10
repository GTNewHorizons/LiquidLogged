package com.gtnewhorizons.waterlogged.internal.flow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.gtnewhorizons.waterlogged.Waterlogged;
import com.gtnewhorizons.waterlogged.api.IFluidHolder;
import com.gtnewhorizons.waterlogged.internal.rules.FaceSolidity;
import com.gtnewhorizons.waterlogged.internal.rules.VanillaHolders;
import com.gtnewhorizons.waterlogged.mixins.early.minecraft.AccessorBlockDynamicLiquid;

/** Flow out of a block that stores a fluid: it acts as a permanent source behind the block's closed faces. */
public final class LoggedFlow {

    private static final ForgeDirection[] SIDEWAYS = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH,
        ForgeDirection.SOUTH };

    private static final Set<String> WARNED = Collections.synchronizedSet(new HashSet<>());

    private LoggedFlow() {}

    /** Spreads the stored fluid at (x, y, z) once, like a vanilla source block would. */
    public static void tick(World world, int x, int y, int z, Fluid fluid) {
        Block flowing = flowingBlock(fluid);
        if (flowing == null) {
            if (WARNED.add(fluid.getName())) {
                Waterlogged.LOG
                    .warn("Stored fluid '{}' is not a vanilla liquid; it does not flow out of blocks", fluid.getName());
            }
            return;
        }
        AccessorBlockDynamicLiquid liquid = (AccessorBlockDynamicLiquid) flowing;
        if (canPass(world, x, y, z, ForgeDirection.DOWN) && liquid.waterlogged$canFlowInto(world, x, y - 1, z)) {
            liquid.waterlogged$flowInto(world, x, y - 1, z, 8);
            return;
        }
        int level = flowing.getMaterial() == Material.lava && !world.provider.isHellWorld ? 2 : 1;
        // Index order of vanilla func_149808_o: x-1, x+1, z-1, z+1
        boolean[] dirs = liquid.waterlogged$optimalFlowDirections(world, x, y, z);
        for (int i = 0; i < SIDEWAYS.length; i++) {
            ForgeDirection d = SIDEWAYS[i];
            if (dirs[i] && canPass(world, x, y, z, d)) {
                liquid.waterlogged$flowInto(world, x + d.offsetX, y, z + d.offsetZ, level);
            }
        }
    }

    /** Returns the flowing vanilla block for water and lava, null for other fluids. */
    public static @Nullable Block flowingBlock(Fluid fluid) {
        if (fluid == FluidRegistry.WATER) {
            return Blocks.flowing_water;
        }
        if (fluid == FluidRegistry.LAVA) {
            return Blocks.flowing_lava;
        }
        return null;
    }

    /** Returns false if the face between (x, y, z) and its neighbour in {@code dir} is closed on either side. */
    public static boolean canPass(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {
        return !isSideClosed(world, x, y, z, dir)
            && !isSideClosed(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.getOpposite());
    }

    /** Returns true if the block in the cell closes the given side to fluid. */
    public static boolean isSideClosed(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        Block block = world.getBlock(x, y, z);
        if (VanillaHolders.isVanillaHolder(block)) {
            FaceSolidity.Shape shape = block instanceof BlockStairs ? FaceSolidity.Shape.STAIRS
                : block instanceof BlockSlab ? FaceSolidity.Shape.SLAB : FaceSolidity.Shape.OTHER;
            return FaceSolidity.isSolid(shape, world.getBlockMetadata(x, y, z), side);
        }
        if (block instanceof IFluidHolder) {
            return !((IFluidHolder) block).isFluidPassable(world, x, y, z, side);
        }
        return block.isSideSolid(world, x, y, z, side);
    }

    /** Returns true if flowing fluid must not wash this block away, because it may hold a fluid. */
    public static boolean isHolder(Block block) {
        return VanillaHolders.isVanillaHolder(block) || block instanceof IFluidHolder;
    }
}
