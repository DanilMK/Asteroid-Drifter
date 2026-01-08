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
import net.smok.drifter.blocks.ExtendedBlockEntity;
import net.smok.drifter.menus.AlertSystemMenu;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.ShipBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AlertPanelBlockEntity extends ExtendedBlockEntity implements ExtraDataMenuProvider, ShipBlock, Detector {



    private final Set<BlockPos> lamps = new HashSet<>();
    private final List<AlertContainer> alerts = new ArrayList<>();


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
        return new AlertSystemMenu(i, inventory, this);
    }

    public void tick(@NotNull Level lvl) {
        if (lvl.getGameTime() % 20L != 1L) return;

        List<Alert> alerts = getAllAlerts().stream().filter(Alert::isActiveOrTested).toList();

        for (BlockPos lampPose : new HashSet<>(lamps)) {
            Optional<AlertLampBlockEntity> lamp = lvl.getChunkAt(lampPose)
                    .getBlockEntity(lampPose, DrifterBlocks.ALERT_LAMP_BLOCK_ENTITY.get());

            if (lamp.isPresent()) lamp.get().activate(alerts);
            else lamps.remove(lampPose);
        }
    }


    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        ListTag lamps = new ListTag();
        for (BlockPos pos : this.lamps)
            lamps.add(NbtUtils.writeBlockPos(pos));
        compoundTag.put("lamps", lamps);

        ListTag alertsList = new ListTag();
        for (AlertContainer alert : alerts) {
            CompoundTag tag = new CompoundTag();
            tag.put("detector", NbtUtils.writeBlockPos(alert.blockPos));
            tag.putInt("index", alert.index);
            alertsList.add(tag);
        }
        compoundTag.put("alerts", alertsList);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);


        if (compoundTag.contains("lamps", CompoundTag.TAG_LIST)) {
            ListTag lamps = compoundTag.getList("lamps", CompoundTag.TAG_COMPOUND);
            for (Tag lamp : lamps) this.lamps.add(NbtUtils.readBlockPos((CompoundTag) lamp));
        }

        alerts.clear();
        if (compoundTag.contains("alerts", CompoundTag.TAG_LIST)) {
            ListTag alertsList = compoundTag.getList("alerts", CompoundTag.TAG_COMPOUND);
            for (Tag alert : alertsList) {
                CompoundTag alertTag = (CompoundTag) alert;
                if (alertTag.contains("detector", Tag.TAG_COMPOUND)) {
                    alerts.add(new AlertContainer(NbtUtils.readBlockPos(alertTag.getCompound("detector")),
                            alertTag.contains("index") ? alertTag.getInt("index") : 0));
                }
            }
        }
    }

    @Override
    public List<Alert> getAllAlerts() {
        List<Alert> list = new ArrayList<>();
        if (level == null) return list;
        int max = alerts.size() - 1;
        for (int i = max; i >= 0; i--) {
            AlertContainer alertContainer = alerts.get(i);
            int finalI = i;
            alertContainer.alert(level).ifPresentOrElse(alert -> list.add(0, alert), () -> alerts.remove(finalI));
        }
        if (max + 1 != alerts.size()) setChanged();
        return list;
    }

    @Override
    public int alertsSize() {
        return alerts.size();
    }

    @Override
    public boolean isExtreme() {
        return false;
    }

    @Override
    public void swap(int alertA, int alertB) {
        if (alertA >= alertsSize() || alertA < 0 || alertB >= alertsSize() || alertB < 0) return;

        AlertContainer change = alerts.get(alertA);
        alerts.set(alertA, alerts.get(alertB));
        alerts.set(alertB, change);
        setChanged();
    }

    @Override
    public boolean bind(BlockPos pos, ShipBlock other) {
        if (other instanceof AlertPanelBlockEntity) return false;
        if (other instanceof AlertLampBlockEntity) {
            lamps.add(pos);
            setChanged();
            return true;
        }
        if (other instanceof Detector detector && detector.isExtreme() &&
                (alerts.isEmpty() || alerts.stream().noneMatch(alertContainer -> alertContainer.blockPos.equals(pos)))) {
            List<Alert> allAlerts = detector.getAllAlerts();
            for (int i = 0; i < allAlerts.size(); i++) {
                alerts.add(new AlertContainer(pos, i));
            }
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        lamps.clear();
        alerts.clear();
        setChanged();
    }

    private record AlertContainer(BlockPos blockPos, int index) {

        public Optional<Alert> alert(@NotNull Level level) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof Detector detector && detector.isExtreme() && detector.alertsSize() > index)
                return Optional.of(detector.getAllAlerts().get(index));
            return Optional.empty();
        }
    }

}
