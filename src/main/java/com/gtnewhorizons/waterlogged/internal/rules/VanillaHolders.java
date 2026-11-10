package com.gtnewhorizons.waterlogged.internal.rules;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/** Vanilla blocks that hold water as in Minecraft 26.2 (design §12); exact instances, never subclasses. */
public final class VanillaHolders {

    private static Set<Block> holders;

    private VanillaHolders() {}

    public static boolean isVanillaHolder(Block block) {
        if (holders == null) {
            holders = build();
        }
        return holders.contains(block);
    }

    private static Set<Block> build() {
        Set<Block> s = Collections.newSetFromMap(new IdentityHashMap<>());
        Collections.addAll(
            s,
            Blocks.oak_stairs,
            Blocks.stone_stairs,
            Blocks.brick_stairs,
            Blocks.stone_brick_stairs,
            Blocks.nether_brick_stairs,
            Blocks.sandstone_stairs,
            Blocks.spruce_stairs,
            Blocks.birch_stairs,
            Blocks.jungle_stairs,
            Blocks.quartz_stairs,
            Blocks.acacia_stairs,
            Blocks.dark_oak_stairs,
            Blocks.stone_slab,
            Blocks.wooden_slab,
            Blocks.fence,
            Blocks.nether_brick_fence,
            Blocks.glass_pane,
            Blocks.stained_glass_pane,
            Blocks.iron_bars,
            Blocks.cobblestone_wall,
            Blocks.ladder,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.ender_chest,
            Blocks.trapdoor,
            Blocks.rail,
            Blocks.golden_rail,
            Blocks.detector_rail,
            Blocks.activator_rail,
            Blocks.leaves,
            Blocks.leaves2);
        return s;
    }
}
