package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.DataSlot;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record SimpleFuelWidget(DataSlot value, DataSlot max, DataSlot fuelEfficiency, int posX, int posY, int radius, Font font) implements Renderable, Hovered {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/controller/fuel_container.png");


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {
        float amount = 1f - (float) value.get() / max.get();
        int texAmount = (int) (amount * 100);
        int diameter = radius * 2;
        int screenAmount = (int) (amount * diameter);

        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, posX - radius, posY - radius, 0, 0, diameter, diameter, 200, 100);
        guiGraphics.blit(TEXTURE, posX - radius, posY + screenAmount - radius, 100, texAmount, 100, 100 - texAmount, 200, 100);
        RenderSystem.disableBlend();

        guiGraphics.drawCenteredString(font, Component.literal(value.get() + "/" + max.get()).withStyle(ChatFormatting.WHITE), posX, posY - 9, 50);
        guiGraphics.drawCenteredString(font, Component.translatable("tooltip.asteroid_drifter.milli_buckets").withStyle(ChatFormatting.WHITE), posX, posY + 1, 50);

    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return Hovered.isInCircle(posX, posY, radius, mouseX, mouseY);
    }

    @Contract(" -> new")
    @Override
    public @NotNull @Unmodifiable List<Component> content() {
        return List.of(Component.empty());
    }
}
