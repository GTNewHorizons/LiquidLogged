package com.gtnewhorizons.liquidlogged.api;

import net.minecraft.world.IBlockAccess;

import com.gtnewhorizons.liquidlogged.internal.client.RenderHooks;

/** Client helpers for renderers that draw stored fluids themselves, such as Angelica's chunk mesher. */
public final class LiquidLoggedRenderAPI {

    private LiquidLoggedRenderAPI() {}

    /** Call before drawing a cell's stored fluid with RenderBlocks; pair it with {@link #endStoredFluid()}. */
    public static void beginStoredFluid() {
        RenderHooks.setRenderingStored(true);
    }

    /** Call after drawing a cell's stored fluid, in a finally block. */
    public static void endStoredFluid() {
        RenderHooks.setRenderingStored(false);
    }

    /** Returns true if the block at (x, y, z) covers the given side, so its stored fluid draws no face there. */
    public static boolean isFaceCovered(IBlockAccess world, int x, int y, int z, int side) {
        return RenderHooks.ownSideCovered(world, x, y, z, side);
    }
}
