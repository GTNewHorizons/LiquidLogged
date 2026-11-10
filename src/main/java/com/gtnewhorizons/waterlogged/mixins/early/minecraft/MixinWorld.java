package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.waterlogged.api.WaterloggedBlockAccess;
import com.gtnewhorizons.waterlogged.internal.WorldFluids;
import com.gtnewhorizons.waterlogged.internal.rules.BlockChangeRules;
import com.gtnewhorizons.waterlogged.internal.storage.ChunkFluids;
import com.gtnewhorizons.waterlogged.internal.tick.WorldFluidTicks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(World.class)
public abstract class MixinWorld implements WaterloggedBlockAccess {

    @Shadow
    public boolean isRemote;

    @Shadow
    public abstract boolean blockExists(int x, int y, int z);

    @Shadow
    public abstract Chunk getChunkFromBlockCoords(int x, int z);

    @Shadow
    public abstract Block getBlock(int x, int y, int z);

    @Shadow
    public abstract int getBlockMetadata(int x, int y, int z);

    @Shadow
    public abstract boolean setBlock(int x, int y, int z, Block block, int meta, int flags);

    @Override
    public @Nullable Fluid waterlogged$getFluid(int x, int y, int z) {
        if (y < 0 || y > 255 || !blockExists(x, y, z)) {
            return null;
        }
        return ChunkFluids.get(getChunkFromBlockCoords(x, z), x & 15, y, z & 15);
    }

    @Inject(method = "notifyBlockOfNeighborChange", at = @At("HEAD"))
    private void waterlogged$scheduleOnNeighborChange(int x, int y, int z, Block neighbor, CallbackInfo ci) {
        Fluid fluid = isRemote ? null : waterlogged$getFluid(x, y, z);
        if (fluid != null) {
            WorldFluidTicks.schedule((World) (Object) this, x, y, z, fluid);
        }
    }

    @WrapMethod(method = "setBlock(IIILnet/minecraft/block/Block;II)Z")
    private boolean waterlogged$applyBlockChangeRules(int x, int y, int z, Block block, int meta, int flags,
        Operation<Boolean> original) {
        // Clients get fluids only from packets; applying rules there would race the server
        Fluid fluid = isRemote ? null : waterlogged$getFluid(x, y, z);
        if (fluid == null) {
            return original.call(x, y, z, block, meta, flags);
        }
        Block oldBlock = getBlock(x, y, z);
        int oldMeta = getBlockMetadata(x, y, z);
        if (!original.call(x, y, z, block, meta, flags)) {
            return false;
        }
        World world = (World) (Object) this;
        switch (BlockChangeRules.onChange(oldBlock, oldMeta, block, meta)) {
            case KEEP:
                break;
            case CLEAR:
                // Vanilla already relit and notified neighbours while the fluid was still stored; redo both without it
                WorldFluids.setUnchecked(world, x, y, z, null);
                break;
            case LEAVE_SOURCE:
                WorldFluids.setUnchecked(world, x, y, z, null);
                Block fluidBlock = BlockChangeRules.sourceBlock(fluid);
                if (fluidBlock != null) {
                    setBlock(x, y, z, fluidBlock, 0, 3);
                }
                break;
        }
        return true;
    }
}
