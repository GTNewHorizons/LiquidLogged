package com.gtnewhorizons.liquidlogged.internal.tick;

import java.util.Comparator;

/** One scheduled tick of a fluid stored in a block. */
public final class FluidTick {

    static final Comparator<FluidTick> ORDER = Comparator.comparingLong((FluidTick t) -> t.due)
        .thenComparingInt(t -> t.priority)
        .thenComparingLong(t -> t.seq);

    public final int x;
    public final int y;
    public final int z;
    public final long due;
    public final int priority;
    final long seq;

    FluidTick(int x, int y, int z, long due, int priority, long seq) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.due = due;
        this.priority = priority;
        this.seq = seq;
    }
}
