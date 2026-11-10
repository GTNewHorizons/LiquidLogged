package com.gtnewhorizons.liquidlogged.internal.flow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.gtnewhorizons.liquidlogged.LiquidLogged;
import com.gtnewhorizons.liquidlogged.api.IFluidHolder;
import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.gtnewhorizons.liquidlogged.internal.rules.FaceSolidity;
import com.gtnewhorizons.liquidlogged.internal.rules.VanillaHolders;
import com.gtnewhorizons.liquidlogged.mixins.early.minecraft.AccessorBlockDynamicLiquid;
import com.gtnewhorizons.liquidlogged.mixins.early.minecraft.AccessorBlockLiquid;

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
                LiquidLogged.LOG
                    .warn("Stored fluid '{}' is not a vanilla liquid; it does not flow out of blocks", fluid.getName());
            }
            return;
        }
        AccessorBlockDynamicLiquid liquid = (AccessorBlockDynamicLiquid) flowing;
        if (canPass(world, x, y, z, ForgeDirection.DOWN) && liquid.liquidlogged$canFlowInto(world, x, y - 1, z)) {
            // Vanilla BlockDynamicLiquid.updateTick: lava falling onto water turns it into stone
            if (flowing.getMaterial() == Material.lava && world.getBlock(x, y - 1, z)
                .getMaterial() == Material.water) {
                world.setBlock(x, y - 1, z, Blocks.stone);
                ((AccessorBlockLiquid) flowing).liquidlogged$fizz(world, x, y - 1, z);
                return;
            }
            liquid.liquidlogged$flowInto(world, x, y - 1, z, 8);
            return;
        }
        int level = flowing.getMaterial() == Material.lava && !world.provider.isHellWorld ? 2 : 1;
        // Index order of vanilla func_149808_o: x-1, x+1, z-1, z+1
        boolean[] dirs = liquid.liquidlogged$optimalFlowDirections(world, x, y, z);
        for (int i = 0; i < SIDEWAYS.length; i++) {
            ForgeDirection d = SIDEWAYS[i];
            if (dirs[i] && canPass(world, x, y, z, d)) {
                liquid.liquidlogged$flowInto(world, x + d.offsetX, y, z + d.offsetZ, level);
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

    /** Returns true if the stored fluid in neighbour (nx, ny, nz) must not feed cell (x, y, z) through their face. */
    public static boolean isClosedStoredNeighbour(IBlockAccess world, int x, int y, int z, int nx, int ny, int nz) {
        if (LiquidLoggedAPI.getFluid(world, nx, ny, nz) == null) {
            return false;
        }
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if (x + d.offsetX == nx && y + d.offsetY == ny && z + d.offsetZ == nz) {
                return !canPass(world, x, y, z, d);
            }
        }
        return false;
    }

    /** Returns true if the block in the cell closes the given side to fluid flow. */
    public static boolean isSideClosed(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return isSideClosed(world, x, y, z, side, false);
    }

    /** Returns true if the block hides the stored fluid's face on that side when drawn. */
    public static boolean isSideCoveredForRender(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return isSideClosed(world, x, y, z, side, true);
    }

    private static boolean isSideClosed(IBlockAccess world, int x, int y, int z, ForgeDirection side,
        boolean forRender) {
        Block block = world.getBlock(x, y, z);
        // Leaves (including other mods') stop flow but are see-through; 26.2 still draws the water inside them
        if (block instanceof BlockLeaves) {
            return !forRender;
        }
        if (VanillaHolders.isVanillaHolder(block)) {
            return FaceSolidity.isSolid(shapeOf(block), world.getBlockMetadata(x, y, z), side);
        }
        // Fluid is drawn inside a see-through block; flow stays closed by the checks below
        if (forRender && !block.isOpaqueCube()) {
            return false;
        }
        if (block instanceof IFluidHolder) {
            return !((IFluidHolder) block).isFluidPassable(world, x, y, z, side);
        }
        return block.isSideSolid(world, x, y, z, side);
    }

    private static FaceSolidity.Shape shapeOf(Block block) {
        if (block instanceof BlockStairs) {
            return FaceSolidity.Shape.STAIRS;
        }
        if (block instanceof BlockSlab) {
            return FaceSolidity.Shape.SLAB;
        }
        if (block instanceof BlockLeaves) {
            return FaceSolidity.Shape.LEAVES;
        }
        if (block instanceof BlockTrapDoor) {
            return FaceSolidity.Shape.TRAPDOOR;
        }
        return FaceSolidity.Shape.OTHER;
    }

    /** Returns true if flowing fluid must not wash this block away, because it may hold a fluid. */
    public static boolean isHolder(Block block) {
        return VanillaHolders.isVanillaHolder(block) || block instanceof IFluidHolder;
    }
}
