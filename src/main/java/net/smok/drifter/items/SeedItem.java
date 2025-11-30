package net.smok.drifter.items;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SeedItem extends ItemNameBlockItem {

    public final ResourceLocation secondCropBlock;

    public SeedItem(Block originalBlock, Properties properties, ResourceLocation secondCropBlock) {
        super(originalBlock, properties);
        this.secondCropBlock = secondCropBlock;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState secondState = BuiltInRegistries.BLOCK.get(secondCropBlock).getStateForPlacement(context);
        if (secondState != null && !secondState.isAir() && canPlace(context, secondState)) return secondState;

        return super.getPlacementState(context);
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        BuiltInRegistries.BLOCK.getOptional(secondCropBlock).ifPresent(block -> blockToItemMap.put(block, item));
    }
}
