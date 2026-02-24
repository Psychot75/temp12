package controller;

import audio.SortAudioPlayer;
import factory.SortingAlgorithmFactory;
import interfaces.Observable;
import interfaces.Observer;
import model.SimulationSpeed;
import model.SortingParameters;
import sort.SortStep;
import sort.SortingAlgorithm;
import ui.VisualizationDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller — wires the sort algorithm to the visualization display and audio.
 *
 * Depends on VisualizationDisplay (interface), NOT on VisualizationPage directly.
 * This breaks the circular dependency between the controller and ui packages.
 *
 * Extends Observer (pull model):
 *   - Attaches to SortingAlgorithm (Observable).
 *   - On each update(), pulls a SortStep via algorithm.getCurrentStep().
 *   - Buffers all steps, then replays them on the JavaFX thread with
 *     the configured delay, playing an audio tone for each highlighted element.
 */
public class VisualizationController extends Observer {

    private final VisualizationDisplay display;
    private final SortAudioPlayer      audio;
    private final List<SortStep>       steps = new ArrayList<>();

    private SortingParameters parameters;
    private Thread            sortThread;
    private volatile boolean  running = false;

    public VisualizationController(VisualizationDisplay display, SortAudioPlayer audio) {
        this.display = display;
        this.audio   = audio;
    }

    // ─────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────

    public void prepare(SortingParameters parameters) {
        stop();
        this.parameters = parameters;
        steps.clear();

        int[] col = parameters.getCollection();
        int   min = Arrays.stream(col).min().orElse(0);
        int   max = Arrays.stream(col).max().orElse(1);
        audio.setRange(min, max);

        display.reset(col);
    }

    public void start() {
        if (parameters == null || running) return;
        running = true;
        steps.clear();

        sortThread = new Thread(() -> {
            // 1. Factory creates the correct algorithm
            SortingAlgorithm algorithm =
                    SortingAlgorithmFactory.create(parameters.getAlgorithmType());

            // 2. Attach this controller as Observer
            algorithm.attach(this);

            // 3. Template Method runs the full sort —
            //    each notifyObservers() inside triggers update() below
            algorithm.sort(parameters.getCollection());

            // 4. Replay buffered steps on the JavaFX thread
            replaySteps(parameters.getSpeed());
        });

        sortThread.setDaemon(true);
        sortThread.start();
    }

    public void stop() {
        running = false;
        if (sortThread != null) {
            sortThread.interrupt();
        }
    }

    // ─────────────────────────────────────────────
    //  OBSERVER — pull model
    // ─────────────────────────────────────────────

    /**
     * Called on every notifyObservers() emitted by the sort algorithm.
     *
     * Pull model: cast Observable → SortingAlgorithm, pull the snapshot.
     */
    @Override
    public void update(Observable o) {
        if (o instanceof SortingAlgorithm algorithm) {
            steps.add(algorithm.getCurrentStep());
        }
    }

    // ─────────────────────────────────────────────
    //  REPLAY
    // ─────────────────────────────────────────────

    private void replaySteps(SimulationSpeed speed) {
        for (SortStep step : steps) {
            if (!running) break;

            // Play a tone for each highlighted (compared/swapped) element
            for (int idx : step.getHighlightedIndices()) {
                if (idx < step.getArrayState().length) {
                    audio.playTone(step.getArrayState()[idx]);
                }
            }

            javafx.application.Platform.runLater(() -> display.updateDisplay(step));

            try {
                Thread.sleep(speed.getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
        javafx.application.Platform.runLater(display::notifySortComplete);
    }
}