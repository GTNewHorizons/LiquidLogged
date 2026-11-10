package com.gtnewhorizons.liquidlogged.internal.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;

import org.junit.jupiter.api.Test;

class SectionFluidsPacketTest {

    private final List<Fluid> byId = new ArrayList<>();
    private final Map<Fluid, Integer> ids = new IdentityHashMap<>();

    private Fluid fluid(String name) {
        Fluid f = new Fluid(name);
        ids.put(f, 1000 + byId.size());
        byId.add(f);
        return f;
    }

    private Fluid lookup(int id) {
        int i = id - 1000;
        return i >= 0 && i < byId.size() ? byId.get(i) : null;
    }

    private SectionFluids roundTrip(SectionFluids f) {
        ByteBuffer buf = ByteBuffer.allocate(SectionFluidsPacket.MAX_SECTION_BYTES);
        SectionFluidsPacket.write(f, buf, ids::get);
        buf.flip();
        SectionFluids read = SectionFluidsPacket.read(buf, this::lookup);
        assertEquals(0, buf.remaining());
        return read;
    }

    @Test
    void sparseAndDenseRoundTrip() {
        Fluid water = fluid("test_water");
        Fluid lava = fluid("test_lava");

        SectionFluids sparse = new SectionFluids();
        sparse.set(SectionFluids.index(0, 0, 0), water);
        sparse.set(SectionFluids.index(5, 6, 7), lava);
        sparse.set(SectionFluids.index(15, 15, 15), water);
        SectionFluids s = roundTrip(sparse);
        assertEquals(3, s.count());
        assertSame(lava, s.get(SectionFluids.index(5, 6, 7)));
        assertSame(water, s.get(SectionFluids.index(15, 15, 15)));

        SectionFluids dense = new SectionFluids();
        for (int i = 0; i < 4000; i++) {
            dense.set(i, (i & 1) == 0 ? water : lava);
        }
        SectionFluids d = roundTrip(dense);
        assertEquals(4000, d.count());
        assertSame(water, d.get(0));
        assertSame(lava, d.get(3999));
        assertNull(d.get(4000));
    }

    @Test
    void emptyIsOneByte() {
        ByteBuffer buf = ByteBuffer.allocate(16);
        SectionFluidsPacket.write(null, buf, ids::get);
        assertEquals(1, buf.position());
        SectionFluidsPacket.write(new SectionFluids(), buf, ids::get);
        assertEquals(2, buf.position());
        buf.flip();
        assertNull(SectionFluidsPacket.read(buf, this::lookup));
        assertNull(SectionFluidsPacket.read(buf, this::lookup));
    }

    @Test
    void worstCaseFitsMax() {
        SectionFluids f = new SectionFluids();
        for (int i = 0; i < SectionFluids.MAX_PALETTE; i++) {
            Fluid fl = fluid("test_" + i);
            ids.put(fl, Integer.MAX_VALUE - i);
            f.set(i, fl);
        }
        Fluid last = byId.get(0);
        for (int i = SectionFluids.MAX_PALETTE; i < SectionFluids.SIZE; i++) {
            f.set(i, last);
        }
        ByteBuffer buf = ByteBuffer.allocate(SectionFluidsPacket.MAX_SECTION_BYTES);
        SectionFluidsPacket.write(f, buf, ids::get);
        assertTrue(buf.position() <= SectionFluidsPacket.MAX_SECTION_BYTES);
    }

    @Test
    void unknownIdBecomesEmpty() {
        Fluid water = fluid("test_water");
        Fluid ghost = new Fluid("test_ghost");
        ids.put(ghost, 5);
        SectionFluids f = new SectionFluids();
        f.set(0, water);
        f.set(1, ghost);
        SectionFluids read = roundTrip(f);
        assertSame(water, read.get(0));
        assertNull(read.get(1));
        assertEquals(1, read.count());
    }
}
