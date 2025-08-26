package net.smok.drifter.blocks.alert;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.smok.drifter.registries.Values;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.blocks.ShipBlock;
import net.smok.drifter.utils.SavedDataSlot;

import java.util.List;

public class AlertLampBlockEntity extends BlockEntity implements ShipBlock {


    private final SavedDataSlot<Integer> activeColor = SavedDataSlot.intValue("activeColor", 0, 15);

    public AlertLampBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(DrifterBlocks.ALERT_LAMP_BLOCK_ENTITY.get(), blockPos, blockState);
    }


    public static void tick(Level lvl, BlockPos blockPos, BlockState blockState, AlertLampBlockEntity type) {
        if (lvl.isClientSide()) return;

        if (lvl.getGameTime() % 20L == 0L) type.activeColor.setValue(0);
        if (lvl.getGameTime() % 80L != 2L) return;
        int color = blockState.getValue(AlertLampBlock.COLOR);
        applyEffect(lvl, blockPos, color);
        AlertLampBlock.updateLampColor(blockState, lvl, blockPos, type.getActiveColor());

    }

    private static void applyEffect(Level lvl, BlockPos blockPos, int color) {
        if (color > 0) {

            AABB aABB = new AABB(blockPos).inflate(8d);
            List<Player> list = lvl.getEntitiesOfClass(Player.class, aABB);

            for (Player player : list) {
                player.addEffect(new MobEffectInstance(Values.ALERT_EFFECT.get(), 100, color, true, false, true));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        activeColor.save(compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        activeColor.load(compoundTag);
    }

    public void activate(int color) {
        activeColor.setValue(color);
        if (level != null) {
            AlertLampBlock.updateLampColor(getBlockState(), level, getBlockPos(), color);
        }
    }

    public int getActiveColor() {
        return activeColor.getValue();
    }
}
