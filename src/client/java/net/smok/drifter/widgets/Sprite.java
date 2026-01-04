package net.smok.drifter.widgets;

import earth.terrarium.adastra.client.components.LabeledImageButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.registries.Values;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record Sprite(ResourceLocation textureId, int u, int v, int width, int height, int tWidth, int tHeight) {
    public static final String GUI_FOLDER = "textures/gui/";
    

    @Contract("_, _, _, _, _, _, _ -> new")
    public static @NotNull Sprite ofName(String nameInFolder, int u, int v, int width, int height, int tWidth, int tHeight) {
        return of(makeTextureId(nameInFolder), u, v, width, height, tWidth, tHeight);
    }

    @Contract("_, _, _, _, _, _, _ -> new")
    public static @NotNull Sprite of(ResourceLocation textureId, int u, int v, int width, int height, int tWidth, int tHeight) {
        return new Sprite(textureId, u, v, width, height, tWidth, tHeight);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull Sprite ofName(String nameInFolder, int u, int v, int width, int height) {
        return of(makeTextureId(nameInFolder), u, v, width, height);
    }

    @Contract("_, _, _, _, _ -> new")
    public static @NotNull Sprite of(ResourceLocation textureId, int u, int v, int width, int height) {
        return new Sprite(textureId, u, v, width, height, width, height);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Sprite ofName(String nameInFolder, int width, int height) {
        return of(makeTextureId(nameInFolder), width, height);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Sprite of(ResourceLocation textureId, int width, int height) {
        return new Sprite(textureId, 0, 0, width, height, width, height);
    }

    private static @NotNull ResourceLocation makeTextureId(String nameInFolder) {
        return new ResourceLocation(Values.MOD_ID, GUI_FOLDER + nameInFolder);
    }

    public void draw(@NotNull GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(textureId, x, y, u, v, width, height, tWidth, tHeight);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public @NotNull LabeledImageButton createButton(int x, int y, Button.OnPress onpress) {
        return new LabeledImageButton(x, y, width, height, u, v, height, textureId, tWidth, tHeight, onpress);
    }

    @Contract(value = "_, _, _, _ -> new", pure = true)
    public @NotNull LabeledImageButton createButton(int x, int y, Button.OnPress onpress, Component message) {
        return new LabeledImageButton(x, y, width, height, u, v, height, textureId, tWidth, tHeight, onpress, message);
    }

    @Contract("_, _, _, _ -> new")
    public @NotNull StringWidget createStringWidget(int x, int y, Font font, Supplier<Component> stringGetter) {
        return new StringWidget(x, y, width, height, font, stringGetter, this);
    }

    @Contract("_, _, _, _, _ -> new")
    public @NotNull StringWidget createStringWidget(int x, int y, Font font, StringWidget.Position position, Supplier<Component> stringGetter) {
        return new StringWidget(x, y, width, height, font, stringGetter, position, this);
    }
}
