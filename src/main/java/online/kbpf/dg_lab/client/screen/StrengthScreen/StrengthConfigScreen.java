package online.kbpf.dg_lab.client.screen.StrengthScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import online.kbpf.dg_lab.client.Config.StrengthConfig;
import online.kbpf.dg_lab.client.DgLabClient;
import online.kbpf.dg_lab.client.screen.ButtonWidget;
import online.kbpf.dg_lab.client.screen.ConfigScreen;
import online.kbpf.dg_lab.client.screen.SliderWidget;

import java.util.Locale;
// 移除 functional interface 导入，直接使用自定义方法

import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_HEIGHT;
import static online.kbpf.dg_lab.client.screen.ConfigScreen.BUTTON_SPACING;

public class StrengthConfigScreen extends Screen {

    private static final int SLIDER_WIDTH = 100;
    private static final int INFO_WIDTH = 12;

    private StrengthConfig strengthConfig;

    // 添加缺失的变量，与参考版本保持一致
    private SliderWidget ADamageStrength, BDamageStrength;
    private ButtonWidget DamageStrength;
    private SliderWidget ADelayTime, BDelayTime;
    private ButtonWidget DelayTime;
    private SliderWidget ADownTime, BDownTime;
    private ButtonWidget DownTime;
    private SliderWidget ADownValue, BDownValue;
    private ButtonWidget DownValue;
    private SliderWidget ADeathStrength, BDeathStrength;
    private ButtonWidget DeathStrength;
    private SliderWidget ADeathDelay, BDeathDelay;
    private ButtonWidget DeathDelay;
    private SliderWidget AMin, BMin;
    private ButtonWidget Min;

    public StrengthConfigScreen() {
        super(new TextComponent("强度配置"));
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(new ConfigScreen());
    }

    @Override
    protected void init() {
        this.strengthConfig = DgLabClient.strengthConfig;

        // 使用与Fabric版本一致的布局计算
        int leftX = this.width / 2 - 205;
        int leftX2 = this.width / 2 - 105;
        int rightX = this.width / 2 + 5;
        int rightX2 = this.width / 2 + 105;
        int infoLeft = this.width / 2 - 215;
        int infoRight = this.width / 2 + 205;

        int y = 20;

        // 使用与Fabric版本一致的滑块创建方式
        ADamageStrength = new SliderWidget(leftX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A每伤害强度" + String.format("%.2f", strengthConfig.getADamageStrength())),
                strengthConfig.getADamageStrength() / 20.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                float value = (float) (this.value * 20.0);
                strengthConfig.setADamageStrength(value);
                this.setMessage(new TextComponent("A每伤害强度" + String.format("%.2f", strengthConfig.getADamageStrength())));
            }
        };

