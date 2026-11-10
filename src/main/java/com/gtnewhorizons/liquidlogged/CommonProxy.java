package com.gtnewhorizons.liquidlogged;

import net.minecraftforge.common.MinecraftForge;

import com.falsepattern.chunk.api.DataRegistry;
import com.gtnewhorizons.liquidlogged.internal.chunkapi.FluidsDataManager;
import com.gtnewhorizons.liquidlogged.internal.chunkapi.TicksDataManager;
import com.gtnewhorizons.liquidlogged.internal.command.LiquidLoggedCommand;
import com.gtnewhorizons.liquidlogged.internal.fill.PlacementHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        LiquidLogged.LOG.info("LiquidLogged " + Tags.VERSION);
    }

    public void init(FMLInitializationEvent event) {
        DataRegistry.registerDataManager(FluidsDataManager.INSTANCE, FluidsDataManager.ORDERING);
        DataRegistry.registerDataManager(TicksDataManager.INSTANCE, FluidsDataManager.ORDERING);
        MinecraftForge.EVENT_BUS.register(new PlacementHandler());
        MinecraftForge.EVENT_BUS.register(TicksDataManager.INSTANCE);
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new LiquidLoggedCommand());
    }
}
