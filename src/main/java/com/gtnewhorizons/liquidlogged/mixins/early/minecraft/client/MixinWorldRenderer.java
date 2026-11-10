package com.gtnewhorizons.liquidlogged.mixins.early.minecraft.client;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.world.ChunkCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.liquidlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    // The render loop reads each cell with chunkcache.getBlock and then asks that block about passes;
    // remembering the cell's stored fluid here lets the later wraps see it without fragile local captures
    @Unique
    private Block liquidlogged$fluidBlock;

    @Unique
    private int liquidlogged$pass;

    @WrapOperation(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ChunkCache;getBlock(III)Lnet/minecraft/block/Block;"))
    private Block liquidlogged$rememberCell(ChunkCache cache, int x, int y, int z, Operation<Block> original) {
        liquidlogged$fluidBlock = RenderHooks.storedFluidBlock(cache, x, y, z);
        return original.call(cache, x, y, z);
    }

    @WrapOperation(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRenderBlockPass()I"))
    private int liquidlogged$requestFluidPass(Block block, Operation<Integer> original) {
        int pass = original.call(block);
        return liquidlogged$fluidBlock == null ? pass : Math.max(pass, liquidlogged$fluidBlock.getRenderBlockPass());
    }

    @WrapOperation(
        method = "updateRenderer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;canRenderInPass(I)Z", remap = false))
    private boolean liquidlogged$renderInFluidPass(Block block, int pass, Operation<Boolean> original) {
        liquidlogged$pass = pass;
        return original.call(block, pass)
            || (liquidlogged$fluidBlock != null && liquidlogged$fluidBlock.canRenderInPass(pass));
    }

    @WrapOperation(
        method = "updateRenderer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderBlocks;renderBlockByRenderType(Lnet/minecraft/block/Block;III)Z"))
    private boolean liquidlogged$renderStoredFluid(RenderBlocks renderBlocks, Block block, int x, int y, int z,
        Operation<Boolean> original) {
        Block fluidBlock = liquidlogged$fluidBlock;
        if (fluidBlock == null) {
            return original.call(renderBlocks, block, x, y, z);
        }
        boolean drawn = false;
        if (block.canRenderInPass(liquidlogged$pass)) {
            drawn = original.call(renderBlocks, block, x, y, z);
        }
        if (!renderBlocks.renderFromInside && fluidBlock.canRenderInPass(liquidlogged$pass)) {
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
