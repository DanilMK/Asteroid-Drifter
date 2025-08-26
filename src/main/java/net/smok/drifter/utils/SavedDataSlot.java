package net.smok.drifter.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.DataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class SavedDataSlot<T> extends DataSlot {

    private T value;

    public SavedDataSlot(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public SavedDataSlot<T> setValue(T value) {
        this.value = value;
        return this;
    }

    public abstract void load(CompoundTag compoundTag);

    public abstract void save(CompoundTag compoundTag);


    @Contract(value = "_ -> new", pure = true)
    public static @NotNull SavedDataSlot<Integer> intValue(String name) {
        return new SavedDataSlot<>(0) {
            @Override
            public int get() {
                return getValue();
            }

            @Override
            public void set(int i) {
                setValue(i);
            }

            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(name, Tag.TAG_INT))
                    setValue(compoundTag.getInt(name));
            }

            @Override
            public void save(CompoundTag compoundTag) {
                compoundTag.putInt(name, getValue());
            }
        };
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull SavedDataSlot<Integer> intValue(String name, int min, int max) {
        return new SavedDataSlot<>(min) {
            @Override
            public int get() {
                return getValue();
            }

            @Override
            public void set(int i) {
                setValue(i);
            }

            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(name, Tag.TAG_INT))
                    setValue(compoundTag.getInt(name));
            }

            @Override
            public void save(CompoundTag compoundTag) {
                compoundTag.putInt(name, getValue());
            }

            @Override
            public SavedDataSlot<Integer> setValue(Integer value) {
                if (value < min) value = min;
                if (value > max) value = max;
                return super.setValue(value);
            }
        };
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull SavedDataSlot<Boolean> booleanValue(String name) {
        return new SavedDataSlot<>(false) {

            @Override
            public int get() {
                return getValue() ? 1 : 0;
            }

            @Override
            public void set(int i) {
                setValue(i > 0);
            }

            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(name)) setValue(compoundTag.getBoolean(name));
            }

            @Override
            public void save(CompoundTag compoundTag) {
                compoundTag.putBoolean(name, getValue());
            }
        };
    }

    public static SavedDataSlot<BlockPos> blockPosValue(String name) {
        return new SavedDataSlot<BlockPos>(BlockPos.ZERO) {
            @Override
            public void load(CompoundTag compoundTag) {
                if (compoundTag.contains(name, Tag.TAG_COMPOUND))
                    setValue(NbtUtils.readBlockPos(compoundTag.getCompound(name)));
            }

            @Override
            public void save(CompoundTag compoundTag) {
                compoundTag.put(name, NbtUtils.writeBlockPos(getValue()));
            }

            @Override
            public int get() {
                return 0;
            }

            @Override
            public void set(int value) {

            }
        };
    }


}
