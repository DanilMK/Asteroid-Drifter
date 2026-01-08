package net.smok.drifter.blocks.alert;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.smok.drifter.network.NetworkHandler;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Hovered;
import net.smok.drifter.widgets.Sprite;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public record AlertDisplay(Alert alert, Font font) {

    public static final ResourceLocation TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/alert/alert_buttons.png");

    public static final Sprite TEXT = Sprite.of(TEXTURE, 0, 0, 100, 20, 100, 100);
    public static final Sprite PLUS = Sprite.of(TEXTURE, 0, 20, 20, 20, 100, 100);
    public static final Sprite MINUS = Sprite.of(TEXTURE, 20, 20, 20, 20, 100, 100);
    public static final Sprite TEST_OFF = Sprite.of(TEXTURE, 40, 20, 20, 20, 100, 100);
    public static final Sprite TEST_OFF_PRESS = Sprite.of(TEXTURE, 40, 40, 20, 20, 100, 100);
    public static final Sprite TEST_ON = Sprite.of(TEXTURE, 60, 20, 20, 20, 100, 100);
    public static final Sprite TEST_ON_PRESS = Sprite.of(TEXTURE, 60, 40, 20, 20, 100, 100);
    public static final Sprite ACTIVE = Sprite.of(TEXTURE, 80, 40, 20, 20, 100, 100);
    public static final Sprite ACTIVE_OFF = Sprite.of(TEXTURE, 80, 20, 20, 20, 100, 100);
    public static final Sprite BUTTON = Sprite.of(TEXTURE, 0, 60, 60, 20, 100, 100);
    public static final Sprite BLANK = Sprite.of(TEXTURE, 60, 60, 20, 20, 100, 100);
    public static final Sprite BLANK_PRESS = Sprite.of(TEXTURE, 60, 80, 20, 20, 100, 100);
    public static final Sprite EMPTY = Sprite.of(TEXTURE, 80, 80, 20, 20, 100, 100);
    public static final Sprite UP = Sprite.of(TEXTURE, 80, 60, 10, 10, 100, 100);
    public static final Sprite UP_PRESS = Sprite.of(TEXTURE, 80, 70, 10, 10, 100, 100);
    public static final Sprite DOWN = Sprite.of(TEXTURE, 90, 60, 10, 10, 100, 100);
    public static final Sprite DOWN_PRESS = Sprite.of(TEXTURE, 90, 70, 10, 10, 100, 100);

    public static final Sprite BACKGROUND = Sprite.ofName("alert/alert_system_gui.png", 196, 201);

    public void test(BlockPos blockPos, int index) {
        if (!alert.canBeTested()) return;
        alert().setTested(!alert().isTested());
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        ClientPlayNetworking.send(NetworkHandler.DETECTOR_TEST.getId(), NetworkHandler.DETECTOR_TEST.createPacket(blockPos, index, alert().isTested()).getData());
    }

    public void swap(BlockPos blockPos, int indexA, int indexB) {
        ClientPlayNetworking.send(NetworkHandler.DETECTOR_SWAP.getId(), NetworkHandler.DETECTOR_SWAP.createPacket(blockPos, indexA, indexB).getData());
    }

    public void editSound(@NotNull Minecraft minecraft, Screen parent, BlockPos blockPos, int index) {
        minecraft.setScreen(new AlertSoundEditScreen(parent, alert.getSound(), s -> {
            if (alert.canEditSound()) ClientPlayNetworking.send(NetworkHandler.DETECTOR_SOUND.getId(),
                    NetworkHandler.DETECTOR_SOUND.createPacket(blockPos, index, s).getData());
        }));
    }

    public void editName(@NotNull Minecraft minecraft, Screen parent, BlockPos blockPos, int index) {
        minecraft.setScreen(new AlertNameEditScreen(parent, alert.getName(), s -> {
            if (alert.canEditName()) ClientPlayNetworking.send(NetworkHandler.DETECTOR_NAME.getId(),
                    NetworkHandler.DETECTOR_NAME.createPacket(blockPos, index, s).getData());
        }));
    }

    public void editIcon(@NotNull Minecraft minecraft, Screen parent, BlockPos blockPos, int index) {
        minecraft.setScreen(new AlertIconEditScreen(parent, alert.getIcon(), i -> {
            if (alert.canEditIcon()) ClientPlayNetworking.send(NetworkHandler.DETECTOR_ICON.getId(),
                    NetworkHandler.DETECTOR_ICON.createPacket(blockPos, index, i).getData());
        }));
    }

    public static void renderIcon(@NotNull GuiGraphics guiGraphics, @NotNull Icon icon, int x, int y) {
        if (icon.isPaintIcon()) {
            Color color = icon.getColor();
            guiGraphics.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);
        }
        if (icon.getAsItem() != null) {
            guiGraphics.renderItem(icon.getAsItem(), x + 1, y + 1);
        } else if (icon.getAsMobEffect() != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(icon.getAsMobEffect());
            guiGraphics.blit(x, y, 0, 18, 18, sprite);
        }
        if (icon.isPaintIcon()) guiGraphics.setColor(1, 1, 1, 1);
    }

    public void renderAt(@NotNull GuiGraphics guiGraphics, int x, int y, boolean focusing, int mouseX, int mouseY) {
        BLANK.draw(guiGraphics, x, y);
        renderIcon(guiGraphics, alert().getIcon(), x + 1, y + 1);
        x += BLANK.width();
        TEXT.draw(guiGraphics, x, y);
        guiGraphics.drawString(font(), alert().text(), x + 4, y + 6, Integer.MAX_VALUE);

        x += TEXT.width();
        if (alert().isTested()) {
            if (focusing || Hovered.isHover(x, y, x + TEST_ON.width(), y + TEST_ON.height(), mouseX, mouseY))
                TEST_ON_PRESS.draw(guiGraphics, x, y);
            else TEST_ON.draw(guiGraphics, x, y);
        } else {
            if (focusing || Hovered.isHover(x, y, x + TEST_ON.width(), y + TEST_ON.height(), mouseX, mouseY))
                TEST_OFF_PRESS.draw(guiGraphics, x, y);
            else TEST_OFF.draw(guiGraphics, x, y);
        }

        x += TEST_ON.width();
        if (alert().isActive()) {
            ACTIVE.draw(guiGraphics, x, y);
        } else {
            ACTIVE_OFF.draw(guiGraphics, x, y);
        }
    }
}