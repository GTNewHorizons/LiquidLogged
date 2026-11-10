package com.gtnewhorizons.waterlogged;

import net.minecraftforge.common.MinecraftForge;

import com.falsepattern.chunk.api.DataRegistry;
import com.gtnewhorizons.waterlogged.internal.chunkapi.FluidsDataManager;
import com.gtnewhorizons.waterlogged.internal.chunkapi.TicksDataManager;
import com.gtnewhorizons.waterlogged.internal.command.WaterloggedCommand;
import com.gtnewhorizons.waterlogged.internal.fill.PlacementHandler;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Waterlogged.LOG.info("Waterlogged " + Tags.VERSION);
    }

    public void init(FMLInitializationEvent event) {
        DataRegistry.registerDataManager(FluidsDataManager.INSTANCE, FluidsDataManager.ORDERING);
        DataRegistry.registerDataManager(TicksDataManager.INSTANCE, FluidsDataManager.ORDERING);
        MinecraftForge.EVENT_BUS.register(new PlacementHandler());
        MinecraftForge.EVENT_BUS.register(TicksDataManager.INSTANCE);
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new WaterloggedCommand());
    }
}
