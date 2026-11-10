package com.gtnewhorizons.liquidlogged.internal.light;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;

import com.gtnewhorizons.liquidlogged.mixins.early.minecraft.AccessorChunk;

/** A cell's opacity and emitted light are the stronger of its block and its stored fluid. */
public final class LightHooks {

    private LightHooks() {}

    public static int fluidOpacity(@Nullable Fluid fluid) {
        Block block = fluid == null ? null : fluid.getBlock();
        return block == null ? 0 : block.getLightOpacity();
    }

    public static int fluidLight(@Nullable Fluid fluid) {
        Block block = fluid == null ? null : fluid.getBlock();
        return block == null ? 0 : block.getLightValue();
    }

    /** Updates height map and light after the stored fluid of a cell changed; chunk-local x/z. */
    public static void onFluidChanged(World world, Chunk chunk, int x, int y, int z, @Nullable Fluid oldFluid,
        @Nullable Fluid newFluid) {
        if (fluidOpacity(oldFluid) == fluidOpacity(newFluid) && fluidLight(oldFluid) == fluidLight(newFluid)) {
            return;
        }
        // Same steps vanilla Chunk.func_150807_a takes when a block's opacity changes
        int height = chunk.heightMap[z << 4 | x];
        int opacity = chunk.func_150808_b(x, y, z);
        AccessorChunk access = (AccessorChunk) chunk;
        if (opacity > 0) {
            if (y >= height) {
                access.liquidlogged$relightBlock(x, y + 1, z);
            }
        } else if (y == height - 1) {
            access.liquidlogged$relightBlock(x, y, z);
        }
        access.liquidlogged$propagateSkylightOcclusion(x, z);
        world.func_147451_t(chunk.xPosition << 4 | x, y, chunk.zPosition << 4 | z);
    }
}
