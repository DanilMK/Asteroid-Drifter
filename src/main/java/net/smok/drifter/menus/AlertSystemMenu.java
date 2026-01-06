package net.smok.drifter.menus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.registries.DrifterMenus;
import org.jetbrains.annotations.NotNull;

public class AlertSystemMenu extends ExtendedMenu {

    private final @NotNull AlertPanelBlockEntity alertSystemBlock;


    public AlertSystemMenu(Integer id, Inventory playerInventory, @NotNull AlertPanelBlockEntity alertSystemBlock) {
        super(DrifterMenus.ALERT_SYSTEM_MENU.get(), id, playerInventory.player);

        this.alertSystemBlock = alertSystemBlock;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(alertSystemBlock, player);
    }

    public @NotNull AlertPanelBlockEntity getAlertSystemBlock() {
        return alertSystemBlock;
    }

    public BlockPos getBlockPos() {
        return alertSystemBlock.getBlockPos();
    }
}
