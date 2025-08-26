package net.smok.drifter.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Optional;
import java.util.function.Consumer;

public final class BlockEntityPosition<T extends BlockEntity> {
    private BlockPos pos;
    private final String name;
    private final BlockEntityType<T> blockEntityType;

    public BlockEntityPosition(String name, BlockEntityType<T> blockEntityType) {
        this.name = name;
        this.blockEntityType = blockEntityType;
    }

    public BlockPos pos() {
        return pos;
    }

    public String name() {
        return name;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void load(CompoundTag compoundTag) {
        if (compoundTag.contains(name, Tag.TAG_COMPOUND))
            pos = NbtUtils.readBlockPos(compoundTag.getCompound(name));
    }

    public void save(CompoundTag compoundTag) {
        if (pos != null) compoundTag.put(name, NbtUtils.writeBlockPos(pos));
    }

    public Optional<T> getBlock(Level level) {
        if (pos != null) return level.getBlockEntity(pos, blockEntityType);
        return Optional.empty();
    }

    public void executeIfPresent(Level level, Consumer<T> consumer) {
        getBlock(level).ifPresent(consumer);
    }

    public void executeIfPresentOrElse(Level level, Consumer<T> executable, Consumer<BlockPos> elseExecutable) {
        getBlock(level).ifPresentOrElse(executable, () -> elseExecutable.accept(pos));
    }

    @Override
    public String toString() {
        return "BlockEntityPosition{" +
                "pos=" + pos +
                ", name='" + name + '\'' +
                ", blockEntityType=" + blockEntityType +
                '}';
    }
}
