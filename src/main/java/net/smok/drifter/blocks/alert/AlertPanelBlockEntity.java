package net.smok.drifter.blocks.alert;

import earth.terrarium.botarium.common.menu.ExtraDataMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.blocks.controller.SpaceCollision;
import net.smok.drifter.blocks.controller.collision.Collision;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.utils.SavedDataSlot;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AlertPanelBlockEntity extends BlockEntity implements ExtraDataMenuProvider, ShipBlock {



    private final Danger bigCollision = new Danger("big_collision", 5);
    private final Danger smallCollision = new Danger("small_collision", 7);


    private final Set<BlockPos> lampPoses = new HashSet<>();
    private final List<Danger> dangers = List.of(bigCollision, smallCollision);

    public AlertPanelBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get(), blockPos, blockState);
    }



    @Override
    public void writeExtraData(ServerPlayer player, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(getBlockPos());
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.asteroid_drifter.alert_system");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new AlertSystemMenu(i, this);
    }

    public void tick(@NotNull Level lvl) {
        if (lvl.getGameTime() % 20L == 19L)
            dangers.forEach(danger -> danger.active.setValue(false));

        if (lvl.getGameTime() % 20L != 1L) return;
        for (Danger danger : dangers) {
            if (danger.active.getValue() | danger.tested.getValue()) {
                sendActiveToLamps(lvl, danger);
                break;
            }
        }

    }

    private void sendActiveToLamps(Level lvl, Danger danger) {
        for (BlockPos lampPose : new HashSet<>(lampPoses)) {
            Optional<AlertLampBlockEntity> lamp = lvl.getChunkAt(lampPose)
                    .getBlockEntity(lampPose, DrifterBlocks.ALERT_LAMP_BLOCK_ENTITY.get());

            if (lamp.isPresent()) lamp.get().activate(danger.color.getValue());
            else lampPoses.remove(lampPose);
        }
    }

    public void startDanger(int dangerId) {
        dangers.get(dangerId).active.setValue(true);
        setChanged();
    }

    public void startDanger(SpaceCollision collision) {
        switch (collision) {
            case ONE_BIG -> bigCollision.active.setValue(true);
            case ONE_SMALL -> smallCollision.active.setValue(true);
        }
        setChanged();
    }

    public void startDanger(Collision collision) {
        dangers.get(0).active.setValue(true);
        setChanged();
    }

    public void testDanger(Integer dangerId) {
        SavedDataSlot<Boolean> tested = dangers.get(dangerId).tested;
        tested.setValue(!tested.getValue());
        setChanged();
    }

    public void changeColor(Integer danger, Integer color) {
        dangers.get(danger).color.setValue(color);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ListTag dangersTag = new ListTag();
        for (Danger danger : dangers) dangersTag.add(danger.save());
        compoundTag.put("dangers", dangersTag);

        ListTag lamps = new ListTag();
        for (BlockPos lampPose : lampPoses)
            lamps.add(NbtUtils.writeBlockPos(lampPose));
        compoundTag.put("lamps", lamps);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        ListTag dangersTag = compoundTag.getList("dangers", Tag.TAG_COMPOUND);
        for (int i = 0; i < dangers.size() & i < dangersTag.size(); i++) {
            dangers.get(i).load(dangersTag.getCompound(i));
        }


        if (compoundTag.contains("lamps", CompoundTag.TAG_LIST)) {
            ListTag lamps = compoundTag.getList("lamps", CompoundTag.TAG_COMPOUND);
            for (Tag lamp : lamps) lampPoses.add(NbtUtils.readBlockPos((CompoundTag) lamp));
        }
    }

    public List<Danger> getDangers() {
        return dangers;
    }

    @Override
    public boolean bind(BlockPos pos, ShipBlock other) {
        if (other instanceof AlertLampBlockEntity) {
            lampPoses.add(pos);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        lampPoses.clear();
        setChanged();
    }

    public record Danger(String name, SavedDataSlot<Integer> color, SavedDataSlot<Boolean> active, SavedDataSlot<Boolean> tested) {

        public Danger(String name, int defColor) {
            this(name, SavedDataSlot.intValue("color", 0, 15).setValue(defColor),
                    SavedDataSlot.booleanValue("active"), SavedDataSlot.booleanValue("tested"));
        }

        public @NotNull CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            color.save(tag);
            active.save(tag);
            tested.save(tag);
            return tag;
        }

        public void load(@NotNull CompoundTag tag) {
            color.load(tag);
            active.load(tag);
            tested.load(tag);
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull Component text() {
            return Component.translatable("tooltip.asteroid_drifter.danger." + name);
        }
    }
}
