package com.gtnewhorizons.liquidlogged.internal.storage;

/** Added to {@code S23PacketBlockChange} by mixin; carries the network id of the cell's fluid, -1 for none. */
public interface LiquidLoggedBlockPacket {

    int liquidlogged$getFluidId();

    void liquidlogged$setFluidId(int id);
}
