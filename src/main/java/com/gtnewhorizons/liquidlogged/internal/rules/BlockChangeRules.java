package com.gtnewhorizons.liquidlogged.internal.rules;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/** Decides what happens to a cell's fluid when its block changes. */
public final class BlockChangeRules {

    public enum Outcome {
        /** Same block, only meta changed: the fluid stays. */
        KEEP,
        /** Replaced by another block: the fluid is removed. */
        CLEAR,
        /** Block removed: the fluid becomes a source block in the cell. */
        LEAVE_SOURCE
    }

    private BlockChangeRules() {}

    /** Returns the block to place as a fresh source of the fluid, or null if the fluid has no block. */
    public static @Nullable Block sourceBlock(Fluid fluid) {
        // Vanilla still liquids never tick on their own; the flowing variant spreads like water poured from a bucket
        if (fluid == FluidRegistry.WATER) {
            return Blocks.flowing_water;
        }
        if (fluid == FluidRegistry.LAVA) {
            return Blocks.flowing_lava;
        }
        return fluid.getBlock();
    }

    public static Outcome onChange(Block oldBlock, int oldMeta, Block newBlock, int newMeta) {
        if (newBlock == oldBlock) {
            return Outcome.KEEP;
        }
        if (newBlock.getMaterial() == Material.air) {
            return Outcome.LEAVE_SOURCE;
        }
        return Outcome.CLEAR;
    }
}
