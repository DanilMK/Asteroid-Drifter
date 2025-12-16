package net.smok.drifter.blocks.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.menus.ShipStructureBlockMenu;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.utils.ExtraUtils;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShipStructureBlockEntity extends ExtendedBlockEntity {


    private final SavedDataSlot<Integer> sizeX = SavedDataSlot.intValue("x", 0, 16);
    private final SavedDataSlot<Integer> sizeY = SavedDataSlot.intValue("y", 0, 16);
    private final SavedDataSlot<Integer> sizeZ = SavedDataSlot.intValue("z", 0, 16);
    private final SavedDataSlot<Boolean> visibleBox = SavedDataSlot.booleanValue("visibleBox");
    private final SavedDataSlot<Boolean> visibleBlocks = SavedDataSlot.booleanValue("visibleBlocks");

    private final HashMap<BlockPos, ShipBlock> shipBlocks = new HashMap<>();
    private final long timeOffset;
    private ShipStructure structure;

    public ShipStructureBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.SHIP_STRUCTURE_BLOCK_ENTITY.get(), pos, blockState);
        timeOffset = pos.getX() / 16 + pos.getY() / 16 + pos.getZ() / 16;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.asteroid_drifter.ship_structure_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ShipStructureBlockMenu(i, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag size = new CompoundTag();

        sizeX.save(size);
        sizeY.save(size);
        sizeZ.save(size);
        tag.put("size", size);
        visibleBlocks.save(tag);
        visibleBox.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("size", Tag.TAG_COMPOUND)) {
            CompoundTag size = tag.getCompound("size");
            sizeX.load(size);
            sizeY.load(size);
            sizeZ.load(size);
        }
        visibleBlocks.load(tag);
        visibleBox.load(tag);
    }

    public void tick(long gameTime) {
        if ((gameTime + timeOffset) % 20L == 0) {
            ShipStructure str = getStructure();
            if (collectBlocks()) {
                str.updateShipBlocks(shipBlocks);
            }
        }
    }

    private boolean collectBlocks() {
        if (level == null) return false;

        AtomicBoolean changed = new AtomicBoolean(false);
        HashSet<BlockPos> oldBlocks = new HashSet<>(shipBlocks.keySet());
        shipBlocks.clear();


        BlockPos.betweenClosedStream(getBlockPos().offset(-sizeX(), -sizeY(), -sizeZ()), getBlockPos().offset(sizeX(), sizeY(), sizeZ())).forEach(pos -> {

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ShipBlock shipBlock) {
                shipBlocks.put(blockEntity.getBlockPos(), shipBlock);
                if (!oldBlocks.contains(pos)) changed.set(true);

            } else if (level.getBlockState(pos).getBlock() instanceof ShipBlock shipBlock) {
                shipBlocks.put(new BlockPos(pos), shipBlock);
                if (!oldBlocks.contains(pos)) changed.set(true);
            }
        });

        if (changed.get() || oldBlocks.size() != shipBlocks.size()) {
            setChanged();
            return true;
        }
        return false;
    }

    public int sizeX() {
        return sizeX.getValue();
    }

    public void sizeX(int value) {
        sizeX.setValue(value);
    }

    public int sizeY() {
        return sizeY.getValue();
    }

    public void sizeY(int value) {
        sizeY.setValue(value);
    }

    public int sizeZ() {
        return sizeZ.getValue();
    }

    public void sizeZ(int value) {
        sizeZ.setValue(value);
    }

    public boolean visibleBox() {
        return visibleBox.getValue();
    }

    public void setVisibleBox(boolean isVisible) {
        visibleBox.setValue(isVisible);
    }

    public boolean visibleBLocks() {
        return visibleBlocks.getValue();
    }

    public void setVisibleBlocks(boolean isVisible) {
        visibleBlocks.setValue(isVisible);
    }

    public void setFromMenu(Player player, @NotNull BlockPos size, boolean boxVisible, boolean blockVisible) {
        if (Container.stillValidBlockEntity(this, player)) {
            setSize(size);
            setVisibleBox(boxVisible);
            setVisibleBlocks(blockVisible);
            setChanged();
        }
    }

    public void setSize(@NotNull BlockPos size) {
        sizeX(size.getX());
        sizeY(size.getY());
        sizeZ(size.getZ());
    }

    public @NotNull BlockPos getSize() {
        return new BlockPos(sizeX(), sizeY(), sizeY());
    }

    public @NotNull BlockPos getBoxMin() {
        return getBlockPos().subtract(getSize());
    }

    public @NotNull BlockPos getBoxMax() {
        return getBlockPos().offset(getSize());
    }

    public HashMap<BlockPos, ShipBlock> getShipBlocks() {
        return shipBlocks;
    }

    public ShipStructure getStructure() {
        if (structure != null) return structure;
        ShipStructure str = findStructure(Objects.requireNonNull(level, "Cannot get Ship Structure at this time."), getBlockPos())
                .orElse(new ShipStructure());
        str.put(getBlockPos(), this);
        return structure = str;
    }

    public static @NotNull Optional<ShipStructure> findStructure(@NotNull Level level, @NotNull BlockPos blockPos) {
        for (LevelChunk chunk : ExtraUtils.chunks5x5(level, blockPos)) {
            Optional<ShipStructure> first = chunk.getBlockEntities().values().stream()
                    .filter(blockEntity -> blockEntity instanceof ShipStructureBlockEntity shipStructureBlock && shipStructureBlock.structure != null)
                    .map(blockEntity -> ((ShipStructureBlockEntity) blockEntity).structure).findFirst();

            if (first.isPresent()) {
                return first;
            }
        }
        return Optional.empty();
    }

}
