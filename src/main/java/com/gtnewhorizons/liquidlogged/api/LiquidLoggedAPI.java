package com.gtnewhorizons.liquidlogged.api;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.liquidlogged.api.event.FluidLogEvent;
import com.gtnewhorizons.liquidlogged.internal.WorldFluids;
import com.gtnewhorizons.liquidlogged.internal.rules.HolderRegistry;
import com.gtnewhorizons.liquidlogged.internal.storage.LiquidLoggedSection;

/** Entry point for other mods: read and change the fluid stored inside blocks. */
public final class LiquidLoggedAPI {

    private LiquidLoggedAPI() {}

    /** Returns the stored fluid, or null if none or the access is not a {@link LiquidLoggedBlockAccess}. */
    public static @Nullable Fluid getFluid(IBlockAccess world, int x, int y, int z) {
        return world instanceof LiquidLoggedBlockAccess
            ? ((LiquidLoggedBlockAccess) world).liquidlogged$getFluid(x, y, z)
            : null;
    }

    /** Returns a read-only view of a section's fluids, or null when the section holds none. */
    public static @Nullable SectionFluidsView getSection(ExtendedBlockStorage section) {
        return ((LiquidLoggedSection) section).liquidlogged$get();
    }

    /** Lets {@code predicate} decide for {@code block}; use it for blocks whose code you cannot change. */
    public static void registerPredicate(Block block, FluidHolderPredicate predicate) {
        HolderRegistry.register(block, predicate);
    }

    /** Returns true if the block placed at this position may hold the fluid; false where the chunk is not loaded. */
    public static boolean canHold(World world, int x, int y, int z, Fluid fluid) {
        // world.getBlock would load or even generate the chunk
        return world.blockExists(x, y, z) && HolderRegistry.canHold(world, x, y, z, fluid);
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

    /** Drops the stored fluid if the block no longer accepts it; call after a TE change that keeps the block. */
    public static void revalidate(World world, int x, int y, int z) {
        Fluid fluid = getFluid(world, x, y, z);
        if (fluid != null && !canHold(world, x, y, z, fluid)) {
            setFluid(world, x, y, z, null);
        }
    }
}
