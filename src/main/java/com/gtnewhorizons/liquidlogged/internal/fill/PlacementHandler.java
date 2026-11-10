package com.gtnewhorizons.liquidlogged.internal.fill;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Fills a block a player places into a fluid source; register it on the Forge event bus. */
public class PlacementHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlace(BlockEvent.PlaceEvent event) {
        if (event instanceof BlockEvent.MultiPlaceEvent) {
            for (BlockSnapshot snapshot : ((BlockEvent.MultiPlaceEvent) event).getReplacedBlockSnapshots()) {
                fill(event.world, snapshot);
            }
        } else {
            fill(event.world, event.blockSnapshot);
        }
    }

    private static void fill(World world, BlockSnapshot snapshot) {
        Fluid fluid = sourceFluid(snapshot.getReplacedBlock(), snapshot.meta);
        if (fluid != null) {
            LiquidLoggedAPI.setFluid(world, snapshot.x, snapshot.y, snapshot.z, fluid);
        }
    }

    /** Returns the fluid if the replaced block was a full source, else null. */
    static @Nullable Fluid sourceFluid(Block replaced, int meta) {
        if (replaced == Blocks.water || replaced == Blocks.flowing_water) {
            return meta == 0 ? FluidRegistry.WATER : null;
        }
        if (replaced == Blocks.lava || replaced == Blocks.flowing_lava) {
            return meta == 0 ? FluidRegistry.LAVA : null;
        }
        if (replaced instanceof IFluidBlock) {
            Fluid fluid = ((IFluidBlock) replaced).getFluid();
            // Forge classic fluids use meta 0 for the source block
            return meta == 0 ? fluid : null;
        }
        return null;
    }
}
