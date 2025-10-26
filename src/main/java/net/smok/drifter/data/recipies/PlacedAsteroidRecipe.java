package net.smok.drifter.data.recipies;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.Values;
import net.smok.drifter.utils.FlyUtils;
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


    public List<Component> getTooltip(ShipControllerBlockEntity controller) {
        List<Component> tooltip = recipe().map(r -> new ArrayList<>(r.tooltips().stream().map(t -> (Component) Component.translatable(t)).toList())).orElse(new ArrayList<>());

        MutableComponent distance = Component.translatable("tooltip.asteroid_drifter.full_distance", String.format("%,d", distance()));
        distance.withStyle(ChatFormatting.GRAY);

        Component fuel = controller.getRequired(distance());

        String totalTime = FlyUtils.timeToString(FlyUtils.totalTime(
                controller.maxSpeed(), distance()));
        MutableComponent time = Component.translatable("tooltip.asteroid_drifter.time_required", totalTime).withStyle(ChatFormatting.GRAY);

        tooltip.add(distance);
        if (fuel != null) tooltip.add(fuel);
        tooltip.add(time);

        return tooltip;
    }
}
