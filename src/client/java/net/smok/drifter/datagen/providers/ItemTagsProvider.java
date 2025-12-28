package net.smok.drifter.datagen.providers;

import earth.terrarium.adastra.common.registry.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.smok.drifter.registries.DrifterTags;

import java.util.concurrent.CompletableFuture;

public class ItemTagsProvider extends FabricTagProvider.ItemTagProvider {

    public ItemTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        getOrCreateTagBuilder(DrifterTags.MANDRAKE_FOODS).add(
                Items.ROTTEN_FLESH,
                Items.SPIDER_EYE,
                Items.BEEF,
                Items.COOKED_BEEF,
                Items.CHICKEN,
                Items.COOKED_CHICKEN,
                Items.PORKCHOP,
                Items.COOKED_PORKCHOP,
                Items.MUTTON,
                Items.COOKED_MUTTON,
                Items.RABBIT,
                Items.COOKED_RABBIT,
                Items.COD,
                Items.COOKED_COD,
                Items.SALMON,
                Items.COOKED_SALMON
        );

        getOrCreateTagBuilder(DrifterTags.LEAVES).add(
                Items.ACACIA_LEAVES,
                Items.AZALEA_LEAVES,
                Items.BIRCH_LEAVES,
                Items.CHERRY_LEAVES,
                Items.JUNGLE_LEAVES,
                Items.DARK_OAK_LEAVES,
                Items.FLOWERING_AZALEA_LEAVES,
                Items.MANGROVE_LEAVES,
                Items.OAK_LEAVES,
                Items.SPRUCE_LEAVES,
                ModItems.GLACIAN_LEAVES.get()
        );
    }
}
