package com.gtnewhorizons.waterlogged.internal.tick;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

/** Chunk NBT of pending stored-fluid ticks (design §4.3): chunk-local x/z, delay relative to world time. */
public final class FluidTickNbt {

    public static final byte VERSION = 1;

    private FluidTickNbt() {}

    /** Writes the ticks of one chunk; writes nothing when there are none. */
    public static void write(List<FluidTick> ticks, long now, NBTTagCompound out) {
        if (ticks.isEmpty()) {
            return;
        }
        NBTTagList list = new NBTTagList();
        for (FluidTick t : ticks) {
            list.appendTag(
                new NBTTagIntArray(new int[] { t.x & 15, t.y, t.z & 15, (int) Math.max(0, t.due - now), t.priority }));
        }
        out.setByte("v", VERSION);
        out.setTag("ticks", list);
    }

    /** Reads the ticks of chunk (cx, cz) into {@code into}, due relative to {@code now}. */
    public static void read(NBTTagCompound in, int cx, int cz, long now, FluidTickList into) {
        if (in == null || in.getByte("v") != VERSION) {
            return;
        }
        NBTTagList list = in.getTagList("ticks", Constants.NBT.TAG_INT_ARRAY);
        for (int i = 0; i < list.tagCount(); i++) {
            int[] e = list.func_150306_c(i);
            if (e.length < 5) {
                continue;
            }
            into.schedule(cx << 4 | e[0], e[1], cz << 4 | e[2], now + e[3], e[4]);
        }
    }
}
