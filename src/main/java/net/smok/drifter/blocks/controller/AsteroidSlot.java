package net.smok.drifter.blocks.controller;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AsteroidSlot extends Slot {

    private final DataSlot x1, y1, dist;

    public AsteroidSlot(Container container, int index, int x, int y, DataSlot x1, DataSlot y1, DataSlot dist) {
        super(container, index, x, y);
        this.x1 = x1;
        this.y1 = y1;
        this.dist = dist;
    }

    public int getX() {
        return x1.get();
    }

    public int getY() {
        return y1.get();
    }

    public DataSlot getDist() {
        return dist;
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        super.onTake(player, itemStack);
    }

}
