package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.widgets.AnimationHandler;
import net.smok.drifter.widgets.GuiUtils;
import net.smok.drifter.widgets.Hovered;

import java.util.List;

public record DistanceWidget(ShipControllerBlockEntity controller, int x, int y, int radius, boolean initDriving,
                             AnimationHandler launchAnim, AnimationHandler landAnim) implements Renderable, Hovered {



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float timeDelta) {
        if (isVisible()) render();
    }

    private boolean isVisible() {
        return controller.getRemainDistance() > 0 || (landAnim.isNotFinished() & initDriving);
    }

    private void render() {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        int sides = (radius / 4) * 4;
        double start = 0.625d;
        double end = 0.875d;

        if (launchAnim.work()) start = end - (end - start) * (launchAnim.relativeTime());
        if (landAnim.work()) start = end - (end - start) * (1 - landAnim.relativeTime());

        GuiUtils.drawArc(bufferBuilder, x, y, radius, sides,
                ShipControllerScreen.COLOR_EDGE, ShipControllerScreen.COLOR_FADE, start, end,
                1d - (double) controller.getRemainDistance() / controller.getTotalDistance());

        tessellator.end();
    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return isVisible() && Hovered.isInCircle(x, y, radius, mouseX, mouseY);
    }

    @Override
    public List<Component> content() {
        return List.of(Component.translatable("tooltip.asteroid_drifter.remain_distance", String.format("%,f", controller.getRemainDistance()), String.format("%,f", controller.getTotalDistance())));
    }
}
