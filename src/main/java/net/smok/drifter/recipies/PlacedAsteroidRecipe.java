package net.smok.drifter.recipies;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.drifter.Debug;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class PlacedAsteroidRecipe {
    public static final PlacedAsteroidRecipe EMPTY = new PlacedAsteroidRecipe(new ResourceLocation("empty"), 0, 0, 0);
    private final @NotNull ResourceLocation recipeId;
    private final int x;
    private final int y;
    private final int distance;
    private @Nullable AsteroidRecipe recipe;

    public PlacedAsteroidRecipe(@NotNull ResourceLocation recipeId, int x, int y, int distance) {
        this.recipeId = recipeId;
        this.x = x;
        this.y = y;
        this.distance = distance;
    }

    public void setRecipe(@NotNull Level level) {
        if (recipe == null || !recipe.id().equals(recipeId))
        {
            List<AsteroidRecipe> allRecipesFor = level.getRecipeManager().getAllRecipesFor(Values.ASTEROID_RECIPE_TYPE.get());
            recipe = allRecipesFor
                    .stream().filter(recipe -> recipe.id().equals(recipeId)).findFirst().orElse(null);
        }
    }

    public Optional<AsteroidRecipe> recipe() {
        return Optional.ofNullable(recipe);
    }

    public @NotNull CompoundTag saveData() {
        CompoundTag tag = new CompoundTag();

        tag.putString("recipe", recipeId().toString());
        tag.putInt("x", x);
        tag.putInt("y", y);
        tag.putInt("distance", distance);

        return tag;
    }

    public static @NotNull PlacedAsteroidRecipe loadData(@NotNull CompoundTag tag) {
        if (!tag.contains("recipe", CompoundTag.TAG_STRING) || !tag.contains("x", CompoundTag.TAG_INT)
                || !tag.contains("y", CompoundTag.TAG_INT) || !tag.contains("distance", CompoundTag.TAG_INT)) {
            return EMPTY;
        }

        ResourceLocation id = new ResourceLocation(tag.getString("recipe"));
        int x = tag.getInt("x");
        int y = tag.getInt("y");
        int distance = tag.getInt("distance");

        return new PlacedAsteroidRecipe(id, x, y, distance);

    }

    public @NotNull ResourceLocation recipeId() {
        return recipeId;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int distance() {
        return distance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PlacedAsteroidRecipe) obj;
        return Objects.equals(this.recipeId, that.recipeId) &&
                this.x == that.x &&
                this.y == that.y &&
                this.distance == that.distance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeId, x, y, distance);
    }

    @Override
    public String toString() {
        return "PlacedAsteroidRecipe[" +
                "recipeId=" + recipeId + ", " +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "distance=" + distance + ']';
    }

}
