package net.smok.drifter.widgets;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public record RotateArrowWidget(int posX, int posY, float minRot, float maxRot, ResourceLocation arrowTexture,
                                int offsetX, int offsetY, int textWidth, int textHeight, Supplier<Float> value)
        implements Renderable {


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {

        float r = Mth.lerp(value.get(), minRot(), maxRot());

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(posX, posY, 0);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(r));
        guiGraphics.blit(arrowTexture, -offsetX, -offsetY, 0, 0, textWidth, textHeight, textWidth, textHeight);
        guiGraphics.pose().popPose();

    }
}
