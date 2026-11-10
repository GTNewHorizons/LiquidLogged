package com.gtnewhorizons.liquidlogged.internal.client;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.gtnewhorizons.liquidlogged.internal.flow.LoggedFlow;

/** Render and material helpers: a cell holding a fluid looks like that fluid's source block. */
public final class RenderHooks {

    // True while a fluid stored in a block is being drawn; Angelica meshes off-thread, hence per thread
    private static final ThreadLocal<Boolean> RENDERING_STORED = ThreadLocal.withInitial(() -> false);

    private RenderHooks() {}

    public static @Nullable Block storedFluidBlock(IBlockAccess world, int x, int y, int z) {
        Fluid fluid = LiquidLoggedAPI.getFluid(world, x, y, z);
        return fluid == null ? null : fluid.getBlock();
    }

    /** Returns the stored fluid's block for a cell that holds one, else {@code block}. */
    public static Block blockOrFluid(IBlockAccess world, int x, int y, int z, Block block) {
        Block fluidBlock = storedFluidBlock(world, x, y, z);
        return fluidBlock != null ? fluidBlock : block;
    }

    /** Returns 0 (source) for a cell that holds a fluid, else {@code meta}. */
    public static int metaOrSource(IBlockAccess world, int x, int y, int z, int meta) {
        return storedFluidBlock(world, x, y, z) != null ? 0 : meta;
    }

    public static boolean isRenderingStored() {
        return RENDERING_STORED.get();
    }

    public static void setRenderingStored(boolean value) {
        RENDERING_STORED.set(value);
    }

    /** Returns true if the block in the cell covers the given side, so the stored fluid must not draw a face there. */
    public static boolean ownSideCovered(IBlockAccess world, int x, int y, int z, int side) {
        return LoggedFlow.isSideCoveredForRender(world, x, y, z, ForgeDirection.getOrientation(side));
    }
}
