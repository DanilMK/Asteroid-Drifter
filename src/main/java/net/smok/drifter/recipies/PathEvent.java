package net.smok.drifter.recipies;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.events.ShipEvent;
import net.smok.drifter.registries.ShipEventRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PathEvent(ResourceLocation shipEvent, float secondInDanger, float chance, float minPathTraveled, float maxPathTraveled) {

    public static final Codec<PathEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("event").forGetter(PathEvent::shipEvent),
        Codec.FLOAT.fieldOf("seconds_in_danger").forGetter(PathEvent::secondInDanger),
        Codec.floatRange(0, 1).fieldOf("chance").forGetter(PathEvent::chance),
        Codec.floatRange(0, 1).optionalFieldOf("min_path_traveled", 0f).forGetter(PathEvent::minPathTraveled),
        Codec.floatRange(0, 1).optionalFieldOf("max_path_traveled", 1f).forGetter(PathEvent::maxPathTraveled)
    ).apply(instance, PathEvent::new));


    public @Nullable ShipEvent getShipEvent() {
        return ShipEventRegistries.getCollision(shipEvent);
    }

    public @NotNull Component toComponent() {
        return Component.translatable(shipEvent().toLanguageKey("ship_event"))
                .withStyle(style -> style.withColor(ShipEventRegistries.getCollision(shipEvent).iconColor()))
                .append(Component.literal(": " + String.format("%,f", chance * 100) + '%').withStyle(style -> style.withColor(ChatFormatting.GRAY)));
    }
}
