package net.smok.drifter.widgets;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.*;

public class SliderButton extends AbstractSliderButton {

    private final Supplier<Component> message;
    private final DoubleConsumer resultConsumer;

    public SliderButton(int x, int y, int width, int height, double value, Supplier<Component> message1, DoubleConsumer resultConsumer) {
        super(x, y, width, height, message1.get(), value);
        this.message = message1;
        this.resultConsumer = resultConsumer;
    }

    @Override
    protected void updateMessage() {
        setMessage(message.get());
    }

    @Override
    protected void applyValue() {
        resultConsumer.accept(value);
    }

    public void setValue(double value) {
        this.value = Mth.clamp(value, 0, 1);
        updateMessage();
    }
}
