package net.smok.drifter.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class StringWidget extends AbstractWidget {

    private final Supplier<Component> stringGetter;
    private final Position position;
    private final Font font;
    private final @Nullable Sprite sprite; 

    public StringWidget(int x, int y, int width, int height, Font font, Component component, Position position) {
        this(x, y, width, height, font, () -> component, position, null);
    }

    public StringWidget(int x, int y, int width, int height, Font font, Component component, Position position, @Nullable Sprite sprite) {
        this(x, y, width, height, font, () -> component, position, sprite);
    }

    public StringWidget(int x, int y, int width, int height, Font font, Component component) {
        this(x, y, width, height, font, () -> component, Position.CENTER, null);
    }

    public StringWidget(int x, int y, int width, int height, Font font, Component component, @Nullable Sprite sprite) {
        this(x, y, width, height, font, () -> component, Position.CENTER, sprite);
    }
    
    public StringWidget(int x, int y, int width, int height, Font font, @NotNull Supplier<Component> stringGetter) {
        this(x, y, width, height, font, stringGetter, Position.CENTER, null);
    }
    
    public StringWidget(int x, int y, int width, int height, Font font, @NotNull Supplier<Component> stringGetter, Position position) {
        this(x, y, width, height, font, stringGetter, position, null);
    }
    
    public StringWidget(int x, int y, int width, int height, Font font, @NotNull Supplier<Component> stringGetter, @Nullable Sprite sprite) {
        this(x, y, width, height, font, stringGetter, null, sprite);
    }
    
    public StringWidget(int x, int y, int width, int height, Font font, @NotNull Supplier<Component> stringGetter, Position position, @Nullable Sprite sprite) {
        super(x, y, width, height, Component.empty());
        this.stringGetter = stringGetter;
        this.position = position;
        this.font = font;
        this.sprite = sprite;
    }
    

    @Override
    public @NotNull Component getMessage() {
        return stringGetter.get();
    }
    

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (sprite != null) sprite.draw(guiGraphics, getX(), getY());
        Component message = getMessage();
        final int color = message.getStyle().getColor() != null ? message.getStyle().getColor().getValue() : Integer.MAX_VALUE;

        switch (position) {
            case LEFT_UP -> guiGraphics.drawString(font, message, getX(), getY(), color);
            case LEFT -> guiGraphics.drawString(font, message, getX(), heightCenter(), color);
            case LEFT_DOWN -> guiGraphics.drawString(font, message, getX(), down(), color);

            case CENTER_UP -> guiGraphics.drawCenteredString(font, message, widthCenter(), getY(), color);
            case CENTER -> guiGraphics.drawCenteredString(font, message, widthCenter(), heightCenter(), color);
            case CENTER_DOWN -> guiGraphics.drawCenteredString(font, message, widthCenter(), down(), color);

            case RIGHT_UP -> guiGraphics.drawString(font, message, right(message), getY(), color);
            case RIGHT -> guiGraphics.drawString(font, message, right(message), heightCenter(), color);
            case RIGHT_DOWN -> guiGraphics.drawString(font, message, right(message), down(), color);
        }
    }

    private int down() {
        return getY() + height - 8;
    }

    private int widthCenter() {
        return getX() + width / 2;
    }

    private int right(Component message) {
        return getX() + width - font.width(message);
    }

    private int heightCenter() {
        return getY() + height / 2 - 4;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.HINT, getMessage());
    }
    
    public enum Position {
        LEFT_UP, LEFT, LEFT_DOWN, CENTER_UP, CENTER, CENTER_DOWN, RIGHT_UP, RIGHT, RIGHT_DOWN
    }
}
