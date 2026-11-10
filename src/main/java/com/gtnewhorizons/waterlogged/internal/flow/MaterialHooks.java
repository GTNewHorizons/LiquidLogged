package com.gtnewhorizons.waterlogged.internal.flow;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;

/** Material checks (swimming, currents, drowning) that should see the fluid stored in a block. */
public final class MaterialHooks {

    private MaterialHooks() {}

    /** Returns the stored fluid's block when the cell's block is not of {@code material} but its fluid is. */
    public static Block blockFor(IBlockAccess world, int x, int y, int z, Block block, Material material) {
        if (block.getMaterial() == material) {
            return block;
        }
        Block fluidBlock = RenderHooks.storedFluidBlock(world, x, y, z);
        return fluidBlock != null && fluidBlock.getMaterial() == material ? fluidBlock : block;
    }

    /** Returns 0 (source) when the material match came from the stored fluid, else {@code meta}. */
    public static int metaFor(IBlockAccess world, int x, int y, int z, int meta, Material material) {
        if (world.getBlock(x, y, z)
            .getMaterial() == material) {
            return meta;
        }
        Block fluidBlock = RenderHooks.storedFluidBlock(world, x, y, z);
        return fluidBlock != null && fluidBlock.getMaterial() == material ? 0 : meta;
    }

    /** Returns the stored fluid's block when the cell's block is not a liquid, else {@code block}. */
    public static Block liquidFor(IBlockAccess world, int x, int y, int z, Block block) {
        if (block.getMaterial()
            .isLiquid()) {
            return block;
        }
        return RenderHooks.blockOrFluid(world, x, y, z, block);
    }
}