        BDamageStrength = new SliderWidget(leftX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B每伤害强度" + String.format("%.2f", strengthConfig.getBDamageStrength())),
                strengthConfig.getBDamageStrength() / 20.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                float value = (float) (this.value * 20.0);
                strengthConfig.setBDamageStrength(value);
                this.setMessage(new TextComponent("B每伤害强度" + String.format("%.2f", strengthConfig.getBDamageStrength())));
            }
        };

        DamageStrength = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoLeft, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("每受到半颗心伤害增加的强度\n受伤时增加强度若小于1则增加1\n大于一的强度数值9舍0入\n若为0则不增加"))
                .build();

        // 延迟时间滑块（与Fabric版本一致的时间显示）
        ADelayTime = new SliderWidget(rightX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A强度下降等待" + strengthConfig.getADelayTime() * 50 + "ms"),
                (double) strengthConfig.getADelayTime() / 120) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 120);
                this.setMessage(new TextComponent("A强度下降等待" + value * 50 + "ms"));
                strengthConfig.setADelayTime(value);
            }
        };

        BDelayTime = new SliderWidget(rightX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B强度下降等待" + strengthConfig.getBDelayTime() * 50 + "ms"),
                (double) strengthConfig.getBDelayTime() / 120) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 120);
                this.setMessage(new TextComponent("B强度下降等待" + value * 50 + "ms"));
                strengthConfig.setBDelayTime(value);
            }
        };

        DelayTime = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoRight, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("伤害后强度开始减少前等待的时间"))
                .build();

        // 添加第一行组件到屏幕
        addRenderableWidget(ADamageStrength);
        addRenderableWidget(BDamageStrength);
        addRenderableWidget(DamageStrength);
        addRenderableWidget(ADelayTime);
        addRenderableWidget(BDelayTime);
        addRenderableWidget(DelayTime);

        y += BUTTON_HEIGHT + BUTTON_SPACING;

        // 冷却间隔滑块
        ADownTime = new SliderWidget(leftX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A冷却间隔" + strengthConfig.getADownTime()),
                strengthConfig.getADownTime() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("A冷却间隔" + value));
                strengthConfig.setADownTime(value);
            }
        };

        BDownTime = new SliderWidget(leftX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B冷却间隔" + strengthConfig.getBDownTime()),
                strengthConfig.getBDownTime() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("B冷却间隔" + value));
                strengthConfig.setBDownTime(value);
            }
        };

        DownTime = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoLeft, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("自动冷却触发的频率"))
                .build();

        // 冷却量滑块
        ADownValue = new SliderWidget(rightX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A冷却量" + strengthConfig.getADownValue()),
                strengthConfig.getADownValue() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("A冷却量" + value));
                strengthConfig.setADownValue(value);
            }
        };

        BDownValue = new SliderWidget(rightX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B冷却量" + strengthConfig.getBDownValue()),
                strengthConfig.getBDownValue() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("B冷却量" + value));
                strengthConfig.setBDownValue(value);
            }
        };

        DownValue = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoRight, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("每次冷却tick减少的强度量"))
                .build();

        // 添加第二行组件到屏幕
        addRenderableWidget(ADownTime);
        addRenderableWidget(BDownTime);
        addRenderableWidget(DownTime);
        addRenderableWidget(ADownValue);
        addRenderableWidget(BDownValue);
        addRenderableWidget(DownValue);

        y += BUTTON_HEIGHT + BUTTON_SPACING;

        // 死亡加成滑块
        ADeathStrength = new SliderWidget(leftX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A死亡加成" + strengthConfig.getADeathStrength()),
                strengthConfig.getADeathStrength() / 500.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 500);
                this.setMessage(new TextComponent("A死亡加成" + value));
                strengthConfig.setADeathStrength(value);
            }
        };

        BDeathStrength = new SliderWidget(leftX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B死亡加成" + strengthConfig.getBDeathStrength()),
                strengthConfig.getBDeathStrength() / 500.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 500);
                this.setMessage(new TextComponent("B死亡加成" + value));
                strengthConfig.setBDeathStrength(value);
            }
        };

        DeathStrength = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoLeft, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("玩家死亡时增加的额外强度"))
                .build();

        // 死亡延迟滑块
        ADeathDelay = new SliderWidget(rightX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A死亡延迟" + strengthConfig.getADeathDelay()),
                strengthConfig.getADeathDelay() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("A死亡延迟" + value));
                strengthConfig.setADeathDelay(value);
            }
        };

        BDeathDelay = new SliderWidget(rightX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B死亡延迟" + strengthConfig.getBDeathDelay()),
                strengthConfig.getBDeathDelay() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("B死亡延迟" + value));
                strengthConfig.setBDeathDelay(value);
            }
        };

        DeathDelay = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoRight, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("死亡后冷却恢复前的延迟"))
                .build();

        // 添加第三行组件到屏幕
        addRenderableWidget(ADeathStrength);
        addRenderableWidget(BDeathStrength);
        addRenderableWidget(DeathStrength);
        addRenderableWidget(ADeathDelay);
        addRenderableWidget(BDeathDelay);
        addRenderableWidget(DeathDelay);

        y += BUTTON_HEIGHT + BUTTON_SPACING;

        // 最小强度滑块
        AMin = new SliderWidget(leftX, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("A最小强度" + strengthConfig.getAMin()),
                strengthConfig.getAMin() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("A最小强度" + value));
                strengthConfig.setAMin(value);
            }
        };

        BMin = new SliderWidget(leftX2, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent("B最小强度" + strengthConfig.getBMin()),
                strengthConfig.getBMin() / 200.0) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = (int) (this.value * 200);
                this.setMessage(new TextComponent("B最小强度" + value));
                strengthConfig.setBMin(value);
            }
        };

        Min = ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(infoLeft, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(new TextComponent("基础强度下��（随缺失生命值缩放）"))
                .build();

        // 添加第四行组件到屏幕
        addRenderableWidget(AMin);
        addRenderableWidget(BMin);
        addRenderableWidget(Min);

        // 添加保存按钮
        y += BUTTON_HEIGHT + BUTTON_SPACING;
        ButtonWidget saveButton = ButtonWidget.builder(new TextComponent("保存配置"), button -> {
                    strengthConfig.savaFile();
                    this.minecraft.setScreen(new ConfigScreen());
                })
                .dimensions(this.width / 2 - 50, y, 100, BUTTON_HEIGHT)
                .tooltip(new TextComponent("保存当前强度设置到配置文件"))
                .build();
        addRenderableWidget(saveButton);
    }

    // 保留辅助方法用于向后兼容，但不使用参数化接口
    private SliderWidget createFloatSlider(int x, int y, String label, float currentValue) {
        double sliderValue = Mth.clamp(currentValue / 20.0, 0.0, 1.0);
        return new SliderWidget(x, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent(label + ": " + formatFloat(currentValue)), sliderValue) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                float value = (float) (this.value * 20.0);
                setMessage(new TextComponent(label + ": " + formatFloat(value)));
            }
        };
    }

    private SliderWidget createTickSlider(int x, int y, String label, int currentValue, int maxTicks) {
        double sliderValue = maxTicks > 0 ? Mth.clamp(currentValue / (double) maxTicks, 0.0, 1.0) : 0.0;
        return new SliderWidget(x, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent(label + ": " + currentValue), sliderValue) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = maxTicks > 0 ? (int) (this.value * maxTicks) : 0;
                setMessage(new TextComponent(label + ": " + value));
            }
        };
    }

    private SliderWidget createValueSlider(int x, int y, String label, int currentValue, int maxValue) {
        double sliderValue = maxValue > 0 ? Mth.clamp(currentValue / (double) maxValue, 0.0, 1.0) : 0.0;
        return new SliderWidget(x, y, SLIDER_WIDTH, BUTTON_HEIGHT,
                new TextComponent(label + ": " + currentValue), sliderValue) {
            @Override
            protected void updateMessage() {
            }

            @Override
            protected void applyValue() {
                int value = maxValue > 0 ? (int) (this.value * maxValue) : 0;
                setMessage(new TextComponent(label + ": " + value));
            }
        };
    }

    private ButtonWidget infoButton(int x, int y, Component tooltip) {
        return ButtonWidget.builder(new TextComponent("?"), button -> {})
                .dimensions(x, y, INFO_WIDTH, BUTTON_HEIGHT)
                .tooltip(tooltip)
                .build();
    }

    private String formatFloat(float value) {
        return String.format(Locale.US, "%.2f", value);
    }
}