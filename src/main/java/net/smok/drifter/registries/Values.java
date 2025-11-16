package net.smok.drifter.registries;

import com.mojang.serialization.Codec;
import com.teamresourceful.resourcefulconfig.common.config.Configurator;
import com.teamresourceful.resourcefullib.common.recipe.CodecRecipeSerializer;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.botarium.common.registry.fluid.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.smok.drifter.AlertEffect;
import net.smok.drifter.Debug;
import net.smok.drifter.DrifterConfig;
import net.smok.drifter.data.recipies.AsteroidRecipe;
import net.smok.drifter.world.AsteroidFeature;

import java.util.function.Function;

public final class Values {
    public static void init() {

        SHIP_FUEL.setBucket(DrifterItems.SHIP_FUEL_BUCKET);
        CONFIGURATOR.registerConfig(DrifterConfig.class);
        Debug.log("Asteroid Drifter Values loaded!");
    }

    public static final String MOD_ID = "asteroid_drifter";
    public static final Configurator CONFIGURATOR = new Configurator();
    public static final ResourceLocation ASTEROID_DIMENSION = new ResourceLocation(MOD_ID, "asteroids");
    public static final ResourceKey<Level> ASTEROID_LEVEL = ResourceKey.create(Registries.DIMENSION, ASTEROID_DIMENSION);

    public static final ResourcefulRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_SERIALIZER, MOD_ID);
    public static final ResourcefulRegistry<RecipeType<?>> RECIPE_TYPES = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_TYPE, MOD_ID);
    public static final FluidRegistry FLUID_PROPERTIES = new FluidRegistry(MOD_ID);
    public static final ResourcefulRegistry<Fluid> FLUIDS = ResourcefulRegistries.create(BuiltInRegistries.FLUID, MOD_ID);
    public static final ResourcefulRegistry<MobEffect> EFFECTS = ResourcefulRegistries.create(BuiltInRegistries.MOB_EFFECT, MOD_ID);
    public static final ResourcefulRegistry<Feature<?>> FEATURES = ResourcefulRegistries.create(BuiltInRegistries.FEATURE, MOD_ID);



    public static final FluidData SHIP_FUEL = FLUID_PROPERTIES.register("ship_fuel", FluidProperties.create()
            .still(new ResourceLocation("block/water_still"))
            .flowing(new ResourceLocation("block/water_flow"))
            .overlay(new ResourceLocation("block/water_overlay"))
            .screenOverlay(new ResourceLocation("textures/misc/underwater.png"))
            .viscosity(0).density(-1).disablePlacing().tintColor(0xad2bce).canConvertToSource(false));

    public static final RegistryEntry<BotariumSourceFluid> SHIP_FUEL_SOURCE = FLUIDS.register("ship_fuel",
            () -> new BotariumSourceFluid(SHIP_FUEL));
    public static final RegistryEntry<BotariumFlowingFluid> SHIP_FUEL_FLOWING = FLUIDS.register("ship_fuel_flowing",
            () -> new BotariumFlowingFluid(SHIP_FUEL));
    public static final RegistryEntry<AlertEffect> ALERT_EFFECT = EFFECTS.register("alert",
            () -> new AlertEffect(MobEffectCategory.NEUTRAL, 0xd8d8d8));


    public static final RegistryEntry<RecipeType<AsteroidRecipe>> ASTEROID_RECIPE_TYPE = registerRecipeType("asteroid");
    public static final RegistryEntry<CodecRecipeSerializer<AsteroidRecipe>> ASTEROID_RECIPE =
            registerRecipe("asteroid", ASTEROID_RECIPE_TYPE.get(), AsteroidRecipe::codec);

    public static final RegistryEntry<AsteroidFeature> ASTEROID_FEATURE =
            FEATURES.register("asteroid_feature", AsteroidFeature::new);


    private static <T extends Recipe<?>> RegistryEntry<CodecRecipeSerializer<T>> registerRecipe(String id, RecipeType<T> recipeType, Function<ResourceLocation, Codec<T>> codec) {
        return RECIPE_SERIALIZERS.register(id, () -> new CodecRecipeSerializer<>(recipeType, codec));
    }

    private static <T extends Recipe<?>> RegistryEntry<RecipeType<T>> registerRecipeType(String id) {
        return RECIPE_TYPES.register(id, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return id;
            }
        });
    }

}
