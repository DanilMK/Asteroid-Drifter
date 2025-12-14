package net.smok.drifter.datagen.providers;

import earth.terrarium.adastra.common.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.smok.drifter.datagen.builders.MoonFarmRecipeBuilder;
import net.smok.drifter.registries.DrifterBlocks;
import net.smok.drifter.registries.Values;

import java.util.function.Consumer;

public class RecipeDataProvider extends RecipeProvider {


    public RecipeDataProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        MoonFarmRecipeBuilder.of(DrifterBlocks.CARROTS.get(), Ingredient.of(Items.FARMLAND),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, 81 * 250L, FluidTags.WATER),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.BONE_MEAL),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.MUD)
        ).save(writer, new ResourceLocation("carrots"));

        MoonFarmRecipeBuilder.of(DrifterBlocks.POTATOES.get(), Ingredient.of(Items.FARMLAND),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, 81 * 250L, FluidTags.WATER),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.BONE_MEAL),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.MUD)
        ).save(writer, new ResourceLocation("potatoes"));

        MoonFarmRecipeBuilder.of(DrifterBlocks.FROST_WHEAT.get(), Ingredient.of(Items.FARMLAND),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, 81 * 250L, FluidTags.WATER),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.COAL),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.BROWN_DYE)
        ).save(writer, new ResourceLocation(Values.MOD_ID, "frost_wheat"));

        MoonFarmRecipeBuilder.of(DrifterBlocks.MARTIAN_MANDRAKE.get(), Ingredient.of(Items.FARMLAND, ModItems.MARS_SAND.get()),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, 81 * 250L, FluidTags.WATER),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(Values.MOD_ID, "mandrake_foods"))),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation(Values.MOD_ID, "mandrake_foods")))
        ).save(writer, new ResourceLocation(Values.MOD_ID, "martian_mandrake"));


        registerSapling(writer, Blocks.ACACIA_SAPLING, "acacia");
        registerSapling(writer, Blocks.SPRUCE_SAPLING, "spruce");
        registerSapling(writer, Blocks.CHERRY_SAPLING, "cherry");
        registerSapling(writer, Blocks.BIRCH_SAPLING, "birch");
        registerSapling(writer, Blocks.JUNGLE_SAPLING, "jungle");
        registerSapling(writer, Blocks.OAK_SAPLING, "oak");
        registerSapling(writer, Blocks.DARK_OAK_SAPLING, "dark_oak");
    }

    private static void registerSapling(Consumer<FinishedRecipe> writer, Block sapling, String name) {
        MoonFarmRecipeBuilder.of(sapling, Ingredient.of(Items.DIRT),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, 81 * 250L, FluidTags.WATER),
                MoonFarmRecipeBuilder.nutrientOf(0.25f, 0.5f, Items.BONE_MEAL)
        ).save(writer, new ResourceLocation(name));
    }


}
