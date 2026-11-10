package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;

import com.gtnewhorizons.waterlogged.internal.light.LightHooks;
import com.gtnewhorizons.waterlogged.internal.storage.ChunkFluids;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

@Mixin(Chunk.class)
public abstract class MixinChunk_Light {

    // Height map, sky light and World.getBlockLightOpacity all read opacity through this method
    @ModifyReturnValue(method = "func_150808_b", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private int waterlogged$withStoredFluid(int opacity, int x, int y, int z) {
        return Math.max(opacity, LightHooks.fluidOpacity(ChunkFluids.get((Chunk) (Object) this, x, y, z)));
    }
}
