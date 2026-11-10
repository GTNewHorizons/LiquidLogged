package com.gtnewhorizons.waterlogged.internal.tick;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;

/** Added to {@code WorldServer} by mixin; holds that world's stored-fluid ticks. */
public interface WorldFluidTicks {

    FluidTickList waterlogged$ticks();

    /** Schedules a tick of the stored fluid at (x, y, z) after the fluid's own tick rate; server worlds only. */
    static void schedule(World world, int x, int y, int z, Fluid fluid) {
        if (!(world instanceof WorldServer) || fluid.getBlock() == null) {
            return;
        }
        int delay = fluid.getBlock()
            .tickRate(world);
        ((WorldFluidTicks) world).waterlogged$ticks()
            .schedule(x, y, z, world.getTotalWorldTime() + delay, 0);
    }
}
