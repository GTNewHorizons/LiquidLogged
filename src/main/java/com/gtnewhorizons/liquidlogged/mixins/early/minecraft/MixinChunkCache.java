package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import javax.annotation.Nullable;

import net.minecraft.world.ChunkCache;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.gtnewhorizons.liquidlogged.api.LiquidLoggedBlockAccess;
import com.gtnewhorizons.liquidlogged.internal.storage.ChunkFluids;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache implements LiquidLoggedBlockAccess {

    @Shadow
    private int chunkX;

    @Shadow
    private int chunkZ;

    @Shadow
    private Chunk[][] chunkArray;

    @Override
    public @Nullable Fluid liquidlogged$getFluid(int x, int y, int z) {
        if (y < 0 || y > 255) {
            return null;
        }
        int cx = (x >> 4) - chunkX;
        int cz = (z >> 4) - chunkZ;
        if (cx < 0 || cx >= chunkArray.length || cz < 0 || cz >= chunkArray[cx].length) {
            return null;
        }
        Chunk chunk = chunkArray[cx][cz];
        return chunk == null ? null : ChunkFluids.get(chunk, x & 15, y, z & 15);
    }
}
