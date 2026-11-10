package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Vanilla height map and sky light helpers, called when only a cell's stored fluid changes. */
@Mixin(Chunk.class)
public interface AccessorChunk {

    @Invoker("relightBlock")
    void liquidlogged$relightBlock(int x, int y, int z);

    @Invoker("propagateSkylightOcclusion")
    void liquidlogged$propagateSkylightOcclusion(int x, int z);
}
