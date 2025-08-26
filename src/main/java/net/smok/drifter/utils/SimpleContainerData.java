package net.smok.drifter.utils;

import net.minecraft.world.inventory.ContainerData;

public class SimpleContainerData implements ContainerData {
    private final int[] ints;

    public SimpleContainerData(int i) {
        this.ints = new int[i];
    }

    public int[] getAll() {
        return ints;
    }

    @Override
    public int get(int i) {
        return this.ints[i];
    }

    @Override
    public void set(int i, int j) {
        this.ints[i] = j;
    }

    @Override
    public int getCount() {
        return this.ints.length;
    }

    public void setAll(int[] values) {
        for (int i = 0; i < ints.length && i < values.length; i++) ints[i] = values[i];
    }
}