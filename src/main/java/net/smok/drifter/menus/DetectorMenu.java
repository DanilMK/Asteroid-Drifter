package net.smok.drifter.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.alert.DetectorBlockEntity;
import net.smok.drifter.registries.DrifterMenus;

public class DetectorMenu extends ExtendedMenu{

    private final DetectorBlockEntity detector;

    public DetectorMenu(int containerId, Inventory inventory, DetectorBlockEntity detector) {
        super(DrifterMenus.DETECTOR_MENU.get(), containerId, inventory.player);
        this.detector = detector;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(detector, player);
    }

    public DetectorBlockEntity getDetector() {
        return detector;
    }
}
