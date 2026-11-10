package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.waterlogged.api.WaterloggedAPI;
import com.gtnewhorizons.waterlogged.internal.flow.LoggedFlow;
import com.gtnewhorizons.waterlogged.internal.tick.FluidTick;
import com.gtnewhorizons.waterlogged.internal.tick.FluidTickList;
import com.gtnewhorizons.waterlogged.internal.tick.WorldFluidTicks;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer implements WorldFluidTicks {

    @Unique
    private static final int MAX_TICKS_PER_TICK = 1000;

    @Unique
    private static final int CHUNK_GUARD = 6;

    @Unique
    private final FluidTickList waterlogged$ticks = new FluidTickList();

    @Override
    public FluidTickList waterlogged$ticks() {
        return waterlogged$ticks;
    }

    @Inject(method = "tickUpdates", at = @At("RETURN"))
    private void waterlogged$tickStoredFluids(boolean runAll, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        long now = world.getTotalWorldTime();
        for (FluidTick tick : waterlogged$ticks.pollDue(now, MAX_TICKS_PER_TICK)) {
            Fluid fluid = WaterloggedAPI.getFluid(world, tick.x, tick.y, tick.z);
            if (fluid == null || fluid.getBlock() == null) {
                continue;
            }
            int rate = fluid.getBlock()
                .tickRate(world);
            // Same guard as Hodgepodge preventLoadingChunksWhenLiquidsFlow, applied always
            if (!world.checkChunksExist(
                tick.x - CHUNK_GUARD,
                tick.y,
                tick.z - CHUNK_GUARD,
                tick.x + CHUNK_GUARD,
                tick.y,
                tick.z + CHUNK_GUARD)) {
                waterlogged$ticks.schedule(tick.x, tick.y, tick.z, now + rate * 5L, tick.priority);
                continue;
            }
            LoggedFlow.tick(world, tick.x, tick.y, tick.z, fluid);
        }
    }
}
