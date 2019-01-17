package ua.in.beroal.java;

public class FrequencyDivider {
    private final int k;
    private int i = 0;

    /**
     * Divides frequency of {@link #tick} calls by {@code k}.
     */
    public FrequencyDivider(int k) {
        this.k = k;
    }

    /**
     * @return The index (starting from {@code 0}) of this call
     * is a multiple of the constructor's argument.
     */
    public boolean tick() {
        boolean tickOut = i == 0;
        if (tickOut) {
            i = k;
        }
        i--;
        return tickOut;
    }
}
