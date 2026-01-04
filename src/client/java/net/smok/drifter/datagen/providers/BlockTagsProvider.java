package net.smok.drifter.datagen.providers;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.DrifterTags;

import java.util.concurrent.CompletableFuture;

public class BlockTagsProvider extends FabricTagProvider.BlockTagProvider {


    public BlockTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE).add(
                DrifterBlocks.ALERT_LAMP.get(),
                DrifterBlocks.ALERT_PANEL_BLOCK.get(),
                DrifterBlocks.SHIP_CONTROLLER.get(),
                DrifterBlocks.ENGINE_PANEL_BLOCK.get(),
                DrifterBlocks.STEEL_TANK_BLOCK.get(),
                DrifterBlocks.DESH_TANK_BLOCK.get(),
                DrifterBlocks.OSTRUM_TANK_BLOCK.get(),
                DrifterBlocks.CALORITE_TANK_BLOCK.get(),
                DrifterBlocks.STEEL_NUZZLE_BLOCK.get(),
                DrifterBlocks.DESH_NUZZLE_BLOCK.get(),
                DrifterBlocks.OSTRUM_NUZZLE_BLOCK.get(),
                DrifterBlocks.CALORITE_NUZZLE_BLOCK.get(),
                DrifterBlocks.MOON_FARM.get(),
                DrifterBlocks.SHIP_STRUCTURE_BLOCK.get(),
                DrifterBlocks.OIL_SLUDGE.get(),
                DrifterBlocks.CRYO_SLUDGE.get(),
                DrifterBlocks.BIO_SLUDGE.get(),
                DrifterBlocks.DETECTOR_BLOCK.get()
        );

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE).add(
                DrifterBlocks.FROST_WHEAT.get(),
                DrifterBlocks.MARTIAN_MANDRAKE.get(),
                DrifterBlocks.POTATOES.get(),
                DrifterBlocks.CARROTS.get()
        );

        getOrCreateTagBuilder(BlockTags.CROPS).add(
                DrifterBlocks.FROST_WHEAT.get(),
                DrifterBlocks.MARTIAN_MANDRAKE.get(),
                DrifterBlocks.POTATOES.get(),
                DrifterBlocks.CARROTS.get()
        );

        getOrCreateTagBuilder(BlockTags.SWORD_EFFICIENT).add(
                DrifterBlocks.FROST_WHEAT.get(),
                DrifterBlocks.MARTIAN_MANDRAKE.get()
        );

        getOrCreateTagBuilder(DrifterTags.MANDRAKE_CONTAINERS).add(
                Blocks.CHEST,
                Blocks.TRAPPED_CHEST,
                Blocks.BARREL
        );

    }
}
