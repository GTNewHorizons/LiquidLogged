package com.gtnewhorizons.liquidlogged.internal.storage;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;

/** Reads and writes the fluids of one section in the format of design §4.2. */
public final class SectionFluidsNbt {

    public static final byte VERSION = 1;
    public static final String TYPE_FLUID = "fluid";

    private SectionFluidsNbt() {}

    /** Result of {@link #read}: the fluids, whether unknown fluid names were dropped, and a tag kept as is. */
    public static final class Result {

        public final @Nullable SectionFluids fluids;
        public final boolean droppedUnknown;
        public final @Nullable NBTTagCompound preservedRaw;

        Result(@Nullable SectionFluids fluids, boolean droppedUnknown, @Nullable NBTTagCompound preservedRaw) {
            this.fluids = fluids;
            this.droppedUnknown = droppedUnknown;
            this.preservedRaw = preservedRaw;
        }
    }

    /** Writes the section into {@code out}; writes nothing for an empty or null section. */
    public static void write(@Nullable SectionFluids fluids, NBTTagCompound out) {
        if (fluids == null || fluids.isEmpty()) {
            return;
        }
        SectionFluids c = fluids.compacted();
        NBTTagList palette = new NBTTagList();
        for (Fluid fluid : c.palette()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("t", TYPE_FLUID);
            entry.setString("n", fluid.getName());
            palette.appendTag(entry);
        }
        out.setByte("v", VERSION);
        out.setTag("palette", palette);
        out.setByteArray("data", c.raw());
    }

    /** Reads a section written by {@link #write}; {@code byName} returns null for fluids that no longer exist. */
    public static Result read(@Nullable NBTTagCompound in, Function<String, Fluid> byName) {
        if (in == null || !in.hasKey("v")) {
            return new Result(null, false, null);
        }
        byte version = in.getByte("v");
        if (version != VERSION) {
            // Written by a newer mod version: keep the tag untouched so saving does not erase it
            return new Result(null, false, (NBTTagCompound) in.copy());
        }
        NBTTagList list = in.getTagList("palette", Constants.NBT.TAG_COMPOUND);
        Fluid[] palette = new Fluid[list.tagCount()];
        boolean dropped = false;
        for (int i = 0; i < palette.length; i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            if (!TYPE_FLUID.equals(entry.getString("t"))) {
                continue;
            }
            palette[i] = byName.apply(entry.getString("n"));
            if (palette[i] == null) {
                dropped = true;
            }
        }
        byte[] data = in.getByteArray("data");
        if (data.length != SectionFluids.SIZE) {
            return new Result(null, dropped, null);
        }
        SectionFluids fluids = SectionFluids.of(palette, data);
        return new Result(fluids.isEmpty() ? null : fluids, dropped, null);
    }
}
