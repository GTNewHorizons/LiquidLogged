package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.gtnewhorizons.waterlogged.internal.client.RenderHooks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(BlockFluidClassic.class)
public abstract class MixinBlockFluidClassic {

    // Forge classic fluids read neighbour quanta here for heights and flow; a cell holding the fluid is a full source
    @WrapOperation(
        method = "getQuantaValue",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/IBlockAccess;getBlock(III)Lnet/minecraft/block/Block;",
            remap = true))
    private Block waterlogged$fluidAsBlock(IBlockAccess world, int x, int y, int z, Operation<Block> original) {
        return RenderHooks.blockOrFluid(world, x, y, z, original.call(world, x, y, z));
    }

    @WrapOperation(
        method = "getQuantaValue",
        remap = false,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/IBlockAccess;getBlockMetadata(III)I", remap = true))
    private int waterlogged$fluidAsSource(IBlockAccess world, int x, int y, int z, Operation<Integer> original) {
        return RenderHooks.metaOrSource(world, x, y, z, original.call(world, x, y, z));
    }
}
