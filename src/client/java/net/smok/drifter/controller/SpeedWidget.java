package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.ShipConfig;
import net.smok.drifter.widgets.GuiUtils;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record SpeedWidget(ShipControllerBlockEntity controller, int posX, int posY, int radius, Font font) implements Renderable, Hovered {

    private static final int MAIN_COLOR = 0xFFFFFFFF, SECOND_COLOR = 0xFF999999;


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        GuiUtils.drawCircle(bufferBuilder, posX, posY, radius, radius, MAIN_COLOR);
        GuiUtils.drawArc(bufferBuilder, posX, posY, radius, radius, MAIN_COLOR, SECOND_COLOR, 0.5, 1, (double) controller.getSpeed() / controller.maxSpeed());
        tessellator.end();

        MutableComponent time = Component.literal(ShipConfig.timeToString(controller.getRemainDistance() / controller.getSpeed()));

        guiGraphics.drawCenteredString(font, time.withStyle(ChatFormatting.WHITE), posX, posY - 9, 50);
        guiGraphics.drawCenteredString(font, Component.translatable("tooltip.asteroid_drifter.min_sec").withStyle(ChatFormatting.WHITE), posX, posY + 1, 50);


    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return Hovered.isInCircle(posX, posY, radius, mouseX, mouseY);
    }

    @Contract(" -> new")
    @Override
    public @NotNull @Unmodifiable List<Component> content() {
        return List.of(Component.translatable("tooltip.asteroid_drifter.speed_container", String.format("%,d", (int) controller.getSpeed())));
    }
}
