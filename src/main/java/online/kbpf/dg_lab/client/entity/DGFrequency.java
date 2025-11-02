package online.kbpf.dg_lab.client.entity;

public class DGFrequency {
    private int[] A = {100, 100, 100, 100}, B = {100, 100, 100, 100}, N = {10, 10, 10, 10};

    // Default damage waveform (16 samples, 5 ms per step in the original DG protocol).
    private final int[] damage = {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 0, 0, 0};


    private final int[] healing = {25, 33, 40, 47, 54, 61, 68, 75, 75, 67, 60, 53, 46, 39, 32, 25};

    public DGFrequency() {
    }

    public DGFrequency(int[] a, int[] b) {
        A = a;
        B = b;
    }

    public int[] getA() {
        return A;
    }

    public void setA(int[] a) {
        A = a;
    }

    public int[] getB() {
        return B;
    }

    public void setB(int[] b) {
        B = b;
    }



    public String getHexString(int A1orB2) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (int i : N) {
            hexStringBuilder.append(String.format("%02X", i));
        }
        if (A1orB2 == 1) {
            for (int i : A) {
                hexStringBuilder.append(String.format("%02X", i));
            }
        }
        if (A1orB2 == 2) {
            for (int i : B) {
                hexStringBuilder.append(String.format("%02X", i));
            }
        }

        return hexStringBuilder.toString();
    }



    // Convert the preset damage waveform to the encoded hexadecimal form.
    public String getDamageFrequency() {
        StringBuilder frequency = new StringBuilder();
        for (int i = 0; i <= 15; i++) {
            if(i % 4 == 0) {
                if (i != 0) frequency.append("\",\"0A0A0A0A");
                else frequency.append("\"0A0A0A0A");
            }
            frequency.append(String.format("%02X", damage[i]));
        }
        frequency.append('\"');
        System.out.println(frequency);
        return frequency.toString();
    }



    // Convert the preset healing waveform to the encoded hexadecimal form.
    public String getHealingFrequency() {
        StringBuilder frequency = new StringBuilder();
        for (int i = 0; i <= 15; i++) {
            if (i % 4 == 0) {
                if (i != 0) frequency.append("\",\"0A0A0A0A");
                else frequency.append("\"0A0A0A0A");
            }
            frequency.append(String.format("%02X", healing[i]));
        }
        frequency.append('\"');
        System.out.println(frequency);
        return frequency.toString();
    }


}
