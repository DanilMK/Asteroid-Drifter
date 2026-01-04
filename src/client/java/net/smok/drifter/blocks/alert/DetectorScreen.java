package net.smok.drifter.blocks.alert;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.smok.drifter.menus.DetectorMenu;
import net.smok.drifter.network.NetworkHandler;
import net.smok.drifter.registries.Values;
import net.smok.drifter.widgets.Sprite;
import net.smok.drifter.widgets.StringWidget;
import org.jetbrains.annotations.NotNull;

public class DetectorScreen extends AbstractContainerScreen<DetectorMenu> {

    public static final Sprite BACKGROUND_SPRITE = Sprite.of(new ResourceLocation(Values.MOD_ID, "textures/gui/alert/alert_system_gui.png"), 184, 201);
    private final DetectorBlockEntity detector;
    private final Alert alert;

    public DetectorScreen(DetectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        detector = menu.getDetector();
        alert = detector.getAlert();
        imageWidth = BACKGROUND_SPRITE.width();
        imageHeight = BACKGROUND_SPRITE.height();
    }

    @Override
    protected void init() {
        super.init();

        int x = leftPos + 8;
        int y = topPos + 10;

        addRenderableWidget(new StringWidget(leftPos, y, imageWidth, 10, font, title, StringWidget.Position.CENTER));

        y += 14;

        addRenderableWidget(new AlertDisplayWidget(x, y, alert, font, this::setTest));

        y += AlertDangerWidget.HEIGHT;

        addRenderableWidget(AlertDisplayWidget.BUTTON.createButton(x, y, button -> editName(),
                Component.translatable("tooltip.asteroid_drifter.detector_edit_name")));

        addRenderableWidget(AlertDisplayWidget.BUTTON.createButton(leftPos + imageWidth - 68, y, button -> editIcon(),
                Component.translatable("tooltip.asteroid_drifter.detector_edit_icon")));

        y += 30;

        addRenderableOnly(new StringWidget(leftPos + 8, y, 60, 10, font, Component.translatable("tooltip.asteroid_drifter.detector_min"), StringWidget.Position.CENTER_DOWN));
        addRenderableOnly(new StringWidget(leftPos + imageWidth - 68, y, 60, 10, font, Component.translatable("tooltip.asteroid_drifter.detector_max"), StringWidget.Position.CENTER_DOWN));

        y += 10;

        addRenderableWidget(AlertDisplayWidget.PLUS.createButton(x, y, button -> setMinSignal(detector.getMinSignal() + 1)));

        x += 20;

        addRenderableOnly(AlertDisplayWidget.EMPTY.createStringWidget(x, y, font, StringWidget.Position.CENTER,
                () -> Component.literal(String.valueOf(detector.getMinSignal()))));

        x += 20;
        addRenderableWidget(AlertDisplayWidget.MINUS.createButton(x, y, button -> setMinSignal(detector.getMinSignal() - 1)));

        x = leftPos + imageWidth - 8 - 60;

        addRenderableWidget(AlertDisplayWidget.PLUS.createButton(x, y, button -> setMaxSignal(detector.getMaxSignal() + 1)));

        x += 20;

        addRenderableOnly(AlertDisplayWidget.EMPTY.createStringWidget(x, y, font, StringWidget.Position.CENTER,
                () -> Component.literal(String.valueOf(detector.getMaxSignal()))));

        x += 20;
        addRenderableWidget(AlertDisplayWidget.MINUS.createButton(x, y, button -> setMaxSignal(detector.getMaxSignal() - 1)));
    }

    private void editName() {
        minecraft.setScreen(new AlertNameEditScreen(this, alert.getName(), s ->
                ClientPlayNetworking.send(NetworkHandler.DETECTOR_NAME.getId(),
                        NetworkHandler.DETECTOR_NAME.createPacket(detector.getBlockPos(), 0, s).getData())));
    }

    private void editIcon() {
        minecraft.setScreen(new AlertIconEditScreen(this, alert.getIcon(), i ->
                ClientPlayNetworking.send(NetworkHandler.DETECTOR_ICON.getId(),
                        NetworkHandler.DETECTOR_ICON.createPacket(detector.getBlockPos(), 0, i).getData())));
    }

    private void setMinSignal(int minSignal) {
        detector.setMinSignal(minSignal);
        ClientPlayNetworking.send(NetworkHandler.DETECTOR_MIN_SET.getId(),
                NetworkHandler.DETECTOR_MIN_SET.createPacket(detector.getBlockPos(), detector.getMinSignal()).getData());
    }

    private void setMaxSignal(int maxSignal) {
        detector.setMaxSignal(maxSignal);
        ClientPlayNetworking.send(NetworkHandler.DETECTOR_MAX_SET.getId(),
                NetworkHandler.DETECTOR_MAX_SET.createPacket(detector.getBlockPos(), detector.getMaxSignal()).getData());
    }

    private void setTest(boolean test) {
        ClientPlayNetworking.send(NetworkHandler.DETECTOR_TEST.getId(), NetworkHandler.DETECTOR_TEST.createPacket(detector.getBlockPos(), 0, test).getData());
    }


    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        BACKGROUND_SPRITE.draw(guiGraphics, leftPos, topPos);
    }

    private int parseInt(@NotNull String s) {
        if (s.isEmpty() || s.isBlank()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
