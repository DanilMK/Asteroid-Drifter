package net.smok.drifter.datagen.builders;

import com.teamresourceful.resourcefullib.common.datagen.FinishedCodecRecipe;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class CodecRecipeBuilder<C extends Container, T extends CodecRecipe<C>> implements RecipeBuilder {
    private final Function<ResourceLocation, T> recipeFactory;
    private Advancement.Builder advancement;

    public CodecRecipeBuilder(Function<ResourceLocation, T> recipeFactory) {
        this.recipeFactory = recipeFactory;
    }

    public CodecRecipeBuilder<C, T> addAdvancement() {
        advancement = Advancement.Builder.recipeAdvancement();
        return this;
    }

    @Override
    public @NotNull CodecRecipeBuilder<C, T> unlockedBy(String criterionName, CriterionTriggerInstance criterionTrigger) {
        if (advancement == null) advancement = Advancement.Builder.recipeAdvancement();
        this.advancement.addCriterion(criterionName, criterionTrigger);
        return this;
    }

    @Override
    public @NotNull CodecRecipeBuilder<C, T> group(@Nullable String groupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR;
    }

    @Override
    public void save(Consumer<FinishedRecipe> finishedRecipeConsumer, ResourceLocation recipeId) {

        if (advancement != null) advancement.parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .requirements(RequirementsStrategy.OR);
        finishedRecipeConsumer.accept(new FinishedCodecRecipe<>(recipeFactory.apply(recipeId), recipeId, advancement));
    }
}
