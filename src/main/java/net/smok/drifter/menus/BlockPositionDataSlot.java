package net.smok.drifter.menus;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.utils.BlockEntityPosition;
import net.smok.drifter.utils.CustomDataSlot;

import java.util.Optional;

public class BlockPositionDataSlot<T extends ExtendedBlockEntity> extends CustomDataSlot {

    private final Level level;
    private final BlockEntityPosition<T> enginePanel;
    private boolean hadEnginePanelBlock;
    private int changeCount;

    public BlockPositionDataSlot(Level level, BlockEntityPosition<T> enginePanel) {
        this.level = level;
        this.enginePanel = enginePanel;
    }


    @Override
    public boolean changed() {
        return getBlock().map(block -> block.getChangeCount() != changeCount).orElse(hadEnginePanelBlock);
    }

    @Override
    public void sendData(CompoundTag data) {
        Optional<T> block = getBlock();
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

    public Optional<T> getBlock() {
        return enginePanel.getBlock(level);
    }
}
