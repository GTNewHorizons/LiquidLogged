package com.gtnewhorizons.waterlogged.internal.storage;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

/** Added to {@code ExtendedBlockStorage} by mixin; cast a section to it to read or replace its fluids. */
public interface WaterloggedSection {

    @Nullable
    SectionFluids waterlogged$get();

    void waterlogged$set(@Nullable SectionFluids fluids);

    /** Tag of a newer format version, written back unchanged on save. */
    @Nullable
    NBTTagCompound waterlogged$getPreserved();

    void waterlogged$setPreserved(@Nullable NBTTagCompound tag);
}
