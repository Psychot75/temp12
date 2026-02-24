package audio;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SortAudioPlayer {

    private static final float SAMPLE_RATE = 44100f;
    private static final int TONE_MS = 60;
    private static final double FREQ_MIN = 180.0;
    private static final double FREQ_MAX = 1100.0;

    private int collectionMin = 0;
    private int collectionMax = 1;
    private boolean muted = false;
    private ExecutorService executor = newExecutor();

    public void setRange(int min, int max) {
        this.collectionMin = min;
        this.collectionMax = Math.max(max, min + 1);
    }

    public void playTone(int value) {
        if (muted || executor.isShutdown()) return;
        double frequency = valueToFrequency(value);
        executor.submit(() -> synthesizeAndPlay(frequency, TONE_MS));
    }

    public void setMuted(boolean muted) { this.muted = muted; }
    public boolean isMuted() { return muted; }

    public void reset() {
        executor.shutdownNow();
        executor = newExecutor();
    }

    private void synthesizeAndPlay(double frequency, int durationMs) {
        int numSamples = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer = new byte[numSamples * 2];
        double amplitude = 22000.0;
        int fadeSamples = Math.min(numSamples / 6, (int) (SAMPLE_RATE * 0.008));

        for (int i = 0; i < numSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double sine = Math.sin(2.0 * Math.PI * frequency * t);
            double envelope = 1.0;
            if (i < fadeSamples) envelope = (double) i / fadeSamples;
            else if (i > numSamples - fadeSamples) envelope = (double) (numSamples - i) / fadeSamples;
            short sample = (short) (sine * amplitude * envelope);
            buffer[2 * i] = (byte) (sample & 0xFF);
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try {
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format, buffer.length);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException ignored) {}
    }

    private double valueToFrequency(int value) {
        double ratio = (double) (value - collectionMin) / (collectionMax - collectionMin);
        return FREQ_MIN + ratio * (FREQ_MAX - FREQ_MIN);
    }

    private static ExecutorService newExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sort-audio");
            t.setDaemon(true);
            return t;
        });
    }
}
