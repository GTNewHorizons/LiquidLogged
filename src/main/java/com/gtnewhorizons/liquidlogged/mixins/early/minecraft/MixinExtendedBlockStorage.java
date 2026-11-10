package com.gtnewhorizons.liquidlogged.mixins.early.minecraft;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.liquidlogged.internal.storage.LiquidLoggedSection;
import com.gtnewhorizons.liquidlogged.internal.storage.SectionFluids;

@Mixin(ExtendedBlockStorage.class)
public abstract class MixinExtendedBlockStorage implements LiquidLoggedSection {

    @Unique
    private SectionFluids liquidlogged$fluids;

    @Override
    public @Nullable SectionFluids liquidlogged$get() {
        return liquidlogged$fluids;
    }

    @Override
    public void liquidlogged$set(@Nullable SectionFluids fluids) {
        liquidlogged$fluids = fluids;
    }

    @Unique
    private NBTTagCompound liquidlogged$preserved;

    @Override
    public @Nullable NBTTagCompound liquidlogged$getPreserved() {
        return liquidlogged$preserved;
    }

    @Override
    public void liquidlogged$setPreserved(@Nullable NBTTagCompound tag) {
        liquidlogged$preserved = tag;
    }
}
