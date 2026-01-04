package net.smok.drifter.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.smok.drifter.blocks.alert.AlertDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EditScreen extends Screen {
    protected final @Nullable Screen parent;
    protected final Sprite background;
    protected int leftPos;
    protected int topPos;

    public EditScreen(Component title, @Nullable Screen parent, @NotNull Sprite background) {
        super(title);
        this.parent = parent;
        this.background = background;
    }

    public int imageWidth() {
        return background.width();
    }

    public int imageHeight() {
        return background.height();
    }

    @Override
    protected void init() {
        leftPos = (width - background.width()) / 2;
        topPos = (height - background.height()) / 2;
        createLabel();
        createDoneButton();
        createCancelButton();
    }

    protected void createLabel() {
        addRenderableOnly(new StringWidget(leftPos + 8, topPos + 8, background.width(), 10, font, title, StringWidget.Position.LEFT_DOWN));
    }

    protected void createCancelButton() {
        addRenderableWidget(AlertDisplayWidget.BUTTON.createButton(leftPos + background.width() - 8 - AlertDisplayWidget.BUTTON.width(), topPos + background.height() - 26, button -> this.cancel(), Component.translatable("gui.cancel")));
    }

    protected void createDoneButton() {
        addRenderableWidget(AlertDisplayWidget.BUTTON.createButton(leftPos + 8, topPos + background.height() - 26, button -> this.done(), Component.translatable("gui.done")));
    }


    protected void cancel() {
        back();
    }

    protected void done() {
        back();
    }

    protected void back() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        background.draw(guiGraphics, leftPos, topPos);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
