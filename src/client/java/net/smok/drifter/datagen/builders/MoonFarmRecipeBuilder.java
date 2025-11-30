package net.smok.drifter.datagen.builders;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.FluidIngredient;
import earth.terrarium.botarium.common.fluid.utils.QuantifiedFluidIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.smok.drifter.blocks.garden.MoonFarmBlockEntity;
import net.smok.drifter.data.recipies.MoonFarmRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class MoonFarmRecipeBuilder extends CodecRecipeBuilder<MoonFarmBlockEntity, MoonFarmRecipe> {

    public MoonFarmRecipeBuilder(Function<ResourceLocation, MoonFarmRecipe> recipeFactory) {
        super(recipeFactory);
    }

    @Override
    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
        super.save(finishedRecipeConsumer, new ResourceLocation(recipeId.getNamespace(), "moon_farm/" + recipeId.getPath()));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull MoonFarmRecipeBuilder of(Block crop, Ingredient soil,
                                                    List<MoonFarmRecipe.Nutrient> nutrients) {
        return new MoonFarmRecipeBuilder(id ->
                new MoonFarmRecipe(id, crop, soil, nutrients));
    }

    @Contract("_, _, _ -> new")
    public static @NotNull MoonFarmRecipeBuilder of(Block crop, Ingredient soil,
                                                    MoonFarmRecipe.Nutrient... nutrients) {
        return new MoonFarmRecipeBuilder(id ->
                new MoonFarmRecipe(id, crop, soil, List.of(nutrients)));
    }


    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, ItemLike... itemIngredients) {
        return nutrientOf(speedAdd, takeProbability, Ingredient.of(itemIngredients));
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, ItemStack... itemIngredients) {
        return nutrientOf(speedAdd, takeProbability, Ingredient.of(itemIngredients));
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, Stream<ItemStack> itemIngredients) {
        return nutrientOf(speedAdd, takeProbability, Ingredient.of(itemIngredients));
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, TagKey<Item> itemIngredients) {
        return nutrientOf(speedAdd, takeProbability, Ingredient.of(itemIngredients));
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, Ingredient itemIngredient) {
        return new MoonFarmRecipe.Nutrient(itemIngredient, new QuantifiedFluidIngredient(FluidIngredient.of(), 0), speedAdd, takeProbability);
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, long fluidAmount, TagKey<Fluid> fluidIngredients) {
        return new MoonFarmRecipe.Nutrient(Ingredient.of(), new QuantifiedFluidIngredient(FluidIngredient.of(fluidIngredients), fluidAmount), speedAdd, takeProbability);
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, long fluidAmount, FluidHolder... fluidIngredients) {
        return new MoonFarmRecipe.Nutrient(Ingredient.of(), new QuantifiedFluidIngredient(FluidIngredient.of(fluidIngredients), fluidAmount), speedAdd, takeProbability);
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, long fluidAmount, Fluid... fluidIngredients) {
        return new MoonFarmRecipe.Nutrient(Ingredient.of(), new QuantifiedFluidIngredient(FluidIngredient.of(fluidIngredients), fluidAmount), speedAdd, takeProbability);
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, long fluidAmount, FluidIngredient fluidIngredient) {
        return new MoonFarmRecipe.Nutrient(Ingredient.of(), new QuantifiedFluidIngredient(fluidIngredient, fluidAmount), speedAdd, takeProbability);
    }

    public static MoonFarmRecipe.Nutrient nutrientOf(float speedAdd, float takeProbability, QuantifiedFluidIngredient fluidIngredient) {
        return new MoonFarmRecipe.Nutrient(Ingredient.of(), fluidIngredient, speedAdd, takeProbability);
    }
}
