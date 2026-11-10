package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

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

import com.gtnewhorizons.liquidlogged.internal.fill.PistonHooks;

@Mixin(TileEntityPiston.class)
public abstract class MixinTileEntityPiston extends TileEntity implements PistonHooks.MovingFluid {

    @Unique
    private static final String LIQUIDLOGGED_TAG = "liquidlogged:fluid";

    @Unique
    private Fluid liquidlogged$fluid;

    @Override
    public @Nullable Fluid liquidlogged$getFluid() {
        return liquidlogged$fluid;
    }

    @Override
    public void liquidlogged$setFluid(@Nullable Fluid fluid) {
        liquidlogged$fluid = fluid;
    }

    @Inject(
        method = { "clearPistonTileEntity", "updateEntity" },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlock(IIILnet/minecraft/block/Block;II)Z",
            shift = At.Shift.AFTER))
    private void liquidlogged$releaseFluid(CallbackInfo ci) {
        PistonHooks.release(worldObj, xCoord, yCoord, zCoord, (TileEntityPiston) (Object) this);
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void liquidlogged$readFluid(NBTTagCompound nbt, CallbackInfo ci) {
        liquidlogged$fluid = nbt.hasKey(LIQUIDLOGGED_TAG) ? FluidRegistry.getFluid(nbt.getString(LIQUIDLOGGED_TAG))
            : null;
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void liquidlogged$writeFluid(NBTTagCompound nbt, CallbackInfo ci) {
        if (liquidlogged$fluid != null) {
            nbt.setString(LIQUIDLOGGED_TAG, liquidlogged$fluid.getName());
        }
    }
}
