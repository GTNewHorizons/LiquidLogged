package com.gtnewhorizons.waterlogged;

import net.minecraftforge.client.ClientCommandHandler;

import com.gtnewhorizons.waterlogged.internal.command.WaterloggedClientCommand;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientCommandHandler.instance.registerCommand(new WaterloggedClientCommand());
    }
}
