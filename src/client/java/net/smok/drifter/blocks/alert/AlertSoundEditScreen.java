package net.smok.drifter.blocks.alert;

import com.mojang.blaze3d.systems.RenderSystem;
import earth.terrarium.adastra.common.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.smok.drifter.widgets.EditScreen;
import net.smok.drifter.widgets.Sprite;
import net.smok.drifter.widgets.StringWidget;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AlertSoundEditScreen extends EditScreen {

    public static final List<TriConsumer<GuiGraphics, Integer, Integer>> RENDER_SLOTS = List.of(
            (guiGraphics, x, y) -> AlertDisplayWidget.renderIcon(guiGraphics, Icon.ICON_PRESETS[0], x + 1, y + 1),
            (guiGraphics, x, y) -> AlertDisplayWidget.renderIcon(guiGraphics, Icon.ICON_PRESETS[1], x + 1, y + 1),
            (guiGraphics, x, y) -> AlertDisplayWidget.renderIcon(guiGraphics, Icon.ICON_PRESETS[2], x + 1, y + 1),
            (guiGraphics, x, y) -> AlertDisplayWidget.renderIcon(guiGraphics, Icon.ICON_PRESETS[3], x + 1, y + 1),

            (guiGraphics, x, y) -> guiGraphics.renderItem(ModItems.MARS_GLOBE.get().getDefaultInstance(), x + 2, y + 3),
            (guiGraphics, x, y) -> guiGraphics.renderItem(ModItems.EARTH_GLOBE.get().getDefaultInstance(), x + 2, y + 3),
            (guiGraphics, x, y) -> guiGraphics.renderItem(ModItems.VENUS_GLOBE.get().getDefaultInstance(), x + 2, y + 3),
            (guiGraphics, x, y) -> guiGraphics.renderItem(ModItems.GLACIO_GLOBE.get().getDefaultInstance(), x + 2,  y + 3),

            (guiGraphics, x, y) -> guiGraphics.renderItem(ModItems.RED_INDUSTRIAL_LAMP.get().getDefaultInstance(), x + 2, y + 2),
            (guiGraphics, x, y) -> guiGraphics.renderItem(Items.BELL.getDefaultInstance(), x + 2, y + 2),
            (guiGraphics, x, y) -> guiGraphics.renderItem(Items.NOTE_BLOCK.getDefaultInstance(), x + 2, y + 2),
            (guiGraphics, x, y) -> guiGraphics.renderItem(Items.NOTE_BLOCK.getDefaultInstance(), x + 2, y + 2)
    );

    private final AlertSound sound;
    private final Consumer<AlertSound> onDone;
    private SimpleSoundInstance lastPlayedSound;
    private SoundManager soundManager;


    public AlertSoundEditScreen(@Nullable Screen parent, AlertSound sound, Consumer<AlertSound> onDone) {
        super(Component.translatable("tooltip.asteroid_drifter.detector_edit_sound"), parent, Sprite.ofName("alert/alert_system_gui.png", 184, 201));
        this.sound = new AlertSound(sound);
        this.onDone = onDone;
    }

    @Override
    protected void init() {
        super.init();
        soundManager = minecraft.getSoundManager();


        int xLeft = leftPos + 8;
        int xRight = leftPos + imageWidth() - 8;
        int yTop = topPos + 20;


        EditBox idInput = addRenderableWidget(new EditBox(font, xLeft, yTop, imageWidth() - 16, 20, title));
        idInput.setMaxLength(255);
        idInput.setFilter(ResourceLocation::isValidResourceLocation);
        idInput.setValue(sound.getIdAsString());
        idInput.setResponder(sound::setId);

        yTop += 25;

        addRenderableOnly(new StringWidget(xLeft, yTop, 80, 10, font,
                Component.translatable("tooltip.asteroid_drifter.detector_sound_presets"), StringWidget.Position.CENTER_DOWN));

        addRenderableOnly(new StringWidget(xRight - 80, yTop, 80, 10, font,
                Component.translatable("menu.options"), StringWidget.Position.CENTER_DOWN));

        yTop += 15;
        int y1 = yTop;


        PeriodSlider periodSlider = addRenderableWidget(new PeriodSlider(xRight, yTop));

        yTop += 30;

        PitchSlider pitchSlider = addRenderableWidget(new PitchSlider(xRight, yTop));


        addRenderableWidget(AlertDisplayWidget.BUTTON.createButton(xLeft, topPos + imageWidth() - 50, button -> {
            soundManager.stop(lastPlayedSound);
            lastPlayedSound = SimpleSoundInstance.forUI(sound.getSoundEvent(), sound.getPitch());
            soundManager.play(lastPlayedSound);
        }, Component.translatable("mco.selectServer.play")));

        addRenderableWidget(AlertDisplayWidget.BUTTON.createButton(xRight - 60, topPos + imageWidth() - 50,
                button -> soundManager.stop(lastPlayedSound), Component.translatable("gui.asteroid_drifter.stop")));


        // icon presets
        for (int ix = 0; ix < 4; ix++) {
            for (int iy = 0; iy < 3; iy++) {
                int x = xLeft + ix * 20;
                int y = y1 + iy * 20;
                int i = ix + iy * 4;

                AlertSound soundPreset = AlertSound.SOUND_PRESETS[i];

                addRenderableWidget(new Slot(x, y,
                        () -> sound.equals(soundPreset),
                        RENDER_SLOTS.get(i),
                        () -> {
                            idInput.setValue(soundPreset.getId().toString());
                            sound.setPitch(soundPreset.getPitch());
                            sound.setPeriod(soundPreset.getPeriod());
                            pitchSlider.setValue(sound.getPitch());
                            periodSlider.setValue(sound.getPeriod());
                        }, soundPreset));
            }
        }


    }

    @Override
    protected void done() {
        onDone.accept(sound);
        super.done();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }


    private class Slot extends AbstractWidget {

        private final Supplier<Boolean> isSelected;
        private final TriConsumer<GuiGraphics, Integer, Integer> renderItem;
        private final Runnable onClick;
        private final AlertSound toPlay;

        public Slot(int x, int y, Supplier<Boolean> isSelected, TriConsumer<GuiGraphics, Integer, Integer> renderItem, Runnable onClick, AlertSound toPlay) {
            super(x, y, 20, 20, Component.empty());
            this.isSelected = isSelected;
            this.renderItem = renderItem;
            this.onClick = onClick;
            this.toPlay = toPlay;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (isSelected.get()) AlertDisplayWidget.BLANK_PRESS.draw(guiGraphics, getX(), getY());
            else AlertDisplayWidget.BLANK.draw(guiGraphics, getX(), getY());
            renderItem.accept(guiGraphics, getX(), getY());
            if (isHoveredOrFocused()) {
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
            handler.stop(lastPlayedSound);
            lastPlayedSound = SimpleSoundInstance.forUI(toPlay.getSoundEvent(), toPlay.getPitch());
            handler.play(lastPlayedSound);
        }
    }

    private class PitchSlider extends AbstractSliderButton {
        public PitchSlider(int xRight, int yTop) {
            super(xRight - 80, yTop, 80, 20, AlertSoundEditScreen.this.sound.getPitchText(), AlertSoundEditScreen.this.sound.getPitch() / 2d);
        }

        @Override
        protected void updateMessage() {
            setMessage(sound.getPitchText());
        }

        @Override
        protected void applyValue() {
            sound.setPitch((float) (value * 2));
        }

        private void setValue(float value) {
            this.value = value / 2d;
            updateMessage();
        }
    }

    private class PeriodSlider extends AbstractSliderButton {
        public PeriodSlider(int xRight, int yTop) {
            super(xRight - 80, yTop, 80, 20, AlertSoundEditScreen.this.sound.getPeriodText(), (AlertSoundEditScreen.this.sound.getPeriod() - 2) / 100d);
        }

        @Override
        protected void updateMessage() {
            setMessage(sound.getPeriodText());
        }

        @Override
        protected void applyValue() {
            sound.setPeriod((int) (value * 98 + 2));
        }

        private void setValue(int value) {
            this.value = (value - 2) / 100d;
            updateMessage();
        }
    }
}
