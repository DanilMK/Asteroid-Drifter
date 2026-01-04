package net.smok.drifter.blocks.alert;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Hovered;
import net.smok.drifter.widgets.Sprite;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AlertDisplayWidget extends AbstractWidget {


    private static final ResourceLocation TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/alert/alert_buttons.png");

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
    public static final Sprite DOWN = Sprite.of(TEXTURE, 90, 0, 60, 10, 100, 100);
    public static final Sprite DOWN_PRESS = Sprite.of(TEXTURE, 90, 70, 10, 10, 100, 100);

    private final Alert alert;
    private final Consumer<Boolean> onTestClick;
    private final Supplier<Boolean> shouldDrawUp, shouldDrawDown;
    private final Runnable up, down;
    private final Font font;
    private final boolean canBeSelected;

    public AlertDisplayWidget(int x, int y, Alert alert, Font font, Consumer<Boolean> onTestClick, boolean canBeSelected, Supplier<Boolean> shouldDrawUp, Supplier<Boolean> shouldDrawDown, Runnable up, Runnable down) {
        super(x, y, 160, 20, Component.empty());
        this.alert = alert;
        this.onTestClick = onTestClick;
        this.shouldDrawUp = shouldDrawUp;
        this.shouldDrawDown = shouldDrawDown;
        this.up = up;
        this.down = down;
        this.font = font;
        this.canBeSelected = canBeSelected;
    }

    public AlertDisplayWidget(int x, int y, Alert alert, Font font, Consumer<Boolean> onTestClick) {
        this(x, y, alert, font, onTestClick, false, () -> false, () -> false, () -> {}, () -> {});
    }

    @Override @Deprecated
    public void setWidth(int width) {}

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        BLANK.draw(guiGraphics, x, getY());
        renderIcon(guiGraphics, alert.getIcon(), x + 1, getY() + 1);
        x += BLANK.width();
        TEXT.draw(guiGraphics, x, getY());
        renderScrollingString(guiGraphics, font, getMessage(), x, getY(), x + TEXT.width(), getY() + getHeight(), Integer.MAX_VALUE);

        x += TEXT.width();
        if (alert.isTested()) {
            if (isTestHovered(mouseX, mouseY) || (isFocused() && !canBeSelected)) TEST_ON_PRESS.draw(guiGraphics, x, getY());
            else TEST_ON.draw(guiGraphics, x, getY());
        } else {
            if (isTestHovered(mouseX, mouseY) || (isFocused() && !canBeSelected)) TEST_OFF_PRESS.draw(guiGraphics, x, getY());
            else TEST_OFF.draw(guiGraphics, x, getY());
        }

        x += TEST_ON.width();
        if (alert.isActive()) {
            ACTIVE.draw(guiGraphics, x, getY());
        } else {
            ACTIVE_OFF.draw(guiGraphics, x, getY());
        }

        if (shouldDrawDown.get()) {
            if (isDownHovered(mouseX, mouseY)) DOWN_PRESS.draw(guiGraphics, getX(), getY() + 10);
            else DOWN.draw(guiGraphics, getX(), getY() + 10);
        }

        if (shouldDrawUp.get()) {
            if (isUpHovered(mouseX, mouseY)) UP_PRESS.draw(guiGraphics, getX(), getY());
            else UP.draw(guiGraphics, getX(), getY());
        }

        if (canBeSelected && isHoveredOrFocused()) {
            guiGraphics.hLine(getX(), getX() + getWidth(), getY(), Integer.MAX_VALUE);
            guiGraphics.hLine(getX(), getX() + getWidth(), getY() + getHeight() - 1, Integer.MAX_VALUE);
            guiGraphics.vLine(getX(), getY(), getY() + getHeight(), Integer.MAX_VALUE);
            guiGraphics.vLine(getX() + getWidth() - 1, getY(), getY() + getHeight(), Integer.MAX_VALUE);
        }

    }

    private boolean isTestHovered(int mouseX, int mouseY) {
        return Hovered.isHover(getX() + 120, getY(), getX() + 140, getY() + 20, mouseX, mouseY);
    }

    private boolean isUpHovered(int mouseX, int mouseY) {
        return Hovered.isHover(getX(), getY(), getX() + 20, getY() + 10, mouseX, mouseY);
    }

    private boolean isDownHovered(int mouseX, int mouseY) {
        return Hovered.isHover(getX(), getY() + 10, getX() + 20, getY() + 20, mouseX, mouseY);
    }

    @Override
    public @NotNull Component getMessage() {
        return alert.text();
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return canBeSelected && super.clicked(mouseX, mouseY) || isTestHovered((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible && CommonInputs.selected(keyCode)) {
            test();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (isTestHovered(mx, my)) test();
        else if (shouldDrawUp.get() && isUpHovered(mx, my)) up.run();
        else if (shouldDrawDown.get() && isDownHovered(mx, my)) down.run();
        else select();
    }

    private void test() {
        alert.setTested(!alert.isTested());
        onTestClick.accept(alert.isTested());
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void select() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BIT, 1.3348398f));
    }

    @Override
    public void playDownSound(SoundManager handler) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.HINT, getMessage());
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
}
