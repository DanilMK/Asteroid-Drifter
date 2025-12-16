package net.smok.drifter.menus;

import earth.terrarium.adastra.common.menus.slots.PredicateSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.registries.DrifterMenus;
import org.jetbrains.annotations.NotNull;

public class EngineMenu extends ExtendedMenu {


    private final @NotNull EnginePanelBlockEntity enginePanelBlock;

    public EngineMenu(Integer id, Inventory playerInventory, @NotNull EnginePanelBlockEntity enginePanelBlock) {
        super(DrifterMenus.ENGINE_MENU.get(), id, playerInventory.player);
        this.enginePanelBlock = enginePanelBlock;

        addSlot(new PredicateSlot(enginePanelBlock, EnginePanelBlockEntity.BUCKET_INPUT, 30, 22, stack -> EnginePanelBlockEntity.canPlace(EnginePanelBlockEntity.BUCKET_INPUT, stack)));
        addSlot(new PredicateSlot(enginePanelBlock, EnginePanelBlockEntity.BUCKET_OUTPUT, 30, 52, stack -> false));

        addInventorySlots(playerInventory, 0, 0);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return quickMoveStack(index, 3, 4);
    }

    @Override
    public boolean stillValid(Player player) {
        return enginePanelBlock.stillValid(player);
    }

    public @NotNull EnginePanelBlockEntity getEnginePanelBlock() {
        return enginePanelBlock;
    }


}
