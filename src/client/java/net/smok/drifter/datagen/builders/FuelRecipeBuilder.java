package net.smok.drifter.datagen.builders;

import earth.terrarium.botarium.common.fluid.utils.QuantifiedFluidIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.recipies.FuelRecipe;

import java.util.function.Consumer;
import java.util.function.Function;

public class FuelRecipeBuilder extends CodecRecipeBuilder<EnginePanelBlockEntity, FuelRecipe> {
    public FuelRecipeBuilder(Function<ResourceLocation, FuelRecipe> recipeFactory) {
        super(recipeFactory);
    }


    @Override
    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {
        super.save(finishedRecipeConsumer, new ResourceLocation(recipeId.getNamespace(), "fuel/" + recipeId.getPath()));
    }

    public static FuelRecipeBuilder of(QuantifiedFluidIngredient fuelFluid, int fuelAmount) {
        return new FuelRecipeBuilder(id -> new FuelRecipe(id, fuelFluid, fuelAmount));
    }
}
