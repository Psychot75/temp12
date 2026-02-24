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

public class VisualizationController extends Observer {

    private final VisualizationDisplay display;
    private final SortAudioPlayer audio;
    private final List<SortStep> steps = new ArrayList<>();

    private SortingParameters parameters;
    private Thread sortThread;
    private volatile boolean running = false;

    public VisualizationController(VisualizationDisplay display, SortAudioPlayer audio) {
        this.display = display;
        this.audio = audio;
    }

    public void prepare(SortingParameters parameters) {
        stop();
        this.parameters = parameters;
        steps.clear();
        int[] col = parameters.getCollection();
        int min = Arrays.stream(col).min().orElse(0);
        int max = Arrays.stream(col).max().orElse(1);
        audio.setRange(min, max);
        display.reset(col);
    }

    public void start() {
        if (parameters == null || running) return;
        running = true;
        steps.clear();

        sortThread = new Thread(() -> {
            SortingAlgorithm algorithm = SortingAlgorithmFactory.create(parameters.getAlgorithmType());
            algorithm.attach(this);
            algorithm.sort(parameters.getCollection());
            replaySteps(parameters.getSpeed());
        });
        sortThread.setDaemon(true);
        sortThread.start();
    }

    public void stop() {
        running = false;
        if (sortThread != null) sortThread.interrupt();
    }

    @Override
    public void update(Observable o) {
        if (o instanceof SortingAlgorithm algorithm) {
            steps.add(algorithm.getCurrentStep());
        }
    }

    private void replaySteps(SimulationSpeed speed) {
        for (SortStep step : steps) {
            if (!running) break;
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
