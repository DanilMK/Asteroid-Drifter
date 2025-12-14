package net.smok.drifter.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.smok.drifter.registries.Values;
import net.smok.drifter.utils.ExtraUtils;

import java.util.Optional;
import java.util.function.Consumer;

public class ChestGoal extends Goal {

    private final Level level;
    private final PathfinderMob entity;
    private final Consumer<Consumer<ItemStack>> loot;
    private final Runnable afterPut;

    private BlockPos chestPos;

    public ChestGoal(Level level, PathfinderMob entity, Consumer<Consumer<ItemStack>> loot, Runnable afterPut) {
        this.level = level;
        this.entity = entity;
        this.loot = loot;
        this.afterPut = afterPut;
    }

    @Override
    public boolean canUse() {
        if (level.getGameTime() % 10L < 2L) return false;

        Optional<BlockPos> chestOptional = BlockPos.betweenClosedStream(
                        entity.getOnPos().offset(-4, -2, -4),
                        entity.getOnPos().offset(4, 2, 4))
                .filter(pos -> level.getBlockState(pos).is(TagKey.create(BuiltInRegistries.BLOCK.key(),
                        new ResourceLocation(Values.MOD_ID, "mandrake_containers")))).findAny();

        if (chestOptional.isPresent()) {
            chestPos = chestOptional.get();
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        Vec3 position = chestPos.getCenter();
        entity.getLookControl().setLookAt(position);
        entity.getNavigation().moveTo(position.x, position.y, position.z, 1);
    }

    @Override
    public void tick() {
        if (entity.distanceToSqr(chestPos.getCenter()) < 2) {
            putToChest(chestPos);
        }
    }

    private void putToChest(BlockPos containerPosition) {
        BlockEntity blockEntity = level.getBlockEntity(containerPosition);
        if (blockEntity instanceof Container container) {
            loot.accept(itemStack -> {
                if (itemStack != null && ExtraUtils.putItemToContainer(container, itemStack)) {
                    Player nearestPlayer = level.getNearestPlayer(entity, 20);
                    if (nearestPlayer != null) {
                        container.startOpen(nearestPlayer);
                    }
                    afterPut.run();
                }
            });
        }
    }
}
