package com.gtnewhorizons.liquidlogged.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

/** Loads mixins into other mods' classes when those mods are installed. */
@LateMixin
public class LiquidLoggedLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.liquidlogged.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixins = new ArrayList<>();
        if (loadedMods.contains("archaicfix")) {
            mixins.add("archaicfix.MixinLightingEngine");
        }
        return mixins;
    }
}
