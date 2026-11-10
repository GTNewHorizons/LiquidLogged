package com.gtnewhorizons.waterlogged.api;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.waterlogged.api.event.FluidLogEvent;
import com.gtnewhorizons.waterlogged.internal.WorldFluids;
import com.gtnewhorizons.waterlogged.internal.rules.HolderRegistry;
import com.gtnewhorizons.waterlogged.internal.storage.WaterloggedSection;

/** Entry point for other mods: read and change the fluid stored inside blocks. */
public final class WaterloggedAPI {

    private WaterloggedAPI() {}

    /**
     * Returns the fluid stored in the block, or null; null for any access that is not a {@link WaterloggedBlockAccess}.
     */
    public static @Nullable Fluid getFluid(IBlockAccess world, int x, int y, int z) {
        return world instanceof WaterloggedBlockAccess ? ((WaterloggedBlockAccess) world).waterlogged$getFluid(x, y, z)
            : null;
    }

    /** Returns a read-only view of a section's fluids, or null when the section holds none. */
    public static @Nullable SectionFluidsView getSection(ExtendedBlockStorage section) {
        return ((WaterloggedSection) section).waterlogged$get();
    }

    /** Lets {@code predicate} decide for {@code block}; use it for blocks whose code you cannot change. */
    public static void registerPredicate(Block block, FluidHolderPredicate predicate) {
        HolderRegistry.register(block, predicate);
    }

    /** Returns true if the block placed at this position may hold the fluid. */
    public static boolean canHold(World world, int x, int y, int z, Fluid fluid) {
        return HolderRegistry.canHold(world, x, y, z, fluid);
    }

    /** Stores the fluid in the block (null removes it); server side only, returns false if not allowed or cancelled. */
    public static boolean setFluid(World world, int x, int y, int z, @Nullable Fluid fluid) {
        if (world.isRemote) {
            return false;
        }
        if (fluid != null && !canHold(world, x, y, z, fluid)) {
            return false;
        }
        Fluid old = getFluid(world, x, y, z);
        if (old == fluid) {
            return true;
        }
        if (MinecraftForge.EVENT_BUS.post(new FluidLogEvent(world, x, y, z, old, fluid))) {
            return false;
        }
        return WorldFluids.setUnchecked(world, x, y, z, fluid);
    }

    /**
     * Removes the stored fluid if the block can no longer hold it; call after changing a TE without changing the block.
     */
    public static void revalidate(World world, int x, int y, int z) {
        Fluid fluid = getFluid(world, x, y, z);
        if (fluid != null && !canHold(world, x, y, z, fluid)) {
            setFluid(world, x, y, z, null);
        }
    }
}
