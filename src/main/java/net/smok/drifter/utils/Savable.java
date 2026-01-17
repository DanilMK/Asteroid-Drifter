package net.smok.drifter.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Savable {

    void save(CompoundTag tag);
    void load(CompoundTag tag);


    static String getString(@NotNull CompoundTag tag, String key, String or) {
        if (tag.contains(key, Tag.TAG_STRING)) {
            return tag.getString(key);
        }
        return or;
    }

    static int getInt(@NotNull CompoundTag tag, String key, int or) {
        if (tag.contains(key, Tag.TAG_INT)) {
            return tag.getInt(key);
        }
        return or;
    }

    static boolean getBoolean(@NotNull CompoundTag tag, String key, boolean or) {
        if (tag.contains(key)) {
            return tag.getBoolean(key);
        }
        return or;
    }

    static float getFloat(@NotNull CompoundTag tag, String key, float or) {
        if (tag.contains(key, Tag.TAG_FLOAT)) {
            return tag.getFloat(key);
        }
        return or;
    }

    static Optional<CompoundTag> getCompound(@NotNull CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_COMPOUND)) {
            return Optional.of(tag.getCompound(key));
        }
        return Optional.empty();
    }

    static Optional<ListTag> getCompound(@NotNull CompoundTag tag, String key, byte tagType) {
        if (tag.contains(key, Tag.TAG_LIST)) {
            return Optional.of(tag.getList(key, tagType));
        }
        return Optional.empty();
    }


}
