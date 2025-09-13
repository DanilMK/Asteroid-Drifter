package net.smok.drifter.utils;

import net.minecraft.nbt.CompoundTag;

public interface CompoundSerializable {

    default CompoundTag saveData() {
        CompoundTag tag = new CompoundTag();
        saveData(tag);
        return tag;
    }

    void saveData(CompoundTag tag);

    void loadData(CompoundTag tag);
}
