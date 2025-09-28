package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.recipies.AsteroidRecipe;
import net.smok.drifter.recipies.PlacedAsteroidRecipe;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.AnimationHandler;
import net.smok.drifter.widgets.CircleDrawer;
import net.smok.drifter.widgets.Hovered;
import oshi.util.tuples.Pair;

import java.util.List;
import java.util.function.Consumer;

import static net.smok.drifter.controller.ShipControllerScreen.COLOR_EDGE;
import static net.smok.drifter.controller.ShipControllerScreen.COLOR_FADE;

public class FieldWidget extends AbstractWidget implements Hovered {
    public static final ResourceLocation SELECTOR = new ResourceLocation(Values.MOD_ID, "textures/gui/controller/selector.png");

    private final AnimationHandler launchAnim, landAnim;
    private final ShipControllerBlockEntity controller;
    private final int radius;
    private final float scaleFactor;
    private int selected;

    public FieldWidget(int centerX, int centerY, int screenWidth, int screenHeight, int radius,
                       AnimationHandler launchAnim, AnimationHandler landAnim, ShipControllerBlockEntity controller) {
        super(centerX, centerY, screenWidth, screenHeight, Component.empty());

        this.radius = radius;
        this.launchAnim = launchAnim;
        this.landAnim = landAnim;
        this.controller = controller;
        this.scaleFactor = Math.max(Math.min(screenHeight, screenWidth) * .4f, radius) / radius;
    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        if (controller.getRemainDistance() <= 0) {
            if (landAnim.work()) {
                float time = landAnim.relativeTime();
                drawCross(guiGraphics, width / 2, height / 2);
                drawCircle(time);
                drawAsteroidsByAngle(guiGraphics, time);
            } else {
                drawCircle();
                drawCross(guiGraphics, width / 2, height / 2);
                if (isFocused()) {
                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                    guiGraphics.blit(SELECTOR, selectedX() - 9, selectedY() - 9,
                            0, 0, 18, 18, 18, 18);
                    RenderSystem.disableBlend();
                }
                forEachAsteroid(recipe -> renderItemToScreen(guiGraphics, recipe, recipe.x(), recipe.y()));
            }
        } else if (launchAnim.work()) {
            PlacedAsteroidRecipe asteroid = controller.getSelectedRecipe();

            int x = toScreenX(asteroid.x());
            int y = toScreenY(asteroid.y());
            float time = launchAnim.relativeTime() * 2;

            if (time < 1f) {
                drawCross(guiGraphics, x, y, time);
                renderItem(guiGraphics, asteroid, x, y);
            } else {
                time--;
                int x1 = (int) CircleDrawer.lerp(time, 0, 1, x, getX());
                int y1 = (int) CircleDrawer.lerp(time, 0, 1, y, getY());

                drawCross(guiGraphics, x1, y1);
                renderItem(guiGraphics, asteroid, x1, y1);
            }

        } else {

            drawCross(guiGraphics, getX(), getY());
            renderItem(guiGraphics, controller.getSelectedRecipe(), getX(), getY());
        }
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

        CircleDrawer.drawSegment(bufferBuilder, getX(), getY(), radius, 100, COLOR_FADE, COLOR_FADE, 0, amount, 1);
        Pair<Double, Double> point = CircleDrawer.pointOnCircle(amount * 100, 100, radius, getX(), getY());

        bufferBuilder.vertex(getX(), getY(), 0.0).color(COLOR_EDGE).endVertex();
        bufferBuilder.vertex(point.getA(), point.getB(), 0.0).color(COLOR_EDGE).endVertex();

        tesselator.end();
    }

