package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.registries.Values;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.widgets.AnimationHandler;
import net.smok.drifter.widgets.Hovered;

import java.util.List;

public record ShipWidget(int x, int y, int width, ShipControllerBlockEntity controller, AnimationHandler launchAnim, AnimationHandler landAnim,
                         AnimationHandler dangerAnim) implements Renderable, Hovered {

    private static final ResourceLocation SHIP_TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/controller/ship.png");
    private static final ResourceLocation DANGER_TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/controller/danger.png");

    private static final int SHIP_SIZE = 24;
    private static final int DANGER_SIZE = 16;

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {

        if (isVisible()) {
            RenderSystem.enableBlend();
            float amount = launchAnim.work() ? launchAnim.relativeTime() : landAnim.work() ? landAnim.relativeTime() : 1;

            guiGraphics.blit(SHIP_TEXTURE, x - SHIP_SIZE / 2 + controller.getShipPosition() * width / 100, y - SHIP_SIZE / 2, 0, 0, SHIP_SIZE, (int) (SHIP_SIZE * amount), 24, 24);

            ShipControllerBlockEntity.Collision danger = controller.getCollision();
            if (danger != ShipControllerBlockEntity.Collision.NONE) {
                int dx = 0;
                int dy = 0;

                if (dangerAnim.relativeTime() > 0.5f) {
                    if (danger == ShipControllerBlockEntity.Collision.BIG) dx = DANGER_SIZE;
                    dy = DANGER_SIZE;
                }
                if (!ShipControllerBlockEntity.isInDanger(danger, controller.getShipPosition(), controller.getDangerPosition())) {
                    dx = DANGER_SIZE;
                    dy = 0;
                }



                guiGraphics.blit(DANGER_TEXTURE, x - DANGER_SIZE / 2 + controller.getDangerPosition() * width / 100,
                        y - DANGER_SIZE / 2 - SHIP_SIZE, dx, dy, DANGER_SIZE, (int) (DANGER_SIZE * amount), 32, 32);
            }
            RenderSystem.disableBlend();
            dangerAnim.tickLoop(deltaTime);

        }
    }

    private boolean isVisible() {
        return controller.getRemainDistance() > 0 || landAnim.work();
    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return isVisible() && Hovered.isHover(x - width / 2, y - 10, x + width / 2, y + 10, mouseX, mouseY);
    }

    @Override
    public List<Component> content() {
        return List.of(Component.translatable("tooltip.asteroid_drifter.avoidance"));
    }
}
