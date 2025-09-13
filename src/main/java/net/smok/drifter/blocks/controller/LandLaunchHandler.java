package net.smok.drifter.blocks.controller;

import net.minecraft.server.level.ServerLevel;
import net.smok.drifter.recipies.AsteroidRecipe;

public interface LandLaunchHandler {
    void placeOnLand(ServerLevel serverLevel, AsteroidRecipe recipe);
    void destroyOnLaunch(ServerLevel serverLevel);
    void startDestroy();
}
