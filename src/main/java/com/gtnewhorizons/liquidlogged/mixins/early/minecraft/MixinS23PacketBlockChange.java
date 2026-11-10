package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.network.play.server.S23PacketBlockChange;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.liquidlogged.internal.storage.LiquidLoggedBlockPacket;

@Mixin(S23PacketBlockChange.class)
public abstract class MixinS23PacketBlockChange implements LiquidLoggedBlockPacket {

    @Unique
    private int liquidlogged$fluidId = -1;

    @Override
    public int liquidlogged$getFluidId() {
        return liquidlogged$fluidId;
    }

    @Override
    public void liquidlogged$setFluidId(int id) {
        liquidlogged$fluidId = id;
    }
}
