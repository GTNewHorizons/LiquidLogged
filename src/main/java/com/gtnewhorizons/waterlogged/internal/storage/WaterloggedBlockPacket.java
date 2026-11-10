package com.gtnewhorizons.waterlogged.internal.storage;

/** Added to {@code S23PacketBlockChange} by mixin; carries the network id of the cell's fluid, -1 for none. */
public interface WaterloggedBlockPacket {

    int waterlogged$getFluidId();

    void waterlogged$setFluidId(int id);
}
