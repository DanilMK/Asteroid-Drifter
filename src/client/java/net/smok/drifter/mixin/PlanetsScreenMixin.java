package net.smok.drifter.mixin;

import earth.terrarium.adastra.api.planets.Planet;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.smok.drifter.Debug;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlanetsScreen.class)
public abstract class PlanetsScreenMixin {


    @Shadow private @Nullable Planet selectedPlanet;

    @Shadow protected abstract void addSpaceStationButtons(ResourceKey<Level> dimension);

    @Redirect(method = "createPlanetButtons",
            at = @At(value = "INVOKE", target = "Learth/terrarium/adastra/api/planets/Planet;isSpace()Z"),
            remap = false
    )
    private boolean createShipButton(Planet planet) {
        return !planet.dimension().location().equals(Values.ASTEROID_DIMENSION) && planet.isSpace();
    }

    @Inject(method = "createSelectedPlanetButtons", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelLandButton(CallbackInfo callbackInfo) {
        if (selectedPlanet != null && selectedPlanet.dimension().location().equals(Values.ASTEROID_DIMENSION)) {
            addSpaceStationButtons(selectedPlanet.orbitIfPresent());
            callbackInfo.cancel();
        }
    }
}
