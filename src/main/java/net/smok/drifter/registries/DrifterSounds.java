package net.smok.drifter.registries;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class DrifterSounds {

    public static final ResourcefulRegistry<SoundEvent> SOUNDS = ResourcefulRegistries.create(BuiltInRegistries.SOUND_EVENT, Values.MOD_ID);


    public static final RegistryEntry<SoundEvent> SHIP_ALARM = register("ship_alarm");
    public static final RegistryEntry<SoundEvent> HARD_ALARM = register("hard_alarm");
    public static final RegistryEntry<SoundEvent> BIOHAZARD_ALARM = register("biohazard_alarm");
    public static final RegistryEntry<SoundEvent> FIRE_ALARM = register("fire_alarm");

    public static final RegistryEntry<SoundEvent> MARS_ALARM = register("mars_alarm");
    public static final RegistryEntry<SoundEvent> EARTH_ALARM = register("earth_alarm");
    public static final RegistryEntry<SoundEvent> VENUS_ALARM = register("venus_alarm");
    public static final RegistryEntry<SoundEvent> SIREN_ALARM = register("siren_alarm");

    public static final RegistryEntry<SoundEvent> GALAXY_POLICE_ALARM = register("galaxy_police_alarm");



    private static RegistryEntry<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Values.MOD_ID, name)));
    }
}
