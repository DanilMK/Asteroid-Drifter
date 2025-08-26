package net.smok.drifter.blocks.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.drifter.Debug;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.registries.DrifterMenus;
import net.smok.drifter.menus.ExtendedMenu;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.CustomDataSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShipControllerMenu extends ExtendedMenu {

    private final ShipControllerBlockEntity controllerBlock;


    private final List<AsteroidSlot> asteroidSlots;

    private final EngineDataSlot engineDataSlot;


    public ShipControllerMenu(Integer id, Inventory inventory, @NotNull ShipControllerBlockEntity controllerBlock) {
        super(DrifterMenus.SHIP_CONTROLLER_MENU.get(), id, inventory.player);
        this.controllerBlock = controllerBlock;
        asteroidSlots = new ArrayList<>();
        controllerBlock.getDataSlots().forEach(this::addDataSlot);

        engineDataSlot = new EngineDataSlot(inventory.player.level(), controllerBlock.getEnginePanelBlock());

        ContainerData xPoses = controllerBlock.getX();
        ContainerData yPoses = controllerBlock.getY();
        ContainerData dist = controllerBlock.getDist();
        addDataSlots(xPoses);
        addDataSlots(yPoses);
        addDataSlots(dist);

        addCustomDataSlot(engineDataSlot);



        for (int i = 0; i < controllerBlock.getContainerSize(); i++) {
            DataSlot xSlot = DataSlot.forContainer(xPoses, i);
            DataSlot ySlot = DataSlot.forContainer(yPoses, i);
            DataSlot distSlot = DataSlot.forContainer(dist, i);
            AsteroidSlot slot = new AsteroidSlot(controllerBlock, i, -50, -50, xSlot, ySlot, distSlot);
            asteroidSlots.add(slot);
            addSlot(slot);
        }

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

    public List<AsteroidSlot> getAsteroidSlots() {
        return asteroidSlots;
    }

    public ShipControllerBlockEntity controller() {
        return controllerBlock;
    }

    public Optional<EnginePanelBlockEntity> getEngineBlockEntity() {
        return engineDataSlot.getBlock();
    }

    private static class EngineDataSlot extends CustomDataSlot {

        private final Level level;
        private final BlockEntityPosition<EnginePanelBlockEntity> enginePanel;
        private boolean hadEnginePanelBlock;
        private int changeCount;

        private EngineDataSlot(Level level, BlockEntityPosition<EnginePanelBlockEntity> enginePanel) {
            this.level = level;
            this.enginePanel = enginePanel;
        }


        @Override
        public boolean changed() {
            return getBlock().map(block -> block.getChangeCount() != changeCount).orElse(hadEnginePanelBlock);
        }

        @Override
        public void sendData(CompoundTag data) {
            Optional<EnginePanelBlockEntity> block = getBlock();
            block.ifPresent(engine -> {
                CompoundTag tag = engine.saveWithFullMetadata();
                data.put("block", tag);
                changeCount = engine.getChangeCount();
            });
            hadEnginePanelBlock = block.isPresent();
        }

        @Override
        public void receiveData(CompoundTag data) {
            CompoundTag data1 = data.getCompound("block");
            enginePanel.load(data1);
            enginePanel.executeIfPresent(level, block -> block.load(data1));
        }

        public Optional<EnginePanelBlockEntity> getBlock() {
            return enginePanel.getBlock(level);
        }
    }

}
