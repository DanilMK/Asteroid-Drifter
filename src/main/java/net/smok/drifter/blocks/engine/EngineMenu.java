package net.smok.drifter.blocks.engine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.registries.DrifterMenus;
import net.smok.drifter.menus.ExtendedMenu;
import net.smok.drifter.utils.CustomDataSlot;
import org.jetbrains.annotations.NotNull;

public class EngineMenu extends ExtendedMenu {


    private final @NotNull EnginePanelBlockEntity enginePanelBlock;

    public EngineMenu(Integer id, Inventory playerInventory, @NotNull EnginePanelBlockEntity enginePanelBlock) {
        super(DrifterMenus.ENGINE_MENU.get(), id, playerInventory.player);
        this.enginePanelBlock = enginePanelBlock;

        addSlot(new Slot(enginePanelBlock, EnginePanelBlockEntity.TANK_0, 101, 55));
        addSlot(new Slot(enginePanelBlock, EnginePanelBlockEntity.SECOND_UPGRADE, 129, 55));
        addSlot(new Slot(enginePanelBlock, EnginePanelBlockEntity.BUCKET_INPUT, 30, 22));
        addSlot(new Slot(enginePanelBlock, EnginePanelBlockEntity.BUCKET_OUTPUT, 30, 52));


        addCustomDataSlot(new EngineSlot(enginePanelBlock));

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



    private static class EngineSlot extends CustomDataSlot {
        private final EnginePanelBlockEntity engine;
        private int changeCount;


        private EngineSlot(EnginePanelBlockEntity engine) {
            this.engine = engine;
        }

        @Override
        public boolean changed() {
            return engine.getChangeCount() != changeCount;
        }

        @Override
        public void sendData(CompoundTag data) {
            engine.saveAdditional(data);
            changeCount = engine.getChangeCount();
        }

        @Override
        public void receiveData(CompoundTag data) {
            engine.load(data);
        }
    }
}
