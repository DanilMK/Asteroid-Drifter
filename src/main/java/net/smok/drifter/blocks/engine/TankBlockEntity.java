package net.smok.drifter.blocks.engine;

import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.utils.BlockEntityPosition;
import org.jetbrains.annotations.NotNull;

public class TankBlockEntity extends BlockEntity implements BotariumFluidBlock<WrappedBlockFluidContainer> {

    private final WrappedBlockFluidContainer fluidContainer;

    private final BlockEntityPosition<EnginePanelBlockEntity> engine = new BlockEntityPosition<>("engine", DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get());

    public TankBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.TANK_BLOCK_ENTITY.get(), pos, blockState);
        fluidContainer = new WrappedBlockFluidContainer(this,
                new SimpleFluidContainer(blockState.getBlock() instanceof TankBlock tankBlock ? tankBlock.getContainerAmount() : 0,
                        1, (integer, fluidHolder) -> canBeAccessed())
        );
        engine.setPos(getBlockPos().below());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        fluidContainer.serialize(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        fluidContainer.deserialize(tag);
    }

    @Override
    public WrappedBlockFluidContainer getFluidContainer() {
        return fluidContainer;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean canBeAccessed() {
        return engine.getBlock(level).isEmpty() || engine.getBlock(level).get().stand();
    }

    public void decrease() {
        long amount = fluidContainer.getFirstFluid().getFluidAmount();
        if (amount > 0) fluidContainer.getFirstFluid().setAmount(amount - 81);
    }

    // todo add comparator
}
