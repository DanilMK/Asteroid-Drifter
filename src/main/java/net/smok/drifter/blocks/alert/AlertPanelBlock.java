package net.smok.drifter.blocks.alert;

import earth.terrarium.botarium.common.menu.ExtraDataMenuProvider;
import earth.terrarium.botarium.common.menu.MenuHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.smok.drifter.registries.DrifterBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlertPanelBlock extends BaseEntityBlock {
    public AlertPanelBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new AlertPanelBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }


    @Override
    public InteractionResult use(BlockState blockState, @NotNull Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        MenuHooks.openMenu((ServerPlayer) player, (ExtraDataMenuProvider) blockState.getMenuProvider(level, blockPos));
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return blockEntityType == DrifterBlocks.ALERT_PANEL_BLOCK_ENTITY.get() ? (level1, blockPos, blockState1, blockEntity) -> {
            if (!level1.isClientSide) ((AlertPanelBlockEntity)blockEntity).tick(level1);
        } : null;
    }
}
