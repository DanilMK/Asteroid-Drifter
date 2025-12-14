package net.smok.drifter.blocks.garden;

import earth.terrarium.adastra.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.smok.drifter.entities.MartianMandrake;
import net.smok.drifter.registries.DrifterEntities;
import org.jetbrains.annotations.Nullable;

public class MartianMandrakeBlock extends CropBlock {

    private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0, -1.0, 5.0, 11.0, 3.0, 11.0);
    private static final VoxelShape FULL_LOWER_SHAPE = Block.box(3.0, -1.0, 3.0, 13.0, 16.0, 13.0);
    private static final VoxelShape[] LOWER_SHAPE_BY_AGE = new VoxelShape[]{
            COLLISION_SHAPE_BULB, Block.box(3.0, -1.0, 3.0, 13.0, 14.0, 13.0), FULL_LOWER_SHAPE, FULL_LOWER_SHAPE, FULL_LOWER_SHAPE
    };

    public MartianMandrakeBlock(Properties properties) {
        super(4, i -> LOWER_SHAPE_BY_AGE[i], properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE_BULB;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.MARS_SAND.get()) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (isMaxAge(state) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) == 0 && level.random.nextInt(0, 3) != 0) {
            MartianMandrake martianMandrake = new MartianMandrake(DrifterEntities.MARTIAN_MANDRAKE.get(), level);
            martianMandrake.setPos(pos.getCenter());
            level.addFreshEntity(martianMandrake);
            level.playSound(martianMandrake, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1, 1);

            player.awardStat(Stats.BLOCK_MINED.get(this));
            player.causeFoodExhaustion(0.005F);
        } else {
            super.playerDestroy(level, player, pos, state, blockEntity, tool);
        }
    }
}
