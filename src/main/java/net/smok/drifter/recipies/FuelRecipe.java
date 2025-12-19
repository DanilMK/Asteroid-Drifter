package net.smok.drifter.recipies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.QuantifiedFluidIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.registries.DrifterRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record FuelRecipe(
        ResourceLocation id,
        QuantifiedFluidIngredient fluid,
        int fuel
        ) implements CodecRecipe<EnginePanelBlockEntity> {

    public static Codec<FuelRecipe> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RecordCodecBuilder.point(id),
                QuantifiedFluidIngredient.CODEC.fieldOf("fluid").forGetter(FuelRecipe::fluid),
                Codec.INT.fieldOf("fuel_amount").forGetter(FuelRecipe::fuel)
        ).apply(instance, FuelRecipe::new));
    }

    @Override
    public boolean matches(EnginePanelBlockEntity container, Level level) {
        return fluid.test(container.getFluidHolder().getA());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return DrifterRecipes.FUEL_RECIPE.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return DrifterRecipes.FUEL_TYPE.get();
    }

    @Override
    public Codec<FuelRecipe> jsonCodec(ResourceLocation id) {
        return codec(id);
    }

    public static @NotNull Optional<FuelRecipe> findRecipe(@NotNull Level level, FluidHolder fluidHolder) {
        return level.getRecipeManager().getAllRecipesFor(DrifterRecipes.FUEL_TYPE.get()).stream()
                .filter(recipe -> recipe.fluid.test(fluidHolder)).findAny();
    }
}
