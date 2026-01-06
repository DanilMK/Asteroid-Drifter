package net.smok.drifter.blocks.alert;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.smok.drifter.widgets.EditScreen;
import net.smok.drifter.widgets.StringWidget;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AlertIconEditScreen extends EditScreen {


    private final Icon icon;
    private final Consumer<Icon> onDone;



    public AlertIconEditScreen(@Nullable Screen parent, Icon icon, Consumer<Icon> onDone) {
        super(Component.translatable("tooltip.asteroid_drifter.detector_edit_icon"), parent, AlertDisplay.BACKGROUND);
        this.icon = new Icon(icon);
        this.onDone = onDone;
    }

    @Override
    protected void init() {
        super.init();


        int xLeft = leftPos + 8;
        int xRight = leftPos + imageWidth() - 8;
        int yTop = topPos + 20;


        EditBox idInput = addRenderableWidget(new EditBox(font, xLeft + 2, yTop, imageWidth() - 20, 20, title));
        idInput.setMaxLength(255);
        idInput.setFilter(ResourceLocation::isValidResourceLocation);
        idInput.setValue(icon.getIdAsString());
        idInput.setResponder(icon::setId);

        yTop += 25;

        addRenderableWidget(new Checkbox(xLeft, yTop, 120, 20,
                Component.translatable("tooltip.asteroid_drifter.detector_icon_paint")));

        int yIconPos = yTop;
        addRenderableOnly((guiGraphics, mouseX, mouseY, partialTick) -> {
            AlertDisplay.EMPTY.draw(guiGraphics, xRight - 20, yIconPos);
            AlertDisplay.renderIcon(guiGraphics, icon, xRight - 19, yIconPos + 1);
        });


        yTop += 25;

        addRenderableOnly(new StringWidget(xLeft, yTop, 80, 10, font,
                Component.translatable("tooltip.asteroid_drifter.detector_icon_presets"), StringWidget.Position.CENTER_DOWN));

        addRenderableOnly(new StringWidget(xRight - 80, yTop, 80, 10, font,
                Component.translatable("tooltip.asteroid_drifter.detector_icon_color"), StringWidget.Position.CENTER_DOWN));

        yTop += 15;

        // color
        for (int ix = 0; ix < 4; ix++) {
            for (int iy = 0; iy < 4; iy++) {
                int x = xRight - 80 + ix * 20;
                int y = yTop + iy * 20;
                int i = ix + iy * 4;

                addRenderableWidget(new Slot(x, y,
                        () -> icon.getColorIndex() == i,
                        (guiGraphics, integer, integer2) -> guiGraphics.renderItem(Icon.COLORS_WOOL[i], x + 2, y + 2),
                        () -> icon.setColorIndex(i)));
            }
        }

        // icon presets
        for (int ix = 0; ix < 4; ix++) {
            for (int iy = 0; iy < 4; iy++) {
                int x = xLeft + ix * 20;
                int y = yTop + iy * 20;
                int i = ix + iy * 4;

                Icon iconPreset = Icon.ICON_PRESETS[i];

                addRenderableWidget(new Slot(x, y,
                        () -> icon.equals(iconPreset),
                        (guiGraphics, integer, integer2) -> AlertDisplay.renderIcon(guiGraphics, iconPreset, x + 1, y + 1),
                        () -> {
                            idInput.setValue(iconPreset.getId().toString());
                            icon.setId(iconPreset.getId());
                            icon.setPaintIcon(iconPreset.isPaintIcon());
                            icon.setColorIndex(iconPreset.getColorIndex());
                        }));
            }
        }


    }

    @Override
    protected void done() {
        onDone.accept(icon);
        super.done();
    }


    private class Checkbox extends AbstractButton {

        public static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");

        public Checkbox(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void onPress() {
            icon.setPaintIcon(!icon.isPaintIcon());
        }

        public boolean check() {
            return icon.isPaintIcon();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
            if (this.active) {
                if (this.isFocused()) {
                    narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
                } else {
                    narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
                }
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Minecraft minecraft = Minecraft.getInstance();
            RenderSystem.enableDepthTest();
            Font font = minecraft.font;
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            guiGraphics.blit(TEXTURE, this.getX(), this.getY(), this.isFocused() ? 20.0F : 0.0F, check() ? 20.0F : 0.0F, 20, this.height, 64, 64);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            {
                guiGraphics.drawString(font, this.getMessage(), this.getX() + 24, this.getY() + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
            }
        }
    }

    private static class Slot extends AbstractWidget {

        private final Supplier<Boolean> isSelected;
        private final TriConsumer<GuiGraphics, Integer, Integer> renderItem;
        private final Runnable onClick;

        public Slot(int x, int y, Supplier<Boolean> isSelected, TriConsumer<GuiGraphics, Integer, Integer> renderItem, Runnable onClick) {
            super(x, y, 20, 20, Component.empty());
            this.isSelected = isSelected;
            this.renderItem = renderItem;
            this.onClick = onClick;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (isSelected.get()) AlertDisplay.BLANK_PRESS.draw(guiGraphics, getX(), getY());
            else AlertDisplay.BLANK.draw(guiGraphics, getX(), getY());
            renderItem.accept(guiGraphics, getX(), getY());
            if (isHovered()) {
                RenderSystem.enableBlend();
                guiGraphics.fill(getX() + 1, getY() + 1, getX() + 19, getY() + 19, 0x99FFFFFF);
                RenderSystem.disableBlend();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            onClick.run();
        }

        @Override
        public void playDownSound(SoundManager handler) {
            handler.play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BIT, 1.3348398f));
        }
    }
}
