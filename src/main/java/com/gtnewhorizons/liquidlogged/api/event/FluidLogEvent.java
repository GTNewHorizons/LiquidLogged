package com.gtnewhorizons.liquidlogged.api.event;

import javax.annotation.Nullable;

import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/** Fires on the Forge event bus before a block's stored fluid changes; cancel it to keep the old fluid. */
@Cancelable
public class FluidLogEvent extends Event {

    public final World world;
    public final int x;
    public final int y;
    public final int z;
    public final @Nullable Fluid oldFluid;
    public final @Nullable Fluid newFluid;

    public FluidLogEvent(World world, int x, int y, int z, @Nullable Fluid oldFluid, @Nullable Fluid newFluid) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.oldFluid = oldFluid;
        this.newFluid = newFluid;
    }
}
