package com.gtnewhorizons.waterlogged.internal.tick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Pending ticks of stored fluids in one world, at most one per position. */
public final class FluidTickList {

    // Synchronized: Forge reads chunk NBT, and so our ticks, on its chunk IO thread
    private final TreeSet<FluidTick> queue = new TreeSet<>(FluidTick.ORDER);
    private final Map<Long, FluidTick> byPos = new HashMap<>();
    // Ticks read from disk wait here until their chunk is in the world; polling them earlier would drop them
    private final Map<Long, List<FluidTick>> staged = new HashMap<>();
    private long seq;

    /** Keeps a tick read from chunk NBT aside until {@link #commitChunk} for its chunk. */
    public synchronized void stage(int x, int y, int z, long due, int priority) {
        staged.computeIfAbsent(chunkKey(x >> 4, z >> 4), k -> new ArrayList<>())
            .add(new FluidTick(x, y, z, due, priority, 0));
    }

    /** Schedules the staged ticks of chunk (cx, cz); call once the chunk is loaded into the world. */
    public synchronized void commitChunk(int cx, int cz) {
        List<FluidTick> ticks = staged.remove(chunkKey(cx, cz));
        if (ticks != null) {
            for (FluidTick t : ticks) {
                schedule(t.x, t.y, t.z, t.due, t.priority);
            }
        }
    }

    private static long chunkKey(int cx, int cz) {
        return (long) cx << 32 | (cz & 0xFFFFFFFFL);
    }

    /** Schedules a tick; if one is already pending at this position, the earlier of the two is kept. */
    public synchronized void schedule(int x, int y, int z, long due, int priority) {
        long key = key(x, y, z);
        FluidTick old = byPos.get(key);
        if (old != null) {
            if (old.due <= due) {
                return;
            }
            queue.remove(old);
        }
        FluidTick tick = new FluidTick(x, y, z, due, priority, seq++);
        queue.add(tick);
        byPos.put(key, tick);
    }

    /** Removes and returns up to {@code limit} ticks due at or before {@code now}, earliest first. */
    public synchronized List<FluidTick> pollDue(long now, int limit) {
        List<FluidTick> due = new ArrayList<>();
        while (due.size() < limit && !queue.isEmpty() && queue.first().due <= now) {
            FluidTick tick = queue.pollFirst();
            byPos.remove(key(tick.x, tick.y, tick.z));
            due.add(tick);
        }
        return due;
    }

    /** Returns the ticks inside chunk (cx, cz), removing them when {@code remove} is true. */
    public synchronized List<FluidTick> extractChunk(int cx, int cz, boolean remove) {
        List<FluidTick> found = new ArrayList<>();
        Iterator<FluidTick> it = byPos.values()
            .iterator();
        while (it.hasNext()) {
            FluidTick tick = it.next();
            if (tick.x >> 4 == cx && tick.z >> 4 == cz) {
                found.add(tick);
                if (remove) {
                    it.remove();
                    queue.remove(tick);
                }
            }
        }
        found.sort(FluidTick.ORDER);
        return found;
    }

    public synchronized int size() {
        return queue.size();
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | (y & 0xFFFL);
    }
}
