package net.smok.drifter.blocks.engine;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.smok.drifter.menus.EngineMenu;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Hovered;
import net.smok.drifter.widgets.RotateArrowWidget;

import java.util.ArrayList;
import java.util.List;

public class EngineScreen extends AbstractContainerScreen<EngineMenu> {

    private final List<Hovered> hovers = new ArrayList<>();

    public EngineScreen(EngineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 177;
        imageHeight = 168;
        inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        hovers.clear();
        FluidWidget fluidWidget = addRenderableWidget(new FluidWidget(leftPos + 61, topPos + 22, menu.getEnginePanelBlock()));

        // Slag effect
        addRenderableOnly(new RotateArrowWidget(
                leftPos + 133, topPos + 43, 45, 315,
                new ResourceLocation(Values.MOD_ID, "textures/gui/gray_arrow_24x6.png"),
                3, 3, 6, 24,
                () -> menu.getEnginePanelBlock().maxLimitedSpeed() / menu.getEnginePanelBlock().maxSpeed()));

        // Current speed
        addRenderableOnly(new RotateArrowWidget(
                leftPos + 133, topPos + 43, 45, 315,
                new ResourceLocation(Values.MOD_ID, "textures/gui/red_arrow_24x6.png"),
                3, 3, 6, 24,
                () -> menu.getEnginePanelBlock().speed() / menu.getEnginePanelBlock().maxSpeed()));




        hovers.add(new Hovered() {
            @Override
            public boolean isHover(int mouseX, int mouseY) {
                return Hovered.isInCircle(leftPos + 133, topPos + 44, 24, mouseX, mouseY);
            }

            @Override
            public List<Component> content() {
                return List.of(menu.getEnginePanelBlock().getSpeed(), menu.getEnginePanelBlock().getMaxSpeed());
            }
        });

        hovers.add(fluidWidget);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x2a262b, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x2a262b, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        RenderSystem.enableBlend();
        guiGraphics.blit(new ResourceLocation(Values.MOD_ID, "textures/gui/engine/engine_background.png"),
                leftPos+108, topPos+15, 177, 0, 50, 50, 228, 184);

        renderTooltip(guiGraphics, mouseX, mouseY);
        Hovered.renderHover(guiGraphics, font, mouseX, mouseY, false, hovers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        guiGraphics.blit(new ResourceLocation(Values.MOD_ID, "textures/gui/engine/engine_background.png"),
                leftPos, topPos, 0, 0, 177, 168, 228, 168);
        RenderSystem.disableBlend();
    }
}
