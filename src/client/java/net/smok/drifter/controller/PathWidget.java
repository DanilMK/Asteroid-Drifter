package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.blocks.controller.extras.ComplexPathGenerator;
import net.smok.drifter.data.recipies.AsteroidRecipe;
import net.smok.drifter.data.recipies.Path;
import net.smok.drifter.ShipConfig;
import net.smok.drifter.widgets.AnimationHandler;
import net.smok.drifter.widgets.GuiUtils;
import net.smok.drifter.widgets.Hovered;
import net.smok.drifter.widgets.Sprite;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;


public final class PathWidget implements Renderable, Hovered, GuiEventListener, NarratableEntry {

    public static final Sprite SHIP_SPRITE = Sprite.ofName("controller/ship.png", 16, 16);
    public static final Sprite SELECTOR_SPRITE = Sprite.ofName("controller/selector.png", 18, 18);

    private final ShipControllerBlockEntity controller;
    private final Level level;
    private final AnimationHandler startAnim;
    private final AnimationHandler endAnim;
    private final int centerX, centerY;
    private final float scale;

    private Path selectedPath;
    private boolean focused;



    public PathWidget(ShipControllerBlockEntity controller, AnimationHandler startAnim, AnimationHandler endAnim, int centerX, int centerY) {
        this.controller = controller;
        this.level = controller.getLevel();
        this.startAnim = startAnim;
        this.endAnim = endAnim;
        this.centerX = centerX;
        this.centerY = centerY;
        this.scale = (centerY - 40) / 120f;
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //renderRings();
        if (controller.getRemainDistance() > 0) {
            Path recipe = controller.getSelectedRecipe();
            renderPathLine(recipe);
            renderRecipe(guiGraphics, recipe);
            renderShip(guiGraphics, recipe, controller.getRemainDistance(), controller.getTotalDistance());

        } else {
            renderSelection(guiGraphics);
            controller.getAllPaths().forEach(path -> renderRecipe(guiGraphics, path));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (controller.getRemainDistance() > 0) {
            if (isShiHovered((int) mouseX, (int) mouseY, controller.getSelectedRecipe())) {

                Minecraft.getInstance().getSoundManager()
                        .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BIT, 1.3348398f));
                selectedPath = controller.getSelectedRecipe();
                return true;
            }
            return false;
        }
        Optional<Path> first = hoveredPath((int) mouseX, (int) mouseY).min(Comparator.comparing(path -> {
            double x = mouseX - screenX(path);
            double y = mouseY - screenY(path);
            return x * x + y * y;
        }));
        if (first.isPresent()) Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BIT, 1.3348398f));

        selectedPath = first.orElse(null);
        return first.isPresent();
    }

    @Override
    public boolean appendContent(List<Component> contents, int mouseX, int mouseY) {
        if (controller.getRemainDistance() > 0) {
            Path path = controller.getSelectedRecipe();
            if (isPathHovered(mouseX, mouseY, path) || isShiHovered(mouseX, mouseY, path)) {
                path.getRecipe(level).ifPresent(recipe -> {
                    recipe.appendContent(contents);
                    contents.add(Component.translatable("tooltip.asteroid_drifter.remain_distance", ShipConfig.kmToString(controller.getRemainDistance()), ShipConfig.kmToString(controller.getTotalDistance())));
                });
                return true;
            }
            return false;
        }

        AtomicBoolean bl = new AtomicBoolean(false);
        hoveredPath(mouseX, mouseY).forEach(path -> {
            Optional<AsteroidRecipe> recipeOptional = path.getRecipe(level);
            if (recipeOptional.isPresent()) {
                AsteroidRecipe recipe = recipeOptional.get();
                if (bl.get()) contents.add(Component.empty());
                pathTooltip(contents, path, recipe);

                bl.set(true);
            }
        });
        return bl.get();
    }

    @Override
    public boolean isHover(int mouseX, int mouseY) {
        return hoveredPath(mouseX, mouseY).findAny().isPresent();
    }

    @Override
    public List<Component> content() {
        Optional<AsteroidRecipe> recipe = selectedPath.getRecipe(level);
        if (recipe.isEmpty()) return List.of();
        ArrayList<Component> contents = new ArrayList<>();
        pathTooltip(contents, selectedPath, recipe.get());
        return contents;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    private void pathTooltip(List<Component> contents, Path path, AsteroidRecipe recipe) {
        recipe.appendContent(contents);


        MutableComponent distance = Component.translatable("tooltip.asteroid_drifter.full_distance", String.format("%,d", path.distance()));
        distance.withStyle(ChatFormatting.GRAY);

        Component fuel = controller.getRequired(path.distance());

        String totalTime = ShipConfig.timeToString(path.distance() / controller.maxSpeed());
        MutableComponent time = Component.translatable("tooltip.asteroid_drifter.time_required", totalTime).withStyle(ChatFormatting.GRAY);

        contents.add(distance);
        if (fuel != null) contents.add(fuel);
        contents.add(time);
    }

    private Stream<Path> hoveredPath(int mouseX, int mouseY) {
        return controller.getAllPaths().stream().filter(path -> isPathHovered(mouseX, mouseY, path));
    }

    private boolean isPathHovered(int mouseX, int mouseY, Path path) {
        int x = screenX(path);
        int y = screenY(path);
        return Hovered.isHover(x - 9, y - 9, x + 9, y + 9, mouseX, mouseY);
    }

    private boolean isShiHovered(int mouseX, int mouseY, Path path) {
        int x = (int) Mth.lerp(1d - (double) controller.getRemainDistance() / controller.getTotalDistance(), centerX, screenX(path));
        int y = (int) Mth.lerp(1d - (double) controller.getRemainDistance() / controller.getTotalDistance(), centerY, screenY(path));
        return Hovered.isHover(x - 12, y - 12, x + 12, y + 12, mouseX, mouseY);
    }

    private void renderShip(GuiGraphics guiGraphics, Path path, float remainDist, float totalDis) {
        double x = Mth.lerp(1d - (double) remainDist / totalDis, centerX, screenX(path));
        double y = Mth.lerp(1d - (double) remainDist / totalDis, centerY, screenY(path));
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.mulPose(Axis.ZP.rotation(
                path.x() > 0 ?
                (float) (Math.atan((double) path.y() / path.x())) :
                (float) (Math.atan((double) path.y() / path.x()) + Math.PI)
        ));
        RenderSystem.enableBlend();
        SHIP_SPRITE.draw(guiGraphics, -12, -12);
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private void renderPathLine(Path path) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float x = screenX(path), y = screenY(path);
        if (startAnim.work()) {
            x = Mth.lerp(startAnim.relativeTime(), centerX, x);
            y = Mth.lerp(startAnim.relativeTime(), centerY, y);
        }

        renderDashedPathLine(bufferBuilder, centerX - 0.3f, centerY, x - 0.3f, y, 5, ShipControllerScreen.COLOR_FADE, 20);
        renderDashedPathLine(bufferBuilder, centerX, centerY - 0.5f, x, y - 0.3f, 5, ShipControllerScreen.COLOR_FADE, 20);
        renderDashedPathLine(bufferBuilder, centerX + 0.3f, centerY, x + 0.3f, y, 5, ShipControllerScreen.COLOR_FADE, 20);
        renderDashedPathLine(bufferBuilder, centerX, centerY + 0.3f, x, y + 0.3f, 5, ShipControllerScreen.COLOR_FADE, 20);
        renderDashedPathLine(bufferBuilder, centerX, centerY, x, y, 5, ShipControllerScreen.COLOR_FADE, 20);
        tesselator.end();
    }

    private static void renderDashedPathLine(BufferBuilder bufferBuilder, float startX, float startY, float endX, float endY, float dash, int color, float cycleTime) {
        double dx = endX - startX;
        double dy = endY - startY;
        double r = Math.sqrt(dx * dx + dy * dy);

        double normalX = dx * dash / r;
        double normalY = dy * dash / r;

        double shift = getTime() % cycleTime / cycleTime;

        double px = startX + shift * normalX * 2 - normalX * 0.75;
        double py = startY + shift * normalY * 2 - normalY * 0.75;



        bufferBuilder.vertex(startX, startY, 0.0).color(color).endVertex();
        int i = 0;
        for (double p = 0; p < r - dash * 2; p += dash) {
            px += normalX;
            py += normalY;
            bufferBuilder.vertex(px, py, 0.0).color(color).endVertex();
            i++;
        }
        if (i % 2 == 0) bufferBuilder.vertex(endX, endY, 0.0).color(color).endVertex();
    }

    private void renderSelection(GuiGraphics guiGraphics) {
        if (isFocused()) {
            RenderSystem.enableBlend();
            SELECTOR_SPRITE.draw(guiGraphics, screenX(selectedPath) - 9, screenY(selectedPath) - 9);
            RenderSystem.disableBlend();
        }
    }

    private void renderRecipe(GuiGraphics guiGraphics, Path path) {
        Optional<AsteroidRecipe> recipeOptional = path.getRecipe(level);
        if (recipeOptional.isEmpty()) return;
        AsteroidRecipe recipe = recipeOptional.get();
        int x = screenX(path);
        int y = screenY(path);


        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.mulPose(Axis.ZP.rotationDegrees(getTime() + path.distance()));

        ItemStack icon = recipe.icon();
        guiGraphics.renderItem(icon, -8, -8);
        pose.popPose();
    }

    private void renderRings() {
        List<Path> allPaths = controller.getAllPaths();
        int max = allPaths.stream().mapToInt(Path::ring).max().orElse(0) + 1;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float distBetweenRings = ComplexPathGenerator.MAX_PIXELS_RANGE * scale / max;
        for (int i = 1; i < max; i++) {
            GuiUtils.drawCircle(bufferBuilder, centerX, centerY, distBetweenRings * i * i, 100, 0xFF999999, 0.3);
        }
        tesselator.end();
        //Debug.log("Render circles " + max + " : " + distBetweenRings + " : " + distBetweenRings * (max + 1) * (max + 1));
    }

    private static float getTime() {
        return Util.getMillis() / 100.0F;
    }

    private int screenX(Path path) {
        return (int) (centerX + path.x() * scale);
    }

    private int screenY(Path path) {
        return (int) (centerY + path.y() * scale);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarrationPriority.FOCUSED;
        } else {
            return NarrationPriority.NONE;
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public int getSelected() {
        return controller.getAllPaths().indexOf(selectedPath);
    }

    public int selectedX() {
        return screenX(selectedPath);
    }

    public int selectedY() {
        return screenY(selectedPath);
    }
}
