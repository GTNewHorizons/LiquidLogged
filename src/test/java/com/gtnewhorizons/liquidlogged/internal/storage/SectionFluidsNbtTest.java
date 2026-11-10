package com.gtnewhorizons.liquidlogged.internal.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.Fluid;

import org.junit.jupiter.api.Test;

class SectionFluidsNbtTest {

    private static final Fluid WATER = new Fluid("test_water");
    private static final Fluid OIL = new Fluid("test_oil");

    private static final Map<String, Fluid> KNOWN = new HashMap<>();
    static {
        KNOWN.put(WATER.getName(), WATER);
        KNOWN.put(OIL.getName(), OIL);
    }

    private static NBTTagCompound entry(String type, String name) {
        NBTTagCompound e = new NBTTagCompound();
        e.setString("t", type);
        e.setString("n", name);
        return e;
    }

    @Test
    void roundTrip() {
        SectionFluids f = new SectionFluids();
        f.set(SectionFluids.index(0, 0, 0), WATER);
        f.set(SectionFluids.index(1, 2, 3), OIL);
        f.set(SectionFluids.index(15, 15, 15), WATER);
        NBTTagCompound nbt = new NBTTagCompound();
        SectionFluidsNbt.write(f, nbt);

        SectionFluidsNbt.Result r = SectionFluidsNbt.read(nbt, KNOWN::get);
        assertFalse(r.droppedUnknown);
        assertEquals(3, r.fluids.count());
        assertSame(WATER, r.fluids.get(SectionFluids.index(0, 0, 0)));
        assertSame(OIL, r.fluids.get(SectionFluids.index(1, 2, 3)));
        assertSame(WATER, r.fluids.get(SectionFluids.index(15, 15, 15)));
    }

    @Test
    void unknownFluidNameIsDropped() {
        SectionFluids f = new SectionFluids();
        f.set(0, WATER);
        f.set(1, OIL);
        NBTTagCompound nbt = new NBTTagCompound();
        SectionFluidsNbt.write(f, nbt);

        Map<String, Fluid> withoutOil = new HashMap<>();
        withoutOil.put(WATER.getName(), WATER);
        SectionFluidsNbt.Result r = SectionFluidsNbt.read(nbt, withoutOil::get);
        assertTrue(r.droppedUnknown);
        assertSame(WATER, r.fluids.get(0));
        assertNull(r.fluids.get(1));
        assertEquals(1, r.fluids.count());
    }

    @Test
    void unknownEntryTypeIsSkipped() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("v", (byte) 1);
        NBTTagList palette = new NBTTagList();
        palette.appendTag(entry("block", "minecraft:snow_layer"));
        palette.appendTag(entry("fluid", WATER.getName()));
        nbt.setTag("palette", palette);
        byte[] data = new byte[SectionFluids.SIZE];
        data[0] = 1;
        data[1] = 2;
        nbt.setByteArray("data", data);

        SectionFluidsNbt.Result r = SectionFluidsNbt.read(nbt, KNOWN::get);
        assertFalse(r.droppedUnknown);
        assertNull(r.fluids.get(0));
        assertSame(WATER, r.fluids.get(1));
    }

    @Test
    void newerVersionIsPreserved() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("v", (byte) 2);
        nbt.setString("future", "data");

        SectionFluidsNbt.Result r = SectionFluidsNbt.read(nbt, KNOWN::get);
        assertNull(r.fluids);
        assertEquals(nbt, r.preservedRaw);
    }

    @Test
    void emptySectionWritesNothing() {
        NBTTagCompound nbt = new NBTTagCompound();
        SectionFluidsNbt.write(new SectionFluids(), nbt);
        assertTrue(nbt.hasNoTags());
    }

    @Test
    void missingTagReadsAsEmpty() {
        SectionFluidsNbt.Result r = SectionFluidsNbt.read(new NBTTagCompound(), KNOWN::get);
        assertNull(r.fluids);
        assertNull(r.preservedRaw);
        assertFalse(r.droppedUnknown);
    }
}
