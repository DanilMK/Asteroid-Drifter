package net.smok.drifter.blocks.alert;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.smok.drifter.menus.AlertSystemMenu;
import net.smok.drifter.registries.Values;


public class AlertSystemScreen extends AbstractContainerScreen<AlertSystemMenu> {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(Values.MOD_ID, "textures/gui/alert/alert_system_gui.png");

    public AlertSystemScreen(AlertSystemMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);

    }

    @Override
    protected void init() {
        super.init();

        for (int i = 0; i < menu.getDangers().size(); i++) {
            AlertPanelBlockEntity.Danger danger = menu.getDangers().get(i);
            addRenderableWidget(new AlertDangerWidget(leftPos + 8, topPos + 16 + i * AlertDangerWidget.HEIGHT, danger, font, menu.getBlockPos(), i));
        }

    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x2a262b, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float deltaTime, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }
}