    private void drawAsteroidsByAngle(GuiGraphics guiGraphics, float amount) {
        if (amount < 0.25f) {
            forEachAsteroid(recipe -> {
                if (recipe.x() > getX() & recipe.y() > getY()) renderItemToScreen(guiGraphics, recipe, recipe.x(), recipe.y());
            });

        } else if (amount < 0.5f) {
            forEachAsteroid(recipe -> {
                if (recipe.y() > getY()) renderItemToScreen(guiGraphics, recipe, recipe.x(), recipe.y());
            });

        } else if (amount < 0.75f) {
            forEachAsteroid(recipe -> {
                if (recipe.y() > getY() || recipe.x() < getX()) renderItemToScreen(guiGraphics, recipe, recipe.x(), recipe.y());
            });
        } else {
            forEachAsteroid(recipe -> renderItemToScreen(guiGraphics, recipe, recipe.x(), recipe.y()));
        }
    }

    private void renderItemToScreen(GuiGraphics guiGraphics, PlacedAsteroidRecipe recipe, int itemX, int itemY) {
        renderItem(guiGraphics, recipe, toScreenX(itemX), toScreenY(itemY));
    }

    private static void renderItem(GuiGraphics guiGraphics, PlacedAsteroidRecipe asteroid, int screenX, int screenY) {
        guiGraphics.renderItem(asteroid.recipe().map(AsteroidRecipe::icon).orElse(ItemStack.EMPTY), screenX - 8, screenY - 8);
    }

    private void forEachAsteroid(Consumer<PlacedAsteroidRecipe> function) {
        controller.getAllRecipes().forEach(function);
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActive() && visible && controller.isStand()) {
            List<PlacedAsteroidRecipe> allRecipes = controller.getAllRecipes();
            for (int i = 0; i < allRecipes.size(); i++) {

                PlacedAsteroidRecipe recipe = allRecipes.get(i);
                if (isItemHovered(recipe.x(), recipe.y(), (int) mouseX, (int) mouseY)) {

                    if (isFocused() && selected == i) continue;
                    selected = i;

                    Minecraft.getInstance().getSoundManager()
                            .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BIT, 1.3348398f));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isActive() && visible &&
                (controller.isStand() && controller.getAllRecipes().stream().anyMatch(recipe ->
                        isItemHovered(toScreenX(recipe.x()), toScreenY(recipe.y()), (int) mouseX, (int) mouseY)) ||
                (!controller.isStand() && isCenterHover((int) mouseX, (int) mouseY)));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);

    }

    private boolean isItemHovered(int itemX, int itemY, int mouseX, int mouseY) {
        return Hovered.isHover(toScreenX(itemX) - 8, toScreenY(itemY) - 8, toScreenX(itemX) + 8, toScreenY(itemY) + 8, mouseX, mouseY);
    }

    private boolean isCenterHover(int mouseX, int mouseY) {
        return Hovered.isHover(getX() - 8, getY() - 8, getX() + 8, getY() + 8, mouseX, mouseY);
    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return false;
    }

    @Override
    public List<Component> content() {
        return getSelectedRecipe().getTooltip(controller);
    }

    @Override
    public boolean appendContent(List<Component> contents, int mouseX, int mouseY) {
        if (controller.isStand()) {
            boolean b = false;
            for (PlacedAsteroidRecipe recipe : controller.getAllRecipes()) {
                if (isItemHovered(recipe.x(), recipe.y(), mouseX, mouseY)) {

                    if (b) contents.add(Component.empty());
                    contents.addAll(recipe.getTooltip(controller));
                    b = true;
                }
            }
            return b;
        } else {
            if (isCenterHover(mouseX, mouseY)) {
                contents.addAll(controller.getSelectedRecipe().getTooltip(controller));
                return true;
            }
        }
        return false;
    }

    private PlacedAsteroidRecipe getSelectedRecipe() {
        return controller.getAllRecipes().get(selected);
    }

    private int toScreenY(int itemY) {
        return (int) (getY() + itemY * scaleFactor);
    }

    private int toScreenX(int itemX) {
        return (int) (getX() + itemX * scaleFactor);
    }

    public int getSelected() {
        return isFocused() ? selected : -1;
    }

    public int selectedX() {
        return toScreenX(getSelectedRecipe().x());
    }

    public int selectedY() {
        return toScreenY(getSelectedRecipe().y());
    }
}
