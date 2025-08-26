package net.smok.drifter.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.registries.Values;

public abstract class CustomDataSlot {

    public static final ResourceLocation ID = new ResourceLocation(Values.MOD_ID, "custom_data");

    public abstract boolean changed();

    public abstract void sendData(CompoundTag data);

    public abstract void receiveData(CompoundTag data);


}
