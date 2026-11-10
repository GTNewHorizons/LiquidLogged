package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedAPI;
import com.gtnewhorizons.liquidlogged.internal.flow.LoggedFlow;
import com.gtnewhorizons.liquidlogged.internal.tick.FluidTick;
import com.gtnewhorizons.liquidlogged.internal.tick.FluidTickList;
import com.gtnewhorizons.liquidlogged.internal.tick.WorldFluidTicks;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer implements WorldFluidTicks {

    @Unique
    private static final int MAX_TICKS_PER_TICK = 1000;

    @Unique
    private static final int CHUNK_GUARD = 6;

    // No field initializer: World's constructor already loads spawn chunks (and reads their ticks) before
    // WorldServer's fields are initialized, so an initializer would be null then and overwrite them after
    @Unique
    private FluidTickList liquidlogged$ticks;

    @Override
    public synchronized FluidTickList liquidlogged$ticks() {
        if (liquidlogged$ticks == null) {
            liquidlogged$ticks = new FluidTickList();
        }
        return liquidlogged$ticks;
    }

    @Inject(method = "tickUpdates", at = @At("RETURN"))
    private void liquidlogged$tickStoredFluids(boolean runAll, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        long now = world.getTotalWorldTime();
        FluidTickList ticks = liquidlogged$ticks();
        for (FluidTick tick : ticks.pollDue(now, MAX_TICKS_PER_TICK)) {
            Fluid fluid = LiquidLoggedAPI.getFluid(world, tick.x, tick.y, tick.z);
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
                ticks.schedule(tick.x, tick.y, tick.z, now + rate * 5L, tick.priority);
                continue;
            }
            LoggedFlow.tick(world, tick.x, tick.y, tick.z, fluid);
        }
    }
}
