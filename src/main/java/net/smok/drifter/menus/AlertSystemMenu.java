package net.smok.drifter.menus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.alert.AlertPanelBlockEntity;
import net.smok.drifter.registries.DrifterMenus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlertSystemMenu extends AbstractContainerMenu {

    private final @NotNull AlertPanelBlockEntity alertSystemBlock;
    private final List<AlertPanelBlockEntity.Danger> dangers;


    public AlertSystemMenu(Integer id, @NotNull AlertPanelBlockEntity alertSystemBlock) {
        super(DrifterMenus.ALERT_SYSTEM_MENU.get(), id);

        this.alertSystemBlock = alertSystemBlock;
        this.dangers = alertSystemBlock.getDangers();
        dangers.forEach(danger -> {
            // todo rework alert system
            /*
            addDataSlot(danger.color());
            addDataSlot(danger.active());
            addDataSlot(danger.tested());*/
        });
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(alertSystemBlock, player);
    }

    public List<AlertPanelBlockEntity.Danger> getDangers() {
        return dangers;
    }

    public BlockPos getBlockPos() {
        return alertSystemBlock.getBlockPos();
    }
}
