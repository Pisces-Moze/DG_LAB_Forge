package online.kbpf.dg_lab.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ButtonWidget extends Button {

    public ButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }

    public ButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip tooltip) {
        super(x, y, width, height, message, onPress, tooltip);
    }

    public static Builder builder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private Component tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;

        public Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public Builder tooltip(Component tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public ButtonWidget build() {
            if (tooltip != null) {
                return new ButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, (button, poseStack, mouseX, mouseY) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.screen != null) {
                        minecraft.screen.renderTooltip(poseStack, tooltip, mouseX, mouseY);
                    }
                });
            }
            return new ButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress);
        }
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
