package net.smok.drifter.controller;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import earth.terrarium.adastra.client.components.LabeledImageButton;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.smok.drifter.blocks.controller.ShipControllerBlockEntity;
import net.smok.drifter.registries.Values;
import net.smok.drifter.blocks.controller.AsteroidSlot;
import net.smok.drifter.blocks.controller.ShipControllerMenu;
import net.smok.drifter.network.NetworkHandler;
import net.smok.drifter.widgets.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShipControllerScreen extends AbstractContainerScreen<ShipControllerMenu> {
    public static final ResourceLocation BUTTON = new ResourceLocation("ad_astra", "textures/gui/sprites/planets/button.png");



    public static final int COLOR_EDGE = 0xFFFFFFFF, COLOR_FADE = 0xFF999999, FILL_COLOR = 0x69777777;

    private final ShipControllerMenu menu;
    private final List<AsteroidSlotWidget> asteroids;

    private final List<Hovered> hoveredWidgets = new ArrayList<>();
    private final Player player;


    private AsteroidFieldWidget asteroidField;
    private FuelWidget fuelWidget;
    private DistanceWidget distanceWidget;
    private SpeedWidget speedWidget;
    private ShipWidget shipWidget;
    private ShipMoveButtonWidget leftButton, rightButton;
    private final boolean initDriving;
    private final AnimationHandler launchAnim;
    private final AnimationHandler landAnim;
    private final ShipControllerBlockEntity controller;

    public ShipControllerScreen(ShipControllerMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.menu = menu;
        controller = menu.controller();
        player = inventory.player;
        asteroids = menu.getAsteroidSlots().stream().map((AsteroidSlot slot) ->
                new AsteroidSlotWidget(controller, slot, this)).toList();

        initDriving = menu.controller().getRemainDistance() != 0;

        launchAnim = new AnimationHandler(60);
        landAnim = new AnimationHandler(60, 20);
    }

    @Override
    protected void init() {
        leftPos = 0;
        topPos = 0;

        imageWidth = width;
        imageHeight = height;


        int buttonY = 12;
        addRenderableWidget(new LabeledImageButton(10,
                buttonY, 99, 20, 0, 0, 20, BUTTON,
                99, 40, (b) -> onClose(), Component.translatable("mco.selectServer.close")));
        buttonY += 24;

        addRenderableWidget(new LabeledImageButton(10,
                buttonY, 99, 20, 0, 0, 20, BUTTON,
                99, 40, (b) -> launch(), Component.translatable("gui.asteroid_drifter.launch")));

        if (player.isCreative()) {
            buttonY += 24;
            addRenderableWidget(new LabeledImageButton(10,
                    buttonY, 99, 20, 0, 0, 20, BUTTON,
                    99, 40, (b) -> rerollAsteroids(), Component.literal("Reroll")));
            buttonY += 24;
            addRenderableWidget(new LabeledImageButton(10,
                    buttonY, 99, 20, 0, 0, 20, BUTTON,
                    99, 40, (b) -> pauseShip(), Component.literal("Pause")));
            buttonY += 24;
            addRenderableWidget(new LabeledImageButton(10,
                    buttonY, 99, 20, 0, 0, 20, BUTTON,
                    99, 40, (b) -> stopShip(), Component.literal("Stop")));
        }

        int centerX = width / 2;
        int distanceWidgetY = height * 15 / 10;
        int distanceWidgetRadius = height * 3 / 4;
        int shipWidgetY = distanceWidgetY - distanceWidgetRadius - 16;
        int shipWidgetWidth = height < 500 ? 100 : 200;



        fuelWidget = addRenderableOnly(new FuelWidget(menu.getEngineBlockEntity(), 60, height - 60, 50, font));

        distanceWidget = addRenderableOnly(new DistanceWidget(menu.controller(),
                centerX, distanceWidgetY, distanceWidgetRadius, initDriving, launchAnim, landAnim));

        asteroidField = addRenderableOnly(new AsteroidFieldWidget(width, height, centerX, height / 2,
                100, menu.controller(), asteroids, launchAnim, landAnim));

        speedWidget = addRenderableOnly(new SpeedWidget(menu.controller(),
                width - 60, height - 60, 50, font));

        shipWidget = addRenderableOnly(new ShipWidget(centerX, shipWidgetY, shipWidgetWidth,
                menu.controller(), launchAnim, landAnim, new AnimationHandler(20)));

        leftButton = addRenderableWidget(new ShipMoveButtonWidget(centerX - shipWidgetWidth / 2 - 40, shipWidgetY - 9, 18, 18, 0, 0, 0,
                new ResourceLocation(Values.MOD_ID, "textures/gui/controller/selector.png"), 18, 18, button -> moveLeft(), Component.literal("A"), launchAnim, landAnim, menu.controller()));

        rightButton = addRenderableWidget(new ShipMoveButtonWidget(centerX + shipWidgetWidth / 2 + 22, shipWidgetY - 9, 18, 18, 0, 0, 0,
                new ResourceLocation(Values.MOD_ID, "textures/gui/controller/selector.png"), 18, 18, button -> moveRight(), Component.literal("D"), launchAnim, landAnim, menu.controller()));

        asteroids.forEach(this::addWidget);

        hoveredWidgets.add(shipWidget);
        hoveredWidgets.add(leftButton);
        hoveredWidgets.add(rightButton);
        hoveredWidgets.add(fuelWidget);
        hoveredWidgets.add(distanceWidget);
        hoveredWidgets.addAll(asteroids);
        hoveredWidgets.add(speedWidget);

        if (initDriving) asteroids.get(controller.getSelectedAsteroid()).setSelected(true);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
        // No labels
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

        guiGraphics.fill(0, 0, this.width, this.height, 0xff000419);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        int i;
        for(i = -this.height; i <= this.width; i += 24) {
            bufferBuilder.vertex(i, 0.0, 0.0).color(0xff0f2559).endVertex();
            bufferBuilder.vertex(i + this.height, this.height, 0.0).color(0xff0f2559).endVertex();
        }

        for(i = this.width + this.height; i >= 0; i -= 24) {
            bufferBuilder.vertex(i, 0.0, 0.0).color(0xff0f2559).endVertex();
            bufferBuilder.vertex(i - this.height, this.height, 0.0).color(0xff0f2559).endVertex();
        }

        tessellator.end();

    }

    private void rerollAsteroids() {
        ClientPlayNetworking.send(NetworkHandler.CREATIVE_SHIP_CONTROL.getId(), NetworkHandler.CREATIVE_SHIP_CONTROL.encode(menu.getBlockPos(), 0));
    }

    private void pauseShip() {
        ClientPlayNetworking.send(NetworkHandler.CREATIVE_SHIP_CONTROL.getId(), NetworkHandler.CREATIVE_SHIP_CONTROL.encode(menu.getBlockPos(), 1));
    }

    private void stopShip() {
        ClientPlayNetworking.send(NetworkHandler.CREATIVE_SHIP_CONTROL.getId(), NetworkHandler.CREATIVE_SHIP_CONTROL.encode(menu.getBlockPos(), 2));
    }

    private void launch() {

        int selected = -1;

        for (int i = 0; i < asteroids.size(); i++) {
            if (asteroids.get(i).isFocused()) {
                selected = i;
                asteroids.get(i).setFocused(false);
                break;
            }
        }


        if (selected > 0) {
            ClientPlayNetworking.send(NetworkHandler.CONTROLLER_LAUNCH.getId(), NetworkHandler.CONTROLLER_LAUNCH.encode(menu.getBlockPos(), selected));
            distanceWidget.launchAnim().start();
            asteroids.get(selected).setSelected(true);
        } else if (controller.getRemainDistance() > 0 && controller.getSpeed() == 0) {
            ClientPlayNetworking.send(NetworkHandler.CONTROLLER_LAUNCH.getId(), NetworkHandler.CONTROLLER_LAUNCH.encode(menu.getBlockPos(), selected));
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (!menu.controller().isStand()) {
            if (minecraft.options.keyLeft.matches(i, j)) {
                moveLeft();
            } else if (minecraft.options.keyRight.matches(i, j)) {
                moveRight();
            }
        } else {
            if (minecraft.options.keyJump.matches(i, j)) {
                launch();
            }
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public @NotNull ShipControllerMenu getMenu() {
        return menu;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float deltaTime) {
        renderBackground(guiGraphics);
        boolean inFly = controller.getRemainDistance() > 0 || landAnim.work();
        leftButton.visible = inFly;
        rightButton.visible = inFly;

        if (launchAnim.work()) launchAnim.tick(deltaTime);
        if (landAnim.work()) landAnim.tick(deltaTime);
        else if (initDriving & landAnim.isNotFinished() & !landAnim.isStarted() & controller.getRemainDistance() <= 0)
        {
            landAnim.start();
            asteroids.forEach(asteroidSlotWidget -> asteroidSlotWidget.setSelected(false));
        }

        super.render(guiGraphics, mouseX, mouseY, deltaTime);




        if (getFocused() instanceof AsteroidSlotWidget slot) {
            guiGraphics.renderTooltip(font, slot.content(), Optional.empty(), slot.getX() + 8, slot.getY());
        } else {
            for (Hovered hoveredWidget : hoveredWidgets) {
                if (hoveredWidget.isHover(mouseX, mouseY)) {
                    guiGraphics.renderTooltip(font, hoveredWidget.content(), Optional.empty(), mouseX, mouseY);
                    break;
                }
            }
        }
/*

        guiGraphics.drawCenteredString(font, "launch anim: " + launchAnim.getTime() + " AllTime:" + launchAnim.allTime() + " F:" + !launchAnim.isNotFinished(), width / 2, height / 2 - 10, COLOR_EDGE);
        guiGraphics.drawCenteredString(font, "land anim: " + landAnim.getTime() + " AllTime:" + landAnim.allTime() + " F:" + !landAnim.isNotFinished(), width / 2, height / 2 + 10, COLOR_EDGE);
*/
    }

    private void moveLeft() {
        ClientPlayNetworking.send(NetworkHandler.CONTROLLER_MOVE_SHIP.getId(), NetworkHandler.CONTROLLER_MOVE_SHIP.encode(menu.getBlockPos(), -1));
    }

    private void moveRight() {
        ClientPlayNetworking.send(NetworkHandler.CONTROLLER_MOVE_SHIP.getId(), NetworkHandler.CONTROLLER_MOVE_SHIP.encode(menu.getBlockPos(), 1));
    }


    @Override
    public boolean mouseClicked(double d, double e, int i) {
        GuiEventListener prev = getFocused();
        boolean b = super.mouseClicked(d, e, i);
        GuiEventListener current = getFocused();
        if (b & prev == current) setFocused(null);

        return b;
    }

    public Minecraft getMinecraft() {
        return minecraft;
    }

}