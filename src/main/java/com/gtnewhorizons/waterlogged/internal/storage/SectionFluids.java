package com.gtnewhorizons.waterlogged.internal.storage;

import java.util.Arrays;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.waterlogged.api.SectionFluidsView;

/** Fluids stored in the cells of one chunk section: a palette of up to 255 fluids and one byte per cell. */
public final class SectionFluids implements SectionFluidsView {

    public static final int SIZE = 16 * 16 * 16;
    public static final int MAX_PALETTE = 255;

    // raw[i] == 0 means no fluid, raw[i] == k means palette[k - 1]
    private final byte[] raw;
    private Fluid[] palette;
    private int[] refs;
    private int count;

    public SectionFluids() {
        this(new byte[SIZE], new Fluid[4], new int[4], 0);
    }

    private SectionFluids(byte[] raw, Fluid[] palette, int[] refs, int count) {
        this.raw = raw;
        this.palette = palette;
        this.refs = refs;
        this.count = count;
    }

    /** Returns the cell index for section-local coordinates 0..15. */
    public static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    /** Builds a section from a palette and raw cell values; cells pointing to a null or missing entry stay empty. */
    public static SectionFluids of(@Nullable Fluid[] palette, byte[] raw) {
        SectionFluids f = new SectionFluids();
        for (int i = 0; i < SIZE; i++) {
            int v = raw[i] & 0xFF;
            if (v != 0 && v <= palette.length && palette[v - 1] != null) {
                f.set(i, palette[v - 1]);
            }
        }
        return f;
    }

    public @Nullable Fluid get(int index) {
        int v = raw[index] & 0xFF;
        return v == 0 ? null : palette[v - 1];
    }

    /** Sets the fluid of a cell; returns false and changes nothing when the palette already holds 255 other fluids. */
    public boolean set(int index, @Nullable Fluid fluid) {
        int old = raw[index] & 0xFF;
        if (fluid == null) {
            if (old != 0) {
                release(old - 1);
                raw[index] = 0;
                count--;
            }
            return true;
        }
        int slot = slotOf(fluid);
        if (slot < 0) {
            slot = allocate(fluid);
            if (slot < 0) {
                return false;
            }
        }
        if (old == slot + 1) {
            return true;
        }
        if (old != 0) {
            release(old - 1);
        } else {
            count++;
        }
        refs[slot]++;
        raw[index] = (byte) (slot + 1);
        return true;
    }

    public int count() {
        return count;
    }

    @Override
    public boolean isEmpty() {
        return count == 0;
    }

    @Override
    public void copyTo(Fluid[] dst) {
        for (int i = 0; i < SIZE; i++) {
            dst[i] = get(i);
        }
    }

    public SectionFluids copy() {
        return new SectionFluids(raw.clone(), palette.clone(), refs.clone(), count);
    }

    /** Returns a copy whose palette has no unused entries, so raw value k always means palette()[k - 1]. */
    public SectionFluids compacted() {
        int used = 0;
        int[] remap = new int[palette.length];
        for (int s = 0; s < palette.length; s++) {
            if (refs[s] > 0) {
                remap[s] = ++used;
            }
        }
        Fluid[] newPalette = new Fluid[Math.max(used, 1)];
        int[] newRefs = new int[newPalette.length];
        for (int s = 0; s < palette.length; s++) {
            if (remap[s] != 0) {
                newPalette[remap[s] - 1] = palette[s];
                newRefs[remap[s] - 1] = refs[s];
            }
        }
        byte[] newRaw = new byte[SIZE];
        for (int i = 0; i < SIZE; i++) {
            int v = raw[i] & 0xFF;
            if (v != 0) {
                newRaw[i] = (byte) remap[v - 1];
            }
        }
        return new SectionFluids(newRaw, newPalette, newRefs, count);
    }

    /** Returns the palette in use order; only meaningful on a {@link #compacted()} section. */
    public Fluid[] palette() {
        int used = 0;
        for (int r : refs) {
            if (r > 0) used++;
        }
        return Arrays.copyOf(palette, used);
    }

    /** Returns a copy of the raw cell values. */
    public byte[] raw() {
        return raw.clone();
    }

    private int slotOf(Fluid fluid) {
        for (int s = 0; s < palette.length; s++) {
            if (refs[s] > 0 && palette[s] == fluid) {
                return s;
            }
        }
        return -1;
    }

    private int allocate(Fluid fluid) {
        for (int s = 0; s < palette.length; s++) {
            if (refs[s] == 0) {
                palette[s] = fluid;
                return s;
            }
        }
        if (palette.length >= MAX_PALETTE) {
            return -1;
        }
        int s = palette.length;
        int size = Math.min(MAX_PALETTE, palette.length * 2);
        palette = Arrays.copyOf(palette, size);
        refs = Arrays.copyOf(refs, size);
        palette[s] = fluid;
        return s;
    }

    private void release(int slot) {
        if (--refs[slot] == 0) {
            palette[slot] = null;
        }
    }
}
