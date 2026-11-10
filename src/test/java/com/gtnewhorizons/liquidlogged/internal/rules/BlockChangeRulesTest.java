package com.gtnewhorizons.liquidlogged.internal.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import org.junit.jupiter.api.Test;

class BlockChangeRulesTest {

    private static final Block STAIRS = new Block(Material.rock) {};
    private static final Block FENCE = new Block(Material.wood) {};
    private static final Block AIR = new Block(Material.air) {};

    @Test
    void metaOnlyKeeps() {
        assertEquals(BlockChangeRules.Outcome.KEEP, BlockChangeRules.onChange(STAIRS, 0, STAIRS, 3));
    }

    @Test
    void toAirLeavesSource() {
        assertEquals(BlockChangeRules.Outcome.LEAVE_SOURCE, BlockChangeRules.onChange(STAIRS, 0, AIR, 0));
    }

    @Test
    void otherBlockClears() {
        assertEquals(BlockChangeRules.Outcome.CLEAR, BlockChangeRules.onChange(STAIRS, 0, FENCE, 0));
    }
}
