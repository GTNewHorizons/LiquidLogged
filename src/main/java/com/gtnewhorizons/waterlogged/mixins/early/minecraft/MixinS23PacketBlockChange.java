package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.network.play.server.S23PacketBlockChange;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.waterlogged.internal.storage.WaterloggedBlockPacket;

@Mixin(S23PacketBlockChange.class)
public abstract class MixinS23PacketBlockChange implements WaterloggedBlockPacket {

    @Unique
    private int waterlogged$fluidId = -1;

    @Override
    public int waterlogged$getFluidId() {
        return waterlogged$fluidId;
    }

    @Override
    public void waterlogged$setFluidId(int id) {
        waterlogged$fluidId = id;
    }
}
