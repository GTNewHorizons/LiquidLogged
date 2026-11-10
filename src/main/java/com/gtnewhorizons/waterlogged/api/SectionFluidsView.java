package com.gtnewhorizons.waterlogged.api;

import net.minecraftforge.fluids.Fluid;

/** Read-only fluid data of one 16x16x16 chunk section; get it from {@code WaterloggedAPI.getSection}. */
public interface SectionFluidsView {

    /** Returns true when no cell of the section holds a fluid. */
    boolean isEmpty();

    /** Copies the fluids of all 4096 cells into {@code dst}, index {@code y << 8 | z << 4 | x}, null for no fluid. */
    void copyTo(Fluid[] dst);
}
