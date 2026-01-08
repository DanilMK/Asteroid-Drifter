package net.smok.drifter.blocks.alert;

import earth.terrarium.adastra.client.components.LabeledImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.smok.drifter.widgets.Hovered;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AlertSelectionList extends AbstractSelectionList<AlertSelectionList.Entry> {

    private final Detector detector;
    private @Nullable LabeledImageButton nameEditButton, soundEditButton, iconEditButton;


    public AlertSelectionList(Minecraft minecraft, int width, int height, int x, int y0, int y1, int itemHeight, Detector detector) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.x0 = x;
        this.x1 = x + width;
        this.detector = detector;
    }

    public void updateSize(int width, int height, int x, int y0, int y1) {
        super.updateSize(width, height, y0, y1);
        this.x0 = x;
        this.x1 = x + width;
    }

    public void refreshEntries() {
        clearEntries();
        List<Alert> allAlerts = detector.getAllAlerts();
        if (allAlerts.isEmpty()) {
            addEntry(new EmptyEntry(minecraft.font));
        } else for (Alert alert : allAlerts)
            addEntry(new AlertEntry(alert, minecraft.font));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<Alert> allAlerts = detector.getAllAlerts();
        if (children().size() != allAlerts.size()) refreshEntries();
        for (int i = 0; i < children().size(); i++)
            if (children().get(i) instanceof AlertEntry entry)
                if (!entry.alertDisplay.alert().equals(allAlerts.get(i))) refreshEntries();

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public int getRowWidth() {
        return 164;
    }

    @Override
    protected int getScrollbarPosition() {
        return x1 - 14;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void setSelected(@Nullable AlertSelectionList.Entry selected) {
        super.setSelected(selected);
        if (selected instanceof AlertEntry alertEntry)
        {
            if (nameEditButton != null) nameEditButton.active = alertEntry.alertDisplay.alert().canEditName();
            if (soundEditButton != null) soundEditButton.active = alertEntry.alertDisplay.alert().canEditSound();
            if (iconEditButton != null) iconEditButton.active = alertEntry.alertDisplay.alert().canEditIcon();
        } else {
            if (nameEditButton != null) nameEditButton.active = false;
            if (soundEditButton != null) soundEditButton.active = false;
            if (iconEditButton != null) iconEditButton.active = false;
        }
    }

    public List<LabeledImageButton> createEditButtons(Minecraft minecraft, Screen parent, int centerX, int y) {

        nameEditButton = (AlertDisplay.BUTTON.createButton(centerX - 90, y, button -> {
            if (getSelected() instanceof AlertEntry selected && selected.alertDisplay.alert().canEditName()) {
                int index = children().indexOf(selected);
                selected.alertDisplay.editName(minecraft, parent, detector.getBlockPos(), index);
            }
                }, Component.translatable("tooltip.asteroid_drifter.detector_edit_name")));

        soundEditButton = (AlertDisplay.BUTTON.createButton(centerX - 30, y, button -> {
            if (getSelected() instanceof AlertEntry selected && selected.alertDisplay.alert().canEditSound()) {
                int index = children().indexOf(selected);
                selected.alertDisplay.editSound(minecraft, parent, detector.getBlockPos(), index);
            }
        }, Component.translatable("tooltip.asteroid_drifter.detector_edit_sound")));

        iconEditButton = (AlertDisplay.BUTTON.createButton(centerX + 30, y, button -> {
            if (getSelected() instanceof AlertEntry selected && selected.alertDisplay.alert().canEditIcon()) {
                int index = children().indexOf(selected);
                selected.alertDisplay.editIcon(minecraft, parent, detector.getBlockPos(), index);
            }
        }, Component.translatable("tooltip.asteroid_drifter.detector_edit_icon")));

        nameEditButton.active = false;
        soundEditButton.active = false;
        iconEditButton.active = false;
        return List.of(nameEditButton, soundEditButton, iconEditButton);
    }


    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {

    }

    public static class EmptyEntry extends Entry {

        private final Font font;

        public EmptyEntry(Font font) {
            this.font = font;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("tooltip.asteroid_drifter.empty_alerts");
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.drawCenteredString(font, getNarration(), left + width / 2, top + 8, 0xFFAAAAAA);
        }

    }


    public class AlertEntry extends Entry {

        private final AlertDisplay alertDisplay;

        public AlertEntry(Alert alert, Font font) {
            this.alertDisplay = new AlertDisplay(alert, font);
        }

        @Override
        public @NotNull Component getNarration() {
            return alertDisplay.alert().text();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {

            /*
            if (hovering) {
                RenderSystem.enableBlend();
                guiGraphics.fill(left, top, left + width, top + height, 0x55777777);
                guiGraphics.hLine(left, left + width, top, Integer.MAX_VALUE);
                guiGraphics.hLine(left, left + width, top + height, Integer.MAX_VALUE);
                guiGraphics.vLine(left, top, top + height, Integer.MAX_VALUE);
                guiGraphics.vLine(left + width, top, top + height, Integer.MAX_VALUE);
                RenderSystem.disableBlend();
            }*/

            alertDisplay.renderAt(guiGraphics, left, top, false, mouseX, mouseY);

            if (hovering) {
                if (index < detector.alertsSize() - 1) {
                    if (isDownHovered(left, top, mouseX, mouseY)) AlertDisplay.DOWN_PRESS.draw(guiGraphics, left, top + 10);
                    else AlertDisplay.DOWN.draw(guiGraphics, left, top + 10);
                }

                if (index > 0) {
                    if (isUpHovered(left, top, mouseX, mouseY)) AlertDisplay.UP_PRESS.draw(guiGraphics, left, top);
                    else AlertDisplay.UP.draw(guiGraphics, left, top);
                }
            }
        }

        private boolean isUpHovered(int x, int y, int mouseX, int mouseY) {
            return Hovered.isHover(x, y, x + 20, y + 10, mouseX, mouseY);
        }

        private boolean isDownHovered(int x, int y, int mouseX, int mouseY) {
            return Hovered.isHover(x, y + 10, x + 20, y + 20, mouseX, mouseY);
        }

        private boolean isTestHovered(int x, int y, int mouseX, int mouseY) {
            return Hovered.isHover(x + 120, y, x + 140, y + 20, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            int x = getRowLeft();
            int index = children().indexOf(this);
            int y = getRowTop(index);
            int mx = (int) mouseX;
            int my = (int) mouseY;

            if (isTestHovered(x, y, mx, my)) {
                alertDisplay.test(detector.getBlockPos(), index);
                return true;
            }

            if (index > 0 && isUpHovered(x, y, mx, my)) {
                alertDisplay.swap(detector.getBlockPos(), index, index - 1);
                return true;
            }

            if (index < detector.alertsSize() - 1 && isDownHovered(x, y, mx, my)) {
                alertDisplay.swap(detector.getBlockPos(), index, index + 1);
                return true;
            }

            return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (Screen.hasShiftDown()) {
                int index = children().indexOf(this);
                if (index == -1) {
                    return true;
                }

                if (keyCode == 264 && index < detector.alertsSize() - 1 || keyCode == 265 && index > 0) {
                    alertDisplay.swap(detector.getBlockPos(), index, keyCode == 264 ? index + 1 : index - 1);
                    refreshEntries();
                    return true;
                }
            }

            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
