package online.kbpf.dg_lab.client.entity.Waveform;

import online.kbpf.dg_lab.client.Tool.DGWaveformTool;

import java.util.ArrayList;
import java.util.List;

public class Waveform {

    private String waveform;
    private String name = "empty";
    private int duration;
    private List<ControlBar> list = new ArrayList<>();

    public Waveform() {
        for (int i = 0; i < 4; i++) {
            list.add(new ControlBar());
        }
        graphToData();
    }

    public Waveform(List<ControlBar> list) {
        this.list = new ArrayList<>(list);
        graphToData();
    }

    public Waveform(String waveform) {
        this.waveform = waveform;
        dataToGraph();
        updateDuration();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWaveform(String waveform) {
        this.waveform = waveform;
        updateDuration();
    }

    public String getWaveform() {
        return waveform;
    }

    public int getDuration() {
        return duration;
    }

    public List<ControlBar> getList() {
        return list;
    }

    public void setList(List<ControlBar> list) {
        this.list = new ArrayList<>(list);
        graphToData();
    }

    public boolean updateDuration() {
        duration = DGWaveformTool.checkAndCountValidSubstrings(waveform);
        return duration <= 0;
    }

    public Waveform dataToGraph() {
        list = new ArrayList<>();
        if (waveform == null || waveform.isEmpty()) {
            return this;
        }

        if (DGWaveformTool.checkAndCountValidSubstrings(waveform) <= 0) {
            return this;
        }

        String[] parts = waveform.split(",");
        for (String part : parts) {
            String content = part.substring(1, part.length() - 1);
            for (int i = 0; i < 4; i++) {
                int frequency = Integer.parseInt(content.substring(i * 2, i * 2 + 2), 16);
                int strength = Integer.parseInt(content.substring(8 + i * 2, 8 + i * 2 + 2), 16);
                list.add(new ControlBar(strength, frequency, true, true));
            }
        }
        return this;
    }

    public void graphToData() {
        if (list.isEmpty()) {
            waveform = "";
            duration = 0;
            return;
        }

        StringBuilder builder = new StringBuilder();
        StringBuilder freqBuilder = new StringBuilder();
        StringBuilder strengthBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            ControlBar bar = list.get(i);
            freqBuilder.append(String.format("%02X", bar.getFrequency()));
            strengthBuilder.append(String.format("%02X", bar.getStrength()));
            if ((i + 1) % 4 == 0) {
                if (!builder.isEmpty()) {
                    builder.append(',');
                }
                builder.append('"')
                        .append(freqBuilder)
                        .append(strengthBuilder)
                        .append('"');
                freqBuilder.setLength(0);
                strengthBuilder.setLength(0);
            }
        }

        waveform = builder.toString();
        updateDuration();
    }
}
