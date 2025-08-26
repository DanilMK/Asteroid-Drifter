package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.inventory.DataSlot;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.widgets.AnimationHandler;
import net.smok.drifter.widgets.CircleDrawer;
import oshi.util.tuples.Pair;

import java.util.List;

import static net.smok.drifter.controller.ShipControllerScreen.COLOR_EDGE;
import static net.smok.drifter.controller.ShipControllerScreen.COLOR_FADE;

public record AsteroidFieldWidget(int width, int height, int centerX, int centerY, int radius,
                                  ShipControllerBlockEntity controller,
                                  List<AsteroidSlotWidget> asteroidSlots, AnimationHandler launchAnim,
                                  AnimationHandler landAnim) implements Renderable {


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float timeDelta) {

        if (controller.getRemainDistance() <= 0) {
            if (landAnim.work()) {
                float time = landAnim.relativeTime();
                drawCross(guiGraphics, width / 2, height / 2);
                drawCircle(time);
                drawAsteroidsByAngle(guiGraphics, time);
            } else {
                drawCircle();
                drawCross(guiGraphics, width / 2, height / 2);
                asteroidSlots.forEach(asteroidSlotWidget -> asteroidSlotWidget.render(guiGraphics, mouseX, mouseY, timeDelta));
            }
        } else if (launchAnim.work()) {
            AsteroidSlotWidget asteroid = asteroidSlots.get(controller.getSelectedAsteroid());

            int x = asteroid.getX();
            int y = asteroid.getY();
            float time = launchAnim.relativeTime() * 2;

            if (time < 1f) {
                drawCross(guiGraphics, x, y, time);
                renderItem(guiGraphics, x, y, asteroid);
            } else {
                time--;
                int x1 = (int) CircleDrawer.lerp(time, 0, 1, x, (double) width / 2);
                int y1 = (int) CircleDrawer.lerp(time, 0, 1, y, (double) height / 2);

                drawCross(guiGraphics, x1, y1);
                renderItem(guiGraphics, x1, y1, asteroid);
            }

        } else {
            int x = width / 2;
            int y = height / 2;

            drawCross(guiGraphics, x, y);
            renderItem(guiGraphics, x, y, asteroidSlots.get(controller.getSelectedAsteroid()));
        }
    }

    private void renderItem(GuiGraphics guiGraphics, int x, int y, AsteroidSlotWidget slotWidget) {
        guiGraphics.renderItem(slotWidget.getItem(), x - 8, y - 8);
    }

    private void drawCross(GuiGraphics guiGraphics, int x, int y, float size) {
        int xSize = (int) (height * size);
        int ySize = (int) (height * size);

        guiGraphics.hLine(x - xSize, x - 8, y, COLOR_FADE);
        guiGraphics.hLine(x + 8, x + 8 + xSize, y, COLOR_FADE);
        guiGraphics.vLine(x, y - ySize, y - 8, COLOR_FADE);
        guiGraphics.vLine(x, y + 8, y + 8 + ySize, COLOR_FADE);
    }

    private void drawCross(GuiGraphics guiGraphics, int x, int y) {

        guiGraphics.hLine(0, x - 8, y, COLOR_FADE);
        guiGraphics.hLine(x + 8, width, y, COLOR_FADE);
        guiGraphics.vLine(x, 0, y - 8, COLOR_FADE);
        guiGraphics.vLine(x, y + 8, height, COLOR_FADE);
    }

    private void drawCircle() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        CircleDrawer.drawSegment(bufferBuilder, width / 2f, height / 2f, radius, 100, COLOR_FADE);

        tesselator.end();
    }

    private void drawCircle(float amount) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        CircleDrawer.drawSegment(bufferBuilder, centerX, centerY, radius, 100, COLOR_FADE, COLOR_FADE, 0, amount, 1);
        Pair<Double, Double> point = CircleDrawer.pointOnCircle(amount * 100, 100, radius, centerX, centerY);

        bufferBuilder.vertex(centerX, centerY, 0.0).color(COLOR_EDGE).endVertex();
        bufferBuilder.vertex(point.getA(), point.getB(), 0.0).color(COLOR_FADE).endVertex();

        tesselator.end();
    }

    private void drawAsteroidsByAngle(GuiGraphics guiGraphics, float amount) {
        if (amount < 0.25f) {
            for (AsteroidSlotWidget asteroidSlot : asteroidSlots)
                if (asteroidSlot.getX() > centerX & asteroidSlot.getY() > centerY)
                    renderItem(guiGraphics, asteroidSlot.getX(), asteroidSlot.getY(), asteroidSlot);

        } else if (amount < 0.5f) {
            for (AsteroidSlotWidget asteroidSlot : asteroidSlots)
                if (asteroidSlot.getY() > centerY)
                    renderItem(guiGraphics, asteroidSlot.getX(), asteroidSlot.getY(), asteroidSlot);

        } else if (amount < 0.75f) {
            for (AsteroidSlotWidget asteroidSlot : asteroidSlots)
                if (asteroidSlot.getY() > centerY || asteroidSlot.getX() < centerX)
                    renderItem(guiGraphics, asteroidSlot.getX(), asteroidSlot.getY(), asteroidSlot);

        } else {
            for (AsteroidSlotWidget asteroidSlot : asteroidSlots)
                renderItem(guiGraphics, asteroidSlot.getX(), asteroidSlot.getY(), asteroidSlot);
        }
    }
}
