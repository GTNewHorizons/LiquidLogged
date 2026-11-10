package com.gtnewhorizons.waterlogged.internal.storage;

import javax.annotation.Nullable;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fluids.Fluid;

/** Reads and writes the fluid of a cell by chunk-local coordinates (x and z 0..15, y 0..255). */
public final class ChunkFluids {

    private ChunkFluids() {}

    public static @Nullable Fluid get(Chunk chunk, int x, int y, int z) {
        ExtendedBlockStorage ebs = section(chunk, y);
        if (ebs == null) {
            return null;
        }
        SectionFluids fluids = ((WaterloggedSection) ebs).waterlogged$get();
        return fluids == null ? null : fluids.get(SectionFluids.index(x, y & 15, z));
    }

    /** Sets the cell's fluid; returns false when the section does not exist or its palette is full. */
    public static boolean set(Chunk chunk, int x, int y, int z, @Nullable Fluid fluid) {
        ExtendedBlockStorage ebs = section(chunk, y);
        if (ebs == null) {
            return fluid == null;
        }
        WaterloggedSection section = (WaterloggedSection) ebs;
        SectionFluids fluids = section.waterlogged$get();
        if (fluids == null) {
            if (fluid == null) {
                return true;
            }
            fluids = new SectionFluids();
            section.waterlogged$set(fluids);
        }
        if (!fluids.set(SectionFluids.index(x, y & 15, z), fluid)) {
            return false;
        }
        if (fluids.isEmpty()) {
            section.waterlogged$set(null);
        }
        chunk.isModified = true;
        return true;
    }

    private static @Nullable ExtendedBlockStorage section(Chunk chunk, int y) {
        if (y < 0 || y > 255) {
            return null;
        }
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        int i = y >> 4;
        return i < sections.length ? sections[i] : null;
    }
}
