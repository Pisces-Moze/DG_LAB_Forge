package online.kbpf.dg_lab.client.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class StrengthConfig {

    private static final String CONFIG_PATH = "config/dg-lab/StrengthConfig.json";

    private int aDownTime;
    private int bDownTime;
    private int aDownValue;
    private int bDownValue;
    private int aDelayTime;
    private int bDelayTime;
    private int aDeathStrength;
    private int bDeathStrength;
    private int aDeathDelay;
    private int bDeathDelay;
    private int aMin = 40;
    private int bMin = 40;
    private float aDamageStrength;
    private float bDamageStrength;

    public StrengthConfig() {
        this(3, 3, 5, 5, 1, 1, 50, 50, 40, 40);
    }

    public StrengthConfig(int aDamageStrength, int bDamageStrength, int aDownTime, int bDownTime,
                          int aDownValue, int bDownValue, int aDelayTime, int bDelayTime,
                          int aMin, int bMin) {
        this.aDamageStrength = aDamageStrength;
        this.bDamageStrength = bDamageStrength;
        this.aDownTime = Math.max(aDownTime, 1);
        this.bDownTime = Math.max(bDownTime, 1);
        this.aDownValue = Math.max(aDownValue, 0);
        this.bDownValue = Math.max(bDownValue, 0);
        this.aDelayTime = Math.max(aDelayTime, 0);
        this.bDelayTime = Math.max(bDelayTime, 0);
        this.aDeathStrength = 50;
        this.bDeathStrength = 50;
        this.aDeathDelay = this.aDelayTime;
        this.bDeathDelay = this.bDelayTime;
        this.aMin = aMin;
        this.bMin = bMin;
    }

    public void saveFile() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(CONFIG_PATH);
        if (!file.exists() && file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(this, writer);
        } catch (IOException ignored) {
        }
    }

    // Legacy name retained for existing callers.
    public void savaFile() {
        saveFile();
    }

    public static StrengthConfig loadJson() {
        Gson gson = new Gson();
        File file = new File(CONFIG_PATH);
        if (!file.exists()) {
            return new StrengthConfig();
        }
        try (Reader reader = new FileReader(file)) {
            StrengthConfig config = gson.fromJson(reader, StrengthConfig.class);
            return config != null ? config : new StrengthConfig();
        } catch (JsonSyntaxException | IOException ignored) {
            return new StrengthConfig();
        }
    }

    public int getAMin() {
        return aMin;
    }

    public void setAMin(int value) {
        this.aMin = Math.max(value, 0);
    }

    public int getBMin() {
        return bMin;
    }

    public void setBMin(int value) {
        this.bMin = Math.max(value, 0);
    }

    public int getADeathStrength() {
        return aDeathStrength;
    }

    public void setADeathStrength(int value) {
        this.aDeathStrength = Math.max(value, 0);
    }

    public int getBDeathStrength() {
        return bDeathStrength;
    }

    public void setBDeathStrength(int value) {
        this.bDeathStrength = Math.max(value, 0);
    }

    public int getADeathDelay() {
        return aDeathDelay;
    }

    public void setADeathDelay(int value) {
        this.aDeathDelay = Math.max(value, 0);
    }

    public int getBDeathDelay() {
        return bDeathDelay;
    }

    public void setBDeathDelay(int value) {
        this.bDeathDelay = Math.max(value, 0);
    }

    public int getADownValue() {
        return aDownValue;
    }

    public void setADownValue(int value) {
        this.aDownValue = Math.max(value, 0);
    }

    public int getBDownValue() {
        return bDownValue;
    }

    public void setBDownValue(int value) {
        this.bDownValue = Math.max(value, 0);
    }

    public int getADelayTime() {
        return aDelayTime;
    }

    public void setADelayTime(int value) {
        this.aDelayTime = Math.max(value, 0);
    }

    public int getBDelayTime() {
        return bDelayTime;
    }

    public void setBDelayTime(int value) {
        this.bDelayTime = Math.max(value, 0);
    }

    public float getADamageStrength() {
        return aDamageStrength;
    }

    public void setADamageStrength(float value) {
        this.aDamageStrength = clampFloat(value);
    }

    public float getBDamageStrength() {
        return bDamageStrength;
    }

    public void setBDamageStrength(float value) {
        this.bDamageStrength = clampFloat(value);
    }

    public int getADownTime() {
        return aDownTime;
    }

    public void setADownTime(int value) {
        this.aDownTime = Math.max(value, 1);
    }

    public int getBDownTime() {
        return bDownTime;
    }

    public void setBDownTime(int value) {
        this.bDownTime = Math.max(value, 1);
    }

    private float clampFloat(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        return Math.round(value * 100.0F) / 100.0F;
    }
}
