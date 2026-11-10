package com.gtnewhorizons.waterlogged.mixins.early.minecraft;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.gtnewhorizons.waterlogged.internal.storage.SectionFluids;
import com.gtnewhorizons.waterlogged.internal.storage.WaterloggedSection;

@Mixin(ExtendedBlockStorage.class)
public abstract class MixinExtendedBlockStorage implements WaterloggedSection {

    @Unique
    private SectionFluids waterlogged$fluids;

    @Override
    public @Nullable SectionFluids waterlogged$get() {
        return waterlogged$fluids;
    }

    @Override
    public void waterlogged$set(@Nullable SectionFluids fluids) {
        waterlogged$fluids = fluids;
    }

    @Unique
    private NBTTagCompound waterlogged$preserved;

    @Override
    public @Nullable NBTTagCompound waterlogged$getPreserved() {
        return waterlogged$preserved;
    }

    @Override
    public void waterlogged$setPreserved(@Nullable NBTTagCompound tag) {
        waterlogged$preserved = tag;
    }
}
