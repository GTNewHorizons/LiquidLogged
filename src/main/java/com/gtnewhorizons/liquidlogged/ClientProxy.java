package com.gtnewhorizons.liquidlogged;

import net.minecraftforge.client.ClientCommandHandler;

import com.gtnewhorizons.liquidlogged.internal.command.LiquidLoggedClientCommand;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientCommandHandler.instance.registerCommand(new LiquidLoggedClientCommand());
    }
}
