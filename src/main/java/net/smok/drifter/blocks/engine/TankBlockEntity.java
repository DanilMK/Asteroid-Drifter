package net.smok.drifter.blocks.engine;

import earth.terrarium.botarium.common.fluid.base.BotariumFluidBlock;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedBlockFluidContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.DrifterRecipes;
import net.smok.drifter.utils.BlockEntityPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.IntToLongFunction;

public class TankBlockEntity extends BlockEntity implements BotariumFluidBlock<WrappedBlockFluidContainer> {

    private final WrappedBlockFluidContainer fluidContainer;

    private final BlockEntityPosition<EnginePanelBlockEntity> engine = new BlockEntityPosition<>("engine", DrifterBlocks.ENGINE_PANEL_BLOCK_ENTITY.get());

    public TankBlockEntity(BlockPos pos, BlockState blockState) {
        super(DrifterBlocks.TANK_BLOCK_ENTITY.get(), pos, blockState);
        fluidContainer = new WrappedBlockFluidContainer(this,
                new TankFluidContainer(blockState.getBlock() instanceof TankBlock tankBlock ? tankBlock.getContainerAmount() : 0,
                        1, this::canBeAccessed)
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
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public boolean canBeAccessed(int tank, FluidHolder holder) {
        Optional<EnginePanelBlockEntity> block = engine.getBlock(level);
        return block.isEmpty() || block.get().stand() && (holder.isEmpty() || level.getRecipeManager()
                .getAllRecipesFor(DrifterRecipes.FUEL_TYPE.get()).stream().anyMatch(recipe -> recipe.fluid().test(holder)));
    }

    private static class TankFluidContainer extends SimpleFluidContainer {

        public TankFluidContainer(IntToLongFunction maxAmount, int tanks, BiPredicate<Integer, FluidHolder> fluidFilter) {
            super(maxAmount, tanks, fluidFilter);
        }

        public TankFluidContainer(long maxAmount, int tanks, BiPredicate<Integer, FluidHolder> fluidFilter) {
            super(maxAmount, tanks, fluidFilter);
        }

        @Override
        public FluidHolder internalExtract(FluidHolder fluid, boolean simulate) {
            for(int i = 0; i < this.storedFluid.size(); ++i) {
                FluidHolder toExtract = fluid.copyHolder();
                if (this.storedFluid.isEmpty()) {
                    return FluidHolder.empty();
                }

                if (this.storedFluid.get(i).matches(fluid)) {
                    long extractedAmount = (long)Mth.clamp((float)fluid.getFluidAmount(), 0.0F, (float) this.storedFluid.get(i).getFluidAmount());
                    toExtract.setAmount(extractedAmount);
                    if (simulate) {
                        return toExtract;
                    }

                    this.storedFluid.get(i).setAmount(this.storedFluid.get(i).getFluidAmount() - extractedAmount);
                    if (this.storedFluid.get(i).getFluidAmount() == 0L) {
                        this.storedFluid.set(i, FluidHolder.empty());
                    }

                    return toExtract;
                }
            }

            return FluidHolder.empty();
        }
    }

    // todo add comparator
}
