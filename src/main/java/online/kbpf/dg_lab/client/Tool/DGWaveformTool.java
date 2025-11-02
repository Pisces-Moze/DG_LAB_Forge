package online.kbpf.dg_lab.client.Tool;

import online.kbpf.dg_lab.client.entity.Waveform.Waveform;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static online.kbpf.dg_lab.client.DgLabClient.waveformMap;

public final class DGWaveformTool {

    private DGWaveformTool() {
    }

    public static String textToWaveform(String text) {
        int length = 0;
        List<Pair> pairs = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        Pair current = new Pair();

        for (char ch : text.toCharArray()) {
            if (Character.isDigit(ch)) {
                number.append(ch);
            } else if (ch == ',') {
                current.time = Integer.parseInt(number.toString());
                number.setLength(0);
                length += current.time + 1;
            } else if (ch == ';') {
                current.strength = Integer.parseInt(number.toString());
                pairs.add(current);
                current = new Pair();
                number.setLength(0);
            }
        }

        double[] frequency = new double[length + 1];
        frequency[0] = 0;
        int index = 1;
        for (Pair pair : pairs) {
            double max = pair.strength;
            double min = frequency[index - 1];
            double delta = (max - min) / (pair.time + 1);
            for (int i = 0; i <= pair.time; i++) {
                frequency[index] = frequency[index - 1] + delta;
                index++;
            }
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= length; i++) {
            if (i % 4 == 0) {
                if (!builder.isEmpty()) {
                    builder.append(",\"0A0A0A0A");
                } else {
                    builder.append("\"0A0A0A0A");
                }
            }
            builder.append(String.format("%02X", (int) frequency[i]));
        }

        switch (length % 4) {
            case 0 -> builder.append("000000");
            case 1 -> builder.append("0000");
            case 2 -> builder.append("00");
            default -> {
            }
        }
        builder.append('"');
        return builder.toString();
    }

    public static String TextToWaveform(String text) {
        return textToWaveform(text);
    }

    public static void updateDuration() {
        for (Waveform waveform : waveformMap.values()) {
            waveform.updateDuration();
        }
    }

    public static int checkAndCountValidSubstrings(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        String[] substrings = input.split(",");
        int validCount = 0;
        for (String substring : substrings) {
            if (!validateSubstring(substring)) {
                return 0;
            }
            validCount++;
        }

        return validCount == StringUtils.countMatches(input, ',') + 1 ? validCount : 0;
    }

    public static boolean validateSubstring(String substring) {
        String regex = "^\"[0-9a-fA-F]{16}\"$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(substring);
        if (!matcher.matches()) {
            return false;
        }

        String content = substring.substring(1, substring.length() - 1);
        for (int i = 0; i < content.length(); i += 2) {
            int decimalValue = Integer.parseInt(content.substring(i, i + 2), 16);
            if (decimalValue > 100) {
                return false;
            }
        }
        return true;
    }

    public static boolean isHexCharacter(char ch) {
        return Character.toString(ch).matches("[0-9a-fA-F]");
    }

    private static final class Pair {
        int time;
        int strength;
    }
}
