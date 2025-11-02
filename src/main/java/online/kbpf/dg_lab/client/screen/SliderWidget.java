package online.kbpf.dg_lab.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public abstract class SliderWidget extends AbstractSliderButton {
    protected SliderWidget(int x, int y, int width, int height, Component message, double value) {
        super(x, y, width, height, message, value);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}
