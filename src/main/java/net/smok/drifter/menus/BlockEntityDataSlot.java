package net.smok.drifter.menus;

import net.minecraft.nbt.CompoundTag;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.utils.CustomDataSlot;

public class BlockEntityDataSlot extends CustomDataSlot {

    private final ExtendedBlockEntity block;
    private int changeCount;

    public BlockEntityDataSlot(ExtendedBlockEntity block) {
        this.block = block;
    }

    @Override
    public boolean changed() {
        return changeCount != block.getChangeCount();
    }

    @Override
    public void sendData(CompoundTag data) {
        data.put("block", block.saveWithFullMetadata());
        changeCount = block.getChangeCount();
    }

    @Override
    public void receiveData(CompoundTag data) {
        if (data.contains("block", CompoundTag.TAG_COMPOUND)) {
            CompoundTag tag = data.getCompound("block");
            block.load(tag);
        }
    }
}
