package net.smok.drifter.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.structure.ShipStructureBlockEntity;
import net.smok.drifter.registries.DrifterMenus;
import org.jetbrains.annotations.NotNull;

public class ShipStructureBlockMenu extends AbstractContainerMenu {


    private final @NotNull ShipStructureBlockEntity structureBlock;

    public ShipStructureBlockMenu(int containerId, Inventory inventory, @NotNull ShipStructureBlockEntity structureBlock) {
        super(DrifterMenus.SHIP_STRUCTURE_MENU.get(), containerId);

        this.structureBlock = structureBlock;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(structureBlock, player);
    }

    public @NotNull ShipStructureBlockEntity getStructureBlock() {
        return structureBlock;
    }
}
