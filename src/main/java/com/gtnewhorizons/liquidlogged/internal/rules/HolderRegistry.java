package com.gtnewhorizons.liquidlogged.internal.rules;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.gtnewhorizons.liquidlogged.api.FluidHolderPredicate;
import com.gtnewhorizons.liquidlogged.api.IFluidHolder;

/** Answers whether a block may hold a fluid: IFluidHolder first, then registered predicates, then the vanilla list. */
public final class HolderRegistry {

    private static final Map<Block, List<FluidHolderPredicate>> PREDICATES = new IdentityHashMap<>();

    private HolderRegistry() {}

    public static synchronized void register(Block block, FluidHolderPredicate predicate) {
        PREDICATES.computeIfAbsent(block, b -> new ArrayList<>())
            .add(predicate);
    }

    public static boolean canHold(IBlockAccess world, int x, int y, int z, Fluid fluid) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (block instanceof IFluidHolder) {
            return ((IFluidHolder) block).canHoldFluid(world, x, y, z, meta, fluid);
        }
        List<FluidHolderPredicate> predicates = PREDICATES.get(block);
        if (predicates != null) {
            for (FluidHolderPredicate p : predicates) {
                Boolean answer = p.canHoldFluid(world, x, y, z, block, meta, fluid);
                if (answer != null) {
                    return answer;
                }
            }
        }
        return fluid == FluidRegistry.WATER && VanillaHolders.isVanillaHolder(block);
    }
}
