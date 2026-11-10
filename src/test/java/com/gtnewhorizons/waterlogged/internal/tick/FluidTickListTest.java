package com.gtnewhorizons.waterlogged.internal.tick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

class FluidTickListTest {

    @Test
    void pollsInDueOrder() {
        FluidTickList list = new FluidTickList();
        list.schedule(1, 64, 1, 30, 0);
        list.schedule(2, 64, 2, 10, 0);
        list.schedule(3, 64, 3, 20, 0);
        List<FluidTick> due = list.pollDue(100, 10);
        assertEquals(3, due.size());
        assertEquals(2, due.get(0).x);
        assertEquals(3, due.get(1).x);
        assertEquals(1, due.get(2).x);
        assertEquals(0, list.size());
    }

    @Test
    void notYetDueStays() {
        FluidTickList list = new FluidTickList();
        list.schedule(0, 64, 0, 50, 0);
        assertTrue(
            list.pollDue(49, 10)
                .isEmpty());
        assertEquals(
            1,
            list.pollDue(50, 10)
                .size());
    }

    @Test
    void duplicateKeepsEarliest() {
        FluidTickList list = new FluidTickList();
        list.schedule(5, 70, 5, 40, 0);
        list.schedule(5, 70, 5, 20, 0);
        list.schedule(5, 70, 5, 60, 0);
        assertEquals(1, list.size());
        assertTrue(
            list.pollDue(19, 10)
                .isEmpty());
        assertEquals(
            1,
            list.pollDue(20, 10)
                .size());
    }

    @Test
    void limitRespected() {
        FluidTickList list = new FluidTickList();
        for (int i = 0; i < 5; i++) {
            list.schedule(i, 64, 0, 1, 0);
        }
        assertEquals(
            2,
            list.pollDue(10, 2)
                .size());
        assertEquals(3, list.size());
    }

    @Test
    void extractChunkOnlyThatChunk() {
        FluidTickList list = new FluidTickList();
        list.schedule(3, 64, 3, 5, 0);
        list.schedule(-1, 64, 3, 5, 0);
        list.schedule(17, 64, 3, 5, 0);
        List<FluidTick> chunk00 = list.extractChunk(0, 0, true);
        assertEquals(1, chunk00.size());
        assertEquals(3, chunk00.get(0).x);
        assertEquals(2, list.size());
        assertEquals(
            1,
            list.extractChunk(-1, 0, false)
                .size());
        assertEquals(2, list.size());
    }

    @Test
    void ticksSurviveChunkRoundTrip() {
        FluidTickList saved = new FluidTickList();
        saved.schedule(-30, 70, 18, 1005, 1);
        saved.schedule(-20, 71, 30, 1010, 0);
        NBTTagCompound nbt = new NBTTagCompound();
        FluidTickNbt.write(saved.extractChunk(-2, 1, true), 1000, nbt);

        FluidTickList loaded = new FluidTickList();
        FluidTickNbt.read(nbt, -2, 1, 5000, loaded);
        assertTrue(
            loaded.pollDue(5004, 10)
                .isEmpty());
        List<FluidTick> due = loaded.pollDue(5010, 10);
        assertEquals(2, due.size());
        assertEquals(-30, due.get(0).x);
        assertEquals(70, due.get(0).y);
        assertEquals(18, due.get(0).z);
        assertEquals(1, due.get(0).priority);
        assertEquals(-20, due.get(1).x);
    }
}
