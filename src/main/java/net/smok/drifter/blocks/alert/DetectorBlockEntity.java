package net.smok.drifter.blocks.alert;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.menus.DetectorMenu;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DetectorBlockEntity extends ExtendedBlockEntity implements Detector, ShipBlock {
    private final SavedDataSlot<Alert> alert = Alert.savedDataSlot(this, "alert.basic", Icon.ICON_PRESETS[0], "alert");

    private final SavedDataSlot<Integer> minSignal = SavedDataSlot.intValue("min_signal", 0, 15).setValue(1);
    private final SavedDataSlot<Integer> maxSignal = SavedDataSlot.intValue("max_signal", 0, 15).setValue(15);

    public DetectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.DETECTOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.asteroid_drifter.detector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DetectorMenu(i, inventory, this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        alert.load(tag);
        minSignal.load(tag);
        maxSignal.load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        alert.save(tag);
        minSignal.save(tag);
        maxSignal.save(tag);
    }

    @Override
    public List<Alert> getAllAlerts() {
        return List.of(alert.getValue());
    }

    public Alert getAlert() {
        return alert.getValue();
    }

    public int getMinSignal() {
        return minSignal.getValue();
    }

    public void setMinSignal(int minSignal) {
        this.minSignal.setValue(minSignal);
        checkAndActivate(getBlockState(), getBlockPos());
        setChanged();
    }

    public int getMaxSignal() {
        return maxSignal.getValue();
    }

    public void setMaxSignal(int maxSignal) {
        this.maxSignal.setValue(maxSignal);
        checkAndActivate(getBlockState(), getBlockPos());
        setChanged();
    }

    public boolean canBeActivate(int signal) {
        return signal >= minSignal.getValue() && signal <= maxSignal.getValue();
    }

    public void checkAndActivate(@NotNull BlockState ownState, @NotNull BlockPos ownPos) {
        boolean canBeActivate = canBeActivate(level.getBestNeighborSignal(ownPos));
        boolean isActive = ownState.getValue(DetectorBlock.POWERED);
        if (canBeActivate != isActive) {
            alert.getValue().setActive(canBeActivate);
            level.setBlock(ownPos, ownState.cycle(DetectorBlock.POWERED), 2);
        }
    }
}
