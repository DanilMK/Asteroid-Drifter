package net.smok.drifter.engine;

import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.smok.drifter.blocks.engine.EnginePanelBlockEntity;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public record SpeedWidget(EnginePanelBlockEntity engine, int posX, int posY, float minRot, float maxRot, Font font) implements Renderable, Hovered {

    private static final ResourceLocation SPEEDOMETER_TEXTURE = new ResourceLocation(Values.MOD_ID, "textures/gui/engine/speedometer.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 100;
    private static final int width = 50, height = 50;

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {

        float r = Mth.lerp(engine.speed() / engine.maxSpeed(), minRot(), maxRot());

        guiGraphics.blit(SPEEDOMETER_TEXTURE, posX, posY, 0, 0, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(posX + width / 2f, posY + height / 2f -1, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(r));
        guiGraphics.blit(SPEEDOMETER_TEXTURE, -3, -4, 55, 4, 6, 24, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.pose().popPose();
        guiGraphics.blit(SPEEDOMETER_TEXTURE, posX, posY, 0, height, width, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);

    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return Hovered.isInCircle(posX + width / 2f, posY + height / 2f, width / 2f, mouseX, mouseY);
    }

    @Contract(" -> new")
    @Override
    public @NotNull @Unmodifiable List<Component> content() {
        return List.of(engine.getSpeed(), engine.getMaxSpeed());
    }
}
