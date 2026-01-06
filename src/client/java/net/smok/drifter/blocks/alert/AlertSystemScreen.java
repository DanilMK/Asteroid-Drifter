package net.smok.drifter.blocks.alert;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.smok.drifter.menus.AlertSystemMenu;
import net.smok.drifter.widgets.Sprite;
import net.smok.drifter.widgets.StringWidget;
import org.jetbrains.annotations.NotNull;


public class AlertSystemScreen extends AbstractContainerScreen<AlertSystemMenu> {

    public static final Sprite BACKGROUND_SPRITE = AlertDisplay.BACKGROUND;
    private final @NotNull AlertPanelBlockEntity alertSystemBlock;
    private AlertSelectionList list;
    private boolean initialized;


    public AlertSystemScreen(AlertSystemMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        alertSystemBlock = menu.getAlertSystemBlock();
        imageHeight = BACKGROUND_SPRITE.height();
        imageWidth = BACKGROUND_SPRITE.width();
    }

    @Override
    protected void init() {
        super.init();
        addRenderableOnly(new StringWidget(leftPos + 4, topPos + 4, imageWidth - 8, 10, font, title, StringWidget.Position.CENTER));
        if (initialized) list.updateSize(imageWidth, imageHeight, leftPos, topPos + 20, topPos + imageHeight - 36);
        else {
            list = new AlertSelectionList(minecraft, imageWidth, imageHeight, leftPos, topPos + 18, topPos + imageHeight - 36, 24, alertSystemBlock);
            list.setRenderBackground(false);
            list.setRenderTopAndBottom(false);
            list.refreshEntries();
            initialized = true;
        }
        addRenderableWidget(list);
        list.createEditButtons(minecraft, this, width / 2, topPos + imageHeight - 28).forEach(this::addRenderableWidget);


    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float deltaTime, int mouseX, int mouseY) {
        BACKGROUND_SPRITE.draw(guiGraphics, leftPos, topPos);
    }

}
