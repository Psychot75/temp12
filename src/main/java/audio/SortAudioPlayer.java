package audio;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generates sorting sounds in real-time using raw PCM sine waves.
 *
 * Frequency mapping:
 *   element value  →  [FREQ_MIN .. FREQ_MAX]  (linear interpolation)
 *
 * Synthesis: pure javax.sound.sampled — no audio files, no external libraries.
 * Each tone is a sine wave burst written directly to a SourceDataLine with a
 * short fade-in / fade-out envelope to prevent clicking artifacts.
 *
 * The player uses a single-thread daemon executor so tones queue naturally
 * without blocking the JavaFX thread.
 *
 * reset() can be called between sorts — it safely drains the current executor
 * and creates a fresh one.
 */
public class SortAudioPlayer {

    // ─── Audio format ─────────────────────────────────────────────────────────
    private static final float SAMPLE_RATE = 44100f;
    private static final int   SAMPLE_BITS = 16;
    private static final int   CHANNELS    = 1;          // mono
    private static final int   TONE_MS     = 60;         // ms per tone burst

    // ─── Frequency range (Hz) ────────────────────────────────────────────────
    private static final double FREQ_MIN = 180.0;        // smallest value → low pitch
    private static final double FREQ_MAX = 1100.0;       // largest value  → high pitch

    // ─── State ────────────────────────────────────────────────────────────────
    private int     collectionMin = 0;
    private int     collectionMax = 1;
    private boolean muted         = false;

    private ExecutorService executor = newExecutor();

    // ─────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────

    /** Set value range before starting a sort so frequencies scale correctly. */
    public void setRange(int min, int max) {
        this.collectionMin = min;
        this.collectionMax = Math.max(max, min + 1);
    }

    /**
     * Plays a short sine burst whose pitch maps to the element's value.
     * Non-blocking — submitted to a daemon thread.
     */
    public void playTone(int value) {
        if (muted || executor.isShutdown()) return;
        double frequency = valueToFrequency(value);
        executor.submit(() -> synthesizeAndPlay(frequency, TONE_MS));
    }

    public void setMuted(boolean muted) { this.muted = muted; }
    public boolean isMuted()            { return muted; }

    /**
     * Cancels pending tones and prepares a fresh executor.
     * Safe to call between sorts.
     */
    public void reset() {
        executor.shutdownNow();
        executor = newExecutor();
    }

    // ─────────────────────────────────────────────
    //  SINE WAVE SYNTHESIS
    // ─────────────────────────────────────────────

    /**
     * Synthesizes a sine wave at the given frequency and plays it immediately.
     *
     * Pseudocode:
     *   numSamples = SAMPLE_RATE * durationMs / 1000
     *   fadeSamples = min(numSamples/6, 8ms worth of samples)
     *   for i in 0..numSamples:
     *     t        = i / SAMPLE_RATE
     *     sine     = sin(2π * frequency * t)
     *     envelope = fade-in ramp at start / fade-out ramp at end
     *     sample   = (short)(sine * amplitude * envelope)
     *     write 16-bit little-endian to buffer
     *   open SourceDataLine → write buffer → drain → close
     */
    private void synthesizeAndPlay(double frequency, int durationMs) {
        int    numSamples = (int)(SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer     = new byte[numSamples * 2];     // 2 bytes per 16-bit sample
        double amplitude  = 22000.0;                      // leave headroom under 32767

        int fadeSamples = Math.min(numSamples / 6, (int)(SAMPLE_RATE * 0.008));

        for (int i = 0; i < numSamples; i++) {
            double t        = (double) i / SAMPLE_RATE;
            double sine     = Math.sin(2.0 * Math.PI * frequency * t);

            // Envelope: linear fade-in at start, fade-out at end (prevents clicks)
            double envelope = 1.0;
            if (i < fadeSamples) {
                envelope = (double) i / fadeSamples;
            } else if (i > numSamples - fadeSamples) {
                envelope = (double)(numSamples - i) / fadeSamples;
            }

            short sample = (short)(sine * amplitude * envelope);

            // Little-endian 16-bit PCM
            buffer[2 * i]     = (byte)(sample & 0xFF);
            buffer[2 * i + 1] = (byte)((sample >> 8) & 0xFF);
        }

        AudioFormat format = new AudioFormat(
                SAMPLE_RATE, SAMPLE_BITS, CHANNELS,
                true,   // signed
                false   // little-endian
        );

        try {
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format, buffer.length);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException ignored) {
            // Audio device busy — skip this tone silently
        }
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    /** Linear interpolation: element value → frequency in [FREQ_MIN, FREQ_MAX]. */
    private double valueToFrequency(int value) {
        double ratio = (double)(value - collectionMin) / (collectionMax - collectionMin);
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