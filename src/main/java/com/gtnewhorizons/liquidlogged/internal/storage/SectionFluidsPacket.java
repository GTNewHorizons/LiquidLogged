package com.gtnewhorizons.liquidlogged.internal.storage;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;

/** Network format of one section's fluids in chunk packets: a palette of fluid ids, then dense or sparse cells. */
public final class SectionFluidsPacket {

    private static final int MAX_VARINT = 5;
    private static final byte MODE_DENSE = 0;
    private static final byte MODE_SPARSE = 1;
    private static final byte HAS_LEVELS = 2;

    /** Upper bound of bytes one section takes in a chunk packet. */
    public static final int MAX_SECTION_BYTES = 1 + 2
        + SectionFluids.MAX_PALETTE * MAX_VARINT
        + 1
        + 2 * SectionFluids.SIZE;

    private SectionFluidsPacket() {}

    /** Writes the section; {@code idOf} maps a fluid to its network id. */
    public static void write(@Nullable SectionFluids fluids, ByteBuffer out, ToIntFunction<Fluid> idOf) {
        if (fluids == null || fluids.isEmpty()) {
            out.put((byte) 0);
            return;
        }
        out.put((byte) 1);
        SectionFluids c = fluids.compacted();
        Fluid[] palette = c.palette();
        writeVarInt(out, palette.length);
        for (Fluid fluid : palette) {
            writeVarInt(out, idOf.applyAsInt(fluid));
        }
        byte[] raw = c.raw();
        int count = c.count();
        byte[] levels = c.levels();
        int flags = levels == null ? 0 : HAS_LEVELS;
        int sparseSize = varIntSize(count) + count * (levels == null ? 3 : 4);
        if (sparseSize < SectionFluids.SIZE * (levels == null ? 1 : 2)) {
            out.put((byte) (MODE_SPARSE | flags));
            writeVarInt(out, count);
            for (int i = 0; i < SectionFluids.SIZE; i++) {
                if (raw[i] != 0) {
                    out.putShort((short) i);
                    out.put(raw[i]);
                    if (levels != null) {
                        out.put(levels[i]);
                    }
                }
            }
        } else {
            out.put((byte) (MODE_DENSE | flags));
            out.put(raw);
            if (levels != null) {
                out.put(levels);
            }
        }
    }

    /** Reads a section written by {@link #write}; {@code byId} returns null for ids this side does not know. */
    public static @Nullable SectionFluids read(ByteBuffer in, IntFunction<Fluid> byId) {
        if (in.get() == 0) {
            return null;
        }
        Fluid[] palette = new Fluid[readVarInt(in)];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = byId.apply(readVarInt(in));
        }
        byte[] raw = new byte[SectionFluids.SIZE];
        int flags = in.get();
        byte[] levels = (flags & HAS_LEVELS) == 0 ? null : new byte[SectionFluids.SIZE];
        if ((flags & MODE_SPARSE) != 0) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                int index = in.getShort() & 0xFFFF;
                raw[index] = in.get();
                if (levels != null) {
                    levels[index] = in.get();
                }
            }
        } else {
            in.get(raw);
            if (levels != null) {
                in.get(levels);
            }
        }
        SectionFluids fluids = SectionFluids.of(palette, raw, levels);
        return fluids.isEmpty() ? null : fluids;
    }

    static void writeVarInt(ByteBuffer out, int value) {
        while ((value & ~0x7F) != 0) {
            out.put((byte) ((value & 0x7F) | 0x80));
            value >>>= 7;
        }
        out.put((byte) value);
    }

    static int readVarInt(ByteBuffer in) {
        int value = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            byte b = in.get();
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return value;
            }
        }
        throw new IllegalStateException("VarInt too long");
    }

    private static int varIntSize(int value) {
        int size = 1;
        while ((value & ~0x7F) != 0) {
            value >>>= 7;
            size++;
        }
        return size;
    }
}
