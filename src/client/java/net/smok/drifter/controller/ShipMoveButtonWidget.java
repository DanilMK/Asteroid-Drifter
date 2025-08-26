package net.smok.drifter.controller;

import earth.terrarium.adastra.client.components.LabeledImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.widgets.AnimationHandler;
import net.smok.drifter.widgets.Hovered;

import java.util.List;

public class ShipMoveButtonWidget extends LabeledImageButton implements Hovered {

    private final AnimationHandler launchAnim, landAnim;
    private final ShipControllerBlockEntity controller;

    public ShipMoveButtonWidget(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex,
                                ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress,
                                Component message, AnimationHandler launchAnim, AnimationHandler landAnim, ShipControllerBlockEntity controller) {

        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress, message);
        this.launchAnim = launchAnim;
        this.landAnim = landAnim;
        this.controller = controller;
    }

    public ShipMoveButtonWidget(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex,
                                ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress,
                                AnimationHandler launchAnim, AnimationHandler landAnim, ShipControllerBlockEntity controller) {

        super(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress);
        this.launchAnim = launchAnim;
        this.landAnim = landAnim;
        this.controller = controller;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        return controller.getRemainDistance() > 0 && super.mouseClicked(d, e, i);
    }

    @Override
    public void renderTexture(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int uOffset, int vOffset,
                              int textureDifference, int width, int height, int textureWidth, int textureHeight) {

        if (launchAnim.work() || landAnim.work()) {
            float amount = launchAnim.work() ? launchAnim.relativeTime() : 1 - landAnim.relativeTime();

            super.renderTexture(guiGraphics, resourceLocation, x, y, uOffset, vOffset, textureDifference, width, (int) (height * amount), textureWidth, textureHeight);
        } else super.renderTexture(guiGraphics, resourceLocation, x, y, uOffset, vOffset, textureDifference, width, height, textureWidth, textureHeight);
    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return isActive() && Hovered.isHover(getX(), getY(), getX() + width, getY() + height, mouseX, mouseY);
    }

    @Override
    public List<Component> content() {
        return List.of(Component.translatable("tooltip.asteroid_drifter.avoidance"));
    }
}
