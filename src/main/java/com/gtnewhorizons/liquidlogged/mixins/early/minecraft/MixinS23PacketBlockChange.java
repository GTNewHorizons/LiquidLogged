package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.network.play.server.S23PacketBlockChange;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.liquidlogged.internal.storage.LiquidLoggedBlockPacket;

@Mixin(S23PacketBlockChange.class)
public abstract class MixinS23PacketBlockChange implements LiquidLoggedBlockPacket {

    @Unique
    private int liquidlogged$fluidId = -1;

    @Unique
    private int liquidlogged$fluidLevel;

    @Override
    public int liquidlogged$getFluidId() {
        return liquidlogged$fluidId;
    }

    @Override
    public void liquidlogged$setFluidId(int id) {
        liquidlogged$fluidId = id;
    }

    @Override
    public int liquidlogged$getFluidLevel() {
        return liquidlogged$fluidLevel;
    }

    @Override
    public void liquidlogged$setFluidLevel(int level) {
        liquidlogged$fluidLevel = level;
    }
}
