package com.brandon3055.brandonscore.lib;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

public interface INBTSerializable<T extends Tag> {

    T serializeNBT(HolderLookup.Provider provider);

    void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
