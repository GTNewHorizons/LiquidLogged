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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.gtnewhorizons.liquidlogged.LiquidLogged;
import com.gtnewhorizons.liquidlogged.api.IFluidHolder;
import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.gtnewhorizons.liquidlogged.api.event.FluidLogEvent;
import com.gtnewhorizons.liquidlogged.internal.WorldFluids;
import com.gtnewhorizons.liquidlogged.internal.rules.FaceSolidity;
import com.gtnewhorizons.liquidlogged.internal.rules.HolderRegistry;
import com.gtnewhorizons.liquidlogged.internal.rules.VanillaHolders;
import com.gtnewhorizons.liquidlogged.mixins.early.minecraft.AccessorBlockDynamicLiquid;
import com.gtnewhorizons.liquidlogged.mixins.early.minecraft.AccessorBlockLiquid;

/** Updates vanilla liquids stored inside other blocks. */
public final class LoggedFlow {

    private static final ForgeDirection[] SIDEWAYS = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH,
        ForgeDirection.SOUTH };

    private static final Set<String> WARNED = Collections.synchronizedSet(new HashSet<>());

    private LoggedFlow() {}

    /** Recalculates and spreads the stored fluid at (x, y, z). */
    public static void tick(World world, int x, int y, int z, Fluid fluid) {
        Block flowing = flowingBlock(fluid);
        if (flowing == null) {
            if (WARNED.add(fluid.getName())) {
                LiquidLogged.LOG
                    .warn("Stored fluid '{}' is not a vanilla liquid; it does not flow out of blocks", fluid.getName());
            }
            return;
        }
        int level = WorldFluids.level(world, x, y, z);
        if (level != 0) {
            int next = LiquidLoggedAPI.canHold(world, x, y, z, fluid) ? incomingWaterLevel(world, x, y, z) : -1;
            if (next != level) {
                if (!WorldFluids.setUnchecked(world, x, y, z, next < 0 ? null : fluid, Math.max(next, 0)) || next < 0) {
                    return;
                }
                level = next;
            }
        }
        AccessorBlockDynamicLiquid liquid = (AccessorBlockDynamicLiquid) flowing;
        if (fluid == FluidRegistry.WATER && canEnterHolder(world, x, y, z, x, y - 1, z)
            && flowIntoHolder(world, x, y - 1, z, level | 8)) {
            return;
        }
        if (level != 0 && waterLevel(world, x, y, z, ForgeDirection.DOWN) >= 0) {
            return;
        }
        if (canPass(world, x, y, z, ForgeDirection.DOWN) && liquid.liquidlogged$canFlowInto(world, x, y - 1, z)) {
            // Vanilla BlockDynamicLiquid.updateTick: lava falling onto water turns it into stone
            if (flowing.getMaterial() == Material.lava && world.getBlock(x, y - 1, z)
                .getMaterial() == Material.water) {
                world.setBlock(x, y - 1, z, Blocks.stone);
                ((AccessorBlockLiquid) flowing).liquidlogged$fizz(world, x, y - 1, z);
                return;
            }
            liquid.liquidlogged$flowInto(world, x, y - 1, z, level | 8);
            return;
        }
        int step = flowing.getMaterial() == Material.lava && !world.provider.isHellWorld ? 2 : 1;
        int out = level >= 8 ? 1 : level + step;
        if (out >= 8) {
            return;
        }
        // Index order of vanilla func_149808_o: x-1, x+1, z-1, z+1
        boolean[] dirs = liquid.liquidlogged$optimalFlowDirections(world, x, y, z);
        for (int i = 0; i < SIDEWAYS.length; i++) {
            ForgeDirection d = SIDEWAYS[i];
            if (dirs[i] && canPass(world, x, y, z, d)) {
                int nx = x + d.offsetX;
                int nz = z + d.offsetZ;
                if (fluid == FluidRegistry.WATER && canEnterHolder(world, x, y, z, nx, y, nz)) {
                    flowIntoHolder(world, nx, y, nz, out);
                } else {
                    liquid.liquidlogged$flowInto(world, nx, y, nz, out);
                }
            }
        }
    }

    private static int incomingWaterLevel(World world, int x, int y, int z) {
        int above = waterLevel(world, x, y, z, ForgeDirection.UP);
        if (above >= 0) {
            return above | 8;
        }
        int next = 8;
        for (ForgeDirection side : SIDEWAYS) {
            int level = waterLevel(world, x, y, z, side);
            if (level >= 0) {
                next = Math.min(next, level >= 8 ? 1 : level + 1);
            }
        }
        return next < 8 ? next : -1;
    }

    private static int waterLevel(World world, int x, int y, int z, ForgeDirection side) {
        if (!canPass(world, x, y, z, side)) {
            return -1;
        }
        int nx = x + side.offsetX;
        int ny = y + side.offsetY;
        int nz = z + side.offsetZ;
        if (LiquidLoggedAPI.getFluid(world, nx, ny, nz) == FluidRegistry.WATER) {
            return WorldFluids.level(world, nx, ny, nz);
        }
        return world.getBlock(nx, ny, nz)
            .getMaterial() == Material.water ? world.getBlockMetadata(nx, ny, nz) : -1;
    }

    public static boolean canEnterHolder(World world, int x, int y, int z, int nx, int ny, int nz) {
        if (!LiquidLoggedAPI.canHold(world, nx, ny, nz, FluidRegistry.WATER)) {
            return false;
        }
        Fluid stored = LiquidLoggedAPI.getFluid(world, nx, ny, nz);
        return (stored == null || stored == FluidRegistry.WATER && WorldFluids.level(world, nx, ny, nz) > 0)
            && canPassBetween(world, x, y, z, nx, ny, nz);
    }

    public static boolean canStrengthenWater(World world, int x, int y, int z, int level) {
        Fluid stored = LiquidLoggedAPI.getFluid(world, x, y, z);
        if (stored == null) {
            return true;
        }
        if (stored != FluidRegistry.WATER) {
            return false;
        }
        int old = WorldFluids.level(world, x, y, z);
        boolean falling = level >= 8;
        boolean wasFalling = old >= 8;
        return old > 0 && (falling == wasFalling ? level < old : falling);
    }

    public static boolean flowIntoHolder(World world, int x, int y, int z, int level) {
        if (!canStrengthenWater(world, x, y, z, level)) {
            return false;
        }
        Fluid stored = LiquidLoggedAPI.getFluid(world, x, y, z);
        if (stored == null
            && MinecraftForge.EVENT_BUS.post(new FluidLogEvent(world, x, y, z, null, FluidRegistry.WATER))) {
            return false;
        }
        return WorldFluids.setUnchecked(world, x, y, z, FluidRegistry.WATER, level);
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
        return LiquidLoggedAPI.getFluid(world, nx, ny, nz) != null && !canPassBetween(world, x, y, z, nx, ny, nz);
    }

    public static boolean canPassBetween(IBlockAccess world, int x, int y, int z, int nx, int ny, int nz) {
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            if (x + d.offsetX == nx && y + d.offsetY == ny && z + d.offsetZ == nz) {
                return canPass(world, x, y, z, d);
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
    public static boolean isHolder(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return VanillaHolders.isVanillaHolder(block) || block instanceof IFluidHolder
            || LiquidLoggedAPI.getFluid(world, x, y, z) != null
            || HolderRegistry.canHold(world, x, y, z, FluidRegistry.WATER);
    }
}
