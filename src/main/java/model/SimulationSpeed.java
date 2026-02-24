package model;

public enum SimulationSpeed {

    SLOW(800),
    NORMAL(300),
    FAST(80);

    private final int delayMs;

    SimulationSpeed(int delayMs) {
        this.delayMs = delayMs;
    }

    public int getDelayMs() { return delayMs; }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
