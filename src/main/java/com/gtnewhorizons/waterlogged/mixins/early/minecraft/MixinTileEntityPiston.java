package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.waterlogged.internal.fill.PistonHooks;

@Mixin(TileEntityPiston.class)
public abstract class MixinTileEntityPiston extends TileEntity implements PistonHooks.MovingFluid {

    @Unique
    private static final String WATERLOGGED_TAG = "waterlogged:fluid";

    @Unique
    private Fluid waterlogged$fluid;

    @Override
    public @Nullable Fluid waterlogged$getFluid() {
        return waterlogged$fluid;
    }

    @Override
    public void waterlogged$setFluid(@Nullable Fluid fluid) {
        waterlogged$fluid = fluid;
    }

    @Inject(
        method = { "clearPistonTileEntity", "updateEntity" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;II)Z",
            shift = At.Shift.AFTER))
    private void waterlogged$releaseFluid(CallbackInfo ci) {
        PistonHooks.release(worldObj, xCoord, yCoord, zCoord, (TileEntityPiston) (Object) this);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void waterlogged$readFluid(NBTTagCompound nbt, CallbackInfo ci) {
        waterlogged$fluid = nbt.hasKey(WATERLOGGED_TAG) ? FluidRegistry.getFluid(nbt.getString(WATERLOGGED_TAG)) : null;
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void waterlogged$writeFluid(NBTTagCompound nbt, CallbackInfo ci) {
        if (waterlogged$fluid != null) {
            nbt.setString(WATERLOGGED_TAG, waterlogged$fluid.getName());
        }
    }
}
