package net.smok.drifter.blocks.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.menus.BlockEntityDataSlot;
import net.smok.drifter.menus.BlockPositionDataSlot;
import net.smok.drifter.registries.DrifterMenus;
import net.smok.drifter.menus.ExtendedMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShipControllerMenu extends ExtendedMenu {

    private final ShipControllerBlockEntity controllerBlock;



    private final BlockPositionDataSlot<EnginePanelBlockEntity> engineDataSlot;


    public ShipControllerMenu(Integer id, Inventory inventory, @NotNull ShipControllerBlockEntity controllerBlock) {
        super(DrifterMenus.SHIP_CONTROLLER_MENU.get(), id, inventory.player);
        this.controllerBlock = controllerBlock;
        controllerBlock.getDataSlots().forEach(this::addDataSlot);

        engineDataSlot = new BlockPositionDataSlot<>(inventory.player.level(), controllerBlock.getEnginePanelBlock());

        addCustomDataSlot(engineDataSlot);
        addCustomDataSlot(new BlockEntityDataSlot(controllerBlock));

    }



    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return controllerBlock.stillValid(player);
    }

    public BlockPos getBlockPos() {
        return controllerBlock.getBlockPos();
    }

    public ShipControllerBlockEntity controller() {
        return controllerBlock;
    }

    public Optional<EnginePanelBlockEntity> getEngineBlockEntity() {
        return engineDataSlot.getBlock();
    }

}
