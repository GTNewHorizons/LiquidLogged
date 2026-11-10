package com.gtnewhorizons.waterlogged.mixins.early.minecraft.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.world.ChunkCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    // The render loop reads each cell with chunkcache.getBlock and then asks that block about passes;
    // remembering the cell's stored fluid here lets the later wraps see it without fragile local captures
    @Unique
    private Block waterlogged$fluidBlock;

    @Unique
    private int waterlogged$pass;

    @WrapOperation(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ChunkCache;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block waterlogged$rememberCell(ChunkCache cache, int x, int y, int z, Operation<Block> original) {
        waterlogged$fluidBlock = RenderHooks.storedFluidBlock(cache, x, y, z);
        return original.call(cache, x, y, z);
    }

    @WrapOperation(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRenderBlockPass()I"))
    private int waterlogged$requestFluidPass(Block block, Operation<Integer> original) {
        int pass = original.call(block);
        return waterlogged$fluidBlock == null ? pass : Math.max(pass, waterlogged$fluidBlock.getRenderBlockPass());
    }

    @WrapOperation(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;canRenderInPass(I)Z", remap = false))
    private boolean waterlogged$renderInFluidPass(Block block, int pass, Operation<Boolean> original) {
        waterlogged$pass = pass;
        return original.call(block, pass)
            || (waterlogged$fluidBlock != null && waterlogged$fluidBlock.canRenderInPass(pass));
    }

    @WrapOperation(
        method = "updateRenderer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderBlocks;renderBlockByRenderType(Lnet/minecraft/block/Block;III)Z"))
    private boolean waterlogged$renderStoredFluid(RenderBlocks renderBlocks, Block block, int x, int y, int z,
        Operation<Boolean> original) {
        Block fluidBlock = waterlogged$fluidBlock;
        if (fluidBlock == null) {
            return original.call(renderBlocks, block, x, y, z);
        }
        boolean drawn = false;
        if (block.canRenderInPass(waterlogged$pass)) {
            drawn = original.call(renderBlocks, block, x, y, z);
        }
        if (!renderBlocks.renderFromInside && fluidBlock.canRenderInPass(waterlogged$pass)) {
            RenderHooks.setRenderingStored(true);
            try {
                drawn |= original.call(renderBlocks, fluidBlock, x, y, z);
            } finally {
                RenderHooks.setRenderingStored(false);
            }
        }
        return drawn;
    }
}
