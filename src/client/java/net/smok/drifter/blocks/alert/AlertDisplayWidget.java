package net.smok.drifter.blocks.alert;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class AlertDisplayWidget extends AbstractWidget {


    private final Supplier<Boolean> shouldDrawUp, shouldDrawDown;
    private final Runnable up, down;
    private final boolean canBeSelected;
    private final AlertDisplay alertDisplay;
    private final BlockPos detectorPos;
    private final int index;

    public AlertDisplayWidget(int x, int y, Alert alert, Font font, boolean canBeSelected, Supplier<Boolean> shouldDrawUp, Supplier<Boolean> shouldDrawDown, Runnable up, Runnable down, BlockPos detectorPos, int index) {
        super(x, y, 160, 20, Component.empty());
        this.detectorPos = detectorPos;
        this.index = index;
        alertDisplay = new AlertDisplay(alert, font);
        this.shouldDrawUp = shouldDrawUp;
        this.shouldDrawDown = shouldDrawDown;
        this.up = up;
        this.down = down;
        this.canBeSelected = canBeSelected;
    }

    public AlertDisplayWidget(int x, int y, Alert alert, Font font, BlockPos detectorPos, int index) {
        this(x, y, alert, font, false, () -> false, () -> false, () -> {}, () -> {}, detectorPos, index);
    }

    @Override @Deprecated
    public void setWidth(int width) {}

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        alertDisplay.renderAt(guiGraphics, x, y, isFocused(), mouseX, mouseY);

        if (shouldDrawDown.get()) {
            if (isDownHovered(getX(), getY(), mouseX, mouseY)) AlertDisplay.DOWN_PRESS.draw(guiGraphics, getX(), y + 10);
            else AlertDisplay.DOWN.draw(guiGraphics, getX(), y + 10);
        }

        if (shouldDrawUp.get()) {
            if (isUpHovered(getX(), getY(), mouseX, mouseY)) AlertDisplay.UP_PRESS.draw(guiGraphics, getX(), y);
            else AlertDisplay.UP.draw(guiGraphics, getX(), y);
        }

        if (canBeSelected && isHoveredOrFocused()) {
            guiGraphics.hLine(getX(), getX() + getWidth(), y, Integer.MAX_VALUE);
            guiGraphics.hLine(getX(), getX() + getWidth(), y + getHeight() - 1, Integer.MAX_VALUE);
            guiGraphics.vLine(getX(), y, y + getHeight(), Integer.MAX_VALUE);
            guiGraphics.vLine(getX() + getWidth() - 1, y, y + getHeight(), Integer.MAX_VALUE);
        }

    }

    private boolean isTestHovered(int x, int y, int mouseX, int mouseY) {
        return Hovered.isHover(x + 120, y, x + 140, y + 20, mouseX, mouseY);
    }

    private boolean isUpHovered(int x, int y, int mouseX, int mouseY) {
        return Hovered.isHover(x, y, x + 20, y + 10, mouseX, mouseY);
    }

    private boolean isDownHovered(int x, int y, int mouseX, int mouseY) {
        return Hovered.isHover(x, y + 10, x + 20, y + 20, mouseX, mouseY);
    }

    @Override
    public @NotNull Component getMessage() {
        return alertDisplay.alert().text();
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return canBeSelected && super.clicked(mouseX, mouseY) || isTestHovered(getX(), getY(), (int) mouseX, (int) mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible && CommonInputs.selected(keyCode)) {
            alertDisplay.test(detectorPos, index);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        if (isTestHovered(getX(), getY(), mx, my)) alertDisplay.test(detectorPos, index);
        else if (shouldDrawUp.get() && isUpHovered(getX(), getY(), mx, my)) up.run();
        else if (shouldDrawDown.get() && isDownHovered(getX(), getY(), mx, my)) down.run();
        else select();
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

}
