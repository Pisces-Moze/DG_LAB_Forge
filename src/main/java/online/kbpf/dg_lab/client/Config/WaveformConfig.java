package online.kbpf.dg_lab.client.Config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import online.kbpf.dg_lab.client.entity.Waveform.Waveform;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public final class WaveformConfig {

    private WaveformConfig() {
    }

    public static Map<String, Waveform> LoadWaveform() {
        Gson gson = new Gson();
        File file = new File("config/dg-lab/WaveformData.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                return gson.fromJson(reader, new TypeToken<Map<String, Waveform>>() {
                }.getType());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, Waveform> waveform = new HashMap<>();
        waveform.put("ADamage", new Waveform("\"0A0A0A0A64646464\",\"0A0A0A0A64646464\",\"0A0A0A0A64646464\",\"0A0A0A0A64000000\"").dataToGraph());
        waveform.put("BDamage", new Waveform("\"0A0A0A0A64646464\",\"0A0A0A0A64646464\",\"0A0A0A0A64646464\",\"0A0A0A0A64000000\"").dataToGraph());
        waveform.put("AHealing", new Waveform("\"0A0A0A0A1921282F\",\"0A0A0A0A363D444B\",\"0A0A0A0A4B433C35\",\"0A0A0A0A2E272019\"").dataToGraph());
        waveform.put("BHealing", new Waveform("\"0A0A0A0A1921282F\",\"0A0A0A0A363D444B\",\"0A0A0A0A4B433C35\",\"0A0A0A0A2E272019\"").dataToGraph());
        return waveform;
    }

    public static void saveWaveform(Map<String, Waveform> waveformData) {
        Gson gson = new Gson();
        File file = new File("config/dg-lab/WaveformData.json");

        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(waveformData, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
