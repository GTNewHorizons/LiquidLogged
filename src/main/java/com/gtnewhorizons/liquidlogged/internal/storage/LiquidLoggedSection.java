package com.gtnewhorizons.liquidlogged.internal.storage;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

/** Added to {@code ExtendedBlockStorage} by mixin; cast a section to it to read or replace its fluids. */
public interface LiquidLoggedSection {

    @Nullable
    SectionFluids liquidlogged$get();

    void liquidlogged$set(@Nullable SectionFluids fluids);

    /** Tag of a newer format version, written back unchanged on save. */
    @Nullable
    NBTTagCompound liquidlogged$getPreserved();

    void liquidlogged$setPreserved(@Nullable NBTTagCompound tag);
}
