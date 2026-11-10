package com.gtnewhorizons.liquidlogged.internal.storage;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;

/** Network format of one section's fluids in chunk packets (design §5). */
public final class SectionFluidsPacket {

    private static final int MAX_VARINT = 5;
    private static final byte MODE_DENSE = 0;
    private static final byte MODE_SPARSE = 1;

    /** Upper bound of bytes one section takes in a chunk packet. */
    public static final int MAX_SECTION_BYTES = 1 + 2 + SectionFluids.MAX_PALETTE * MAX_VARINT + 1 + SectionFluids.SIZE;

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
        int sparseSize = varIntSize(count) + count * 3;
        if (sparseSize < SectionFluids.SIZE) {
            out.put(MODE_SPARSE);
            writeVarInt(out, count);
            for (int i = 0; i < SectionFluids.SIZE; i++) {
                if (raw[i] != 0) {
                    out.putShort((short) i);
                    out.put(raw[i]);
                }
            }
        } else {
            out.put(MODE_DENSE);
            out.put(raw);
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
        if (in.get() == MODE_SPARSE) {
            int count = readVarInt(in);
            for (int i = 0; i < count; i++) {
                int index = in.getShort() & 0xFFFF;
                raw[index] = in.get();
            }
        } else {
            in.get(raw);
        }
        SectionFluids fluids = SectionFluids.of(palette, raw);
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
