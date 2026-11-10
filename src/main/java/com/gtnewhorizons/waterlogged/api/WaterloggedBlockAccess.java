package com.gtnewhorizons.waterlogged.api;

import javax.annotation.Nullable;

import net.minecraftforge.fluids.Fluid;

/** An {@code IBlockAccess} that knows the fluid stored in each block; World and ChunkCache implement it. */
public interface WaterloggedBlockAccess {

    /** Returns the fluid stored in the block at world coordinates, or null. */
    @Nullable
    Fluid waterlogged$getFluid(int x, int y, int z);
}
