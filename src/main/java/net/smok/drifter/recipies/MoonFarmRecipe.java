package net.smok.drifter.recipies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.codecs.recipes.IngredientCodec;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import earth.terrarium.botarium.common.fluid.utils.FluidIngredient;
import earth.terrarium.botarium.common.fluid.utils.QuantifiedFluidIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.smok.drifter.blocks.garden.MoonFarmBlockEntity;
import net.smok.drifter.registries.DrifterRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MoonFarmRecipe(
        ResourceLocation id,
        Block cropBlock,
        Ingredient soil,
        List<Nutrient> nutrients
        ) implements CodecRecipe<MoonFarmBlockEntity> {

    public static Codec<MoonFarmRecipe> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RecordCodecBuilder.point(id),
                ResourceLocation.CODEC.fieldOf("crop").xmap(BuiltInRegistries.BLOCK::get, BuiltInRegistries.BLOCK::getKey)
                        .forGetter(MoonFarmRecipe::cropBlock),
                IngredientCodec.CODEC.fieldOf("soil").forGetter(MoonFarmRecipe::soil),
                Nutrient.CODEC.listOf().optionalFieldOf("moisture", List.of()).forGetter(MoonFarmRecipe::nutrients)
        ).apply(instance, MoonFarmRecipe::new));
    }

    @Override
    public boolean matches(MoonFarmBlockEntity container, Level level) {
        return soil.test(container.soil());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DrifterRecipes.MOON_FARMLAND_RECIPE.get();
    }

    @Override
    public Codec<MoonFarmRecipe> jsonCodec(ResourceLocation id) {
        return codec(id);
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return DrifterRecipes.MOON_FARMLAND_TYPE.get();
    }

    public float sumSpeed(MoonFarmBlockEntity blockEntity) {
        return (float) nutrients.stream().filter(nutrient ->
                blockEntity.nutrients().stream().anyMatch(nutrient.itemIngredient) &&
                blockEntity.getFluidContainer().getFluids().stream().anyMatch(nutrient.fluidIngredient)
        ).mapToDouble(Nutrient::speedAdd).sum();
    }

    public record Nutrient(Ingredient itemIngredient, QuantifiedFluidIngredient fluidIngredient, float speedAdd, float takeProbability) {
        public static final Codec<Nutrient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IngredientCodec.CODEC.optionalFieldOf("item_ingredient", Ingredient.EMPTY).forGetter(Nutrient::itemIngredient),
                QuantifiedFluidIngredient.CODEC.optionalFieldOf("fluid_ingredient", new QuantifiedFluidIngredient(FluidIngredient.of(), 0))
                        .forGetter(Nutrient::fluidIngredient),
                Codec.FLOAT.fieldOf("speed").forGetter(Nutrient::speedAdd),
                Codec.floatRange(0f, 1f).optionalFieldOf("take_probability", 1f).forGetter(Nutrient::speedAdd)
        ).apply(instance, Nutrient::new));
    }

}
