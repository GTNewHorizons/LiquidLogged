package com.gtnewhorizons.liquidlogged.internal.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraftforge.fluids.Fluid;

import org.junit.jupiter.api.Test;

class SectionFluidsTest {

    private static final Fluid A = new Fluid("test_a");
    private static final Fluid B = new Fluid("test_b");

    private static Fluid[] distinctFluids(int n) {
        Fluid[] fluids = new Fluid[n];
        for (int i = 0; i < n; i++) {
            fluids[i] = new Fluid("test_" + i);
        }
        return fluids;
    }

    @Test
    void setAndGetSameCell() {
        SectionFluids f = new SectionFluids();
        assertTrue(f.set(SectionFluids.index(3, 5, 7), A));
        assertSame(A, f.get(SectionFluids.index(3, 5, 7)));
        assertEquals(1, f.count());
    }

    @Test
    void indexOrder() {
        assertEquals(1, SectionFluids.index(1, 0, 0));
        assertEquals(16, SectionFluids.index(0, 0, 1));
        assertEquals(256, SectionFluids.index(0, 1, 0));
    }

    @Test
    void clearLastCellEmpties() {
        SectionFluids f = new SectionFluids();
        int i = SectionFluids.index(0, 0, 0);
        f.set(i, A);
        f.set(i, null);
        assertTrue(f.isEmpty());
        assertNull(f.get(i));
    }

    @Test
    void overwriteKeepsCount() {
        SectionFluids f = new SectionFluids();
        int i = SectionFluids.index(2, 2, 2);
        f.set(i, A);
        f.set(i, B);
        assertSame(B, f.get(i));
        assertEquals(1, f.count());
    }

    @Test
    void rejects256thFluid() {
        SectionFluids f = new SectionFluids();
        Fluid[] fluids = distinctFluids(256);
        for (int i = 0; i < 255; i++) {
            assertTrue(f.set(i, fluids[i]));
        }
        assertFalse(f.set(255, fluids[255]));
        assertNull(f.get(255));
        assertEquals(255, f.count());
    }

    @Test
    void paletteSlotReused() {
        SectionFluids f = new SectionFluids();
        Fluid[] fluids = distinctFluids(256);
        for (int i = 0; i < 255; i++) {
            f.set(i, fluids[i]);
        }
        f.set(0, null);
        assertTrue(f.set(255, fluids[255]));
        assertSame(fluids[255], f.get(255));
        assertEquals(255, f.count());
    }

    @Test
    void copyIsDeep() {
        SectionFluids f = new SectionFluids();
        f.set(0, A);
        SectionFluids copy = f.copy();
        copy.set(0, B);
        copy.set(1, B);
        assertSame(A, f.get(0));
        assertNull(f.get(1));
        assertEquals(1, f.count());
    }

    @Test
    void compactedHasNoHoles() {
        SectionFluids f = new SectionFluids();
        f.set(0, A);
        f.set(1, B);
        f.set(0, null);
        SectionFluids c = f.compacted();
        assertEquals(1, c.palette().length);
        assertSame(B, c.palette()[0]);
        assertSame(B, c.get(1));
    }

    @Test
    void ofClearsCellsWithNullPaletteEntry() {
        byte[] raw = new byte[SectionFluids.SIZE];
        raw[0] = 1;
        raw[1] = 2;
        raw[2] = 3;
        SectionFluids f = SectionFluids.of(new Fluid[] { A, null }, raw);
        assertSame(A, f.get(0));
        assertNull(f.get(1));
        assertNull(f.get(2));
        assertEquals(1, f.count());
    }

    @Test
    void copyToFillsAllCells() {
        SectionFluids f = new SectionFluids();
        f.set(SectionFluids.index(15, 15, 15), A);
        Fluid[] dst = new Fluid[SectionFluids.SIZE];
        dst[0] = B;
        f.copyTo(dst);
        assertNull(dst[0]);
        assertSame(A, dst[SectionFluids.SIZE - 1]);
    }
}
