package online.kbpf.dg_lab.client.screen.WaveformScreen.Custom;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import online.kbpf.dg_lab.client.screen.SliderWidget;

/**
 * Thin wrapper around {@link SliderWidget} that exposes helpers for working with percentage based values.
 */
public abstract class CustomSliderWidget extends SliderWidget {

    protected CustomSliderWidget(int x, int y, int width, int height, Component message, double value) {
        super(x, y, width, height, message, value);
    }

    /**
     * Synchronises the slider with the backing value expressed as a percentage (0-100).
     */
    public void setValue(int value) {
        double clamped = Math.max(0.0, Math.min(1.0, value / 100.0));
        this.value = clamped;
        this.setMessage(new TextComponent(String.valueOf(value)));
    }

    @Override
    protected abstract void updateMessage();

    @Override
    protected abstract void applyValue();
}
