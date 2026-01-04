package net.smok.drifter.blocks.alert;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.smok.drifter.widgets.Sprite;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AlertNameEditScreen extends net.smok.drifter.widgets.EditScreen {


    private final String alertName;
    private EditBox editBox;
    private final Consumer<String> onDone;

    public AlertNameEditScreen(@Nullable Screen parent, String value, Consumer<String> onDone) {
        super(Component.translatable("tooltip.asteroid_drifter.detector_edit_name"), parent, Sprite.ofName("alert/alert_system_gui.png", 184, 201));
        this.alertName = value;
        this.onDone = onDone;

    }

    @Override
    protected void init() {
        super.init();
        editBox = addRenderableWidget(new EditBox(font, leftPos + 8, topPos + 26, imageWidth() - 16, 20, title));
        editBox.setValue(alertName);
    }

    @Override
    protected void done() {
        onDone.accept(editBox.getValue());
        super.done();
    }

}
