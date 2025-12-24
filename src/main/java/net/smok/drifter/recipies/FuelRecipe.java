package net.smok.drifter.recipies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import earth.terrarium.botarium.common.fluid.utils.FluidIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.registries.DrifterRecipes;
import org.jetbrains.annotations.NotNull;

public record FuelRecipe(
        ResourceLocation id,
        FluidIngredient fluid,
        BlockStateProvider slugBlock,
        float consumption, // each 1000 km cost of fuel, default = 2
        float slagChance // amount of slagChance in each 1000 mb of fuel fluid, default = 0.03
        ) implements CodecRecipe<EnginePanelBlockEntity> {

    public static Codec<FuelRecipe> codec(ResourceLocation id) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RecordCodecBuilder.point(id),
                FluidIngredient.CODEC.fieldOf("fluid").forGetter(FuelRecipe::fluid),
                BlockStateProvider.CODEC.optionalFieldOf("slag_block", BlockStateProvider.simple(Blocks.COAL_BLOCK)).forGetter(FuelRecipe::slugBlock),
                Codec.FLOAT.fieldOf("consumption").forGetter(FuelRecipe::consumption),
                Codec.FLOAT.fieldOf("slagChance").forGetter(FuelRecipe::slagChance)
        ).apply(instance, FuelRecipe::new));
    }

    public float kmToMb(float km) {
        return km * consumption / 1000;
    }

    public float mbToKm(float mb) {
        return mb * 1000 / consumption;
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

}
