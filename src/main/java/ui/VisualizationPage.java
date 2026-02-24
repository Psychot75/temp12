package ui;

import audio.SortAudioPlayer;
import controller.VisualizationController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import mediator.AppMediator;
import mediator.Colleague;
import mediator.Mediator;
import model.SortingParameters;
import sort.SortStep;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VisualizationPage extends Colleague implements VisualizationDisplay {

    private static final Color BAR_DEFAULT = Color.STEELBLUE;
    private static final Color BAR_HIGHLIGHT_A = Color.RED;
    private static final Color BAR_HIGHLIGHT_B = Color.ORANGE;
    private static final Color BAR_SORTED = Color.LIMEGREEN;

    private final BorderPane root;
    private final Canvas canvas;
    private final Label lblAlgorithm;
    private final Label lblStep;
    private final Button btnBack;
    private final Button btnMute;

    private final SortAudioPlayer audio;
    private final VisualizationController controller;

    private int[] currentArray = new int[0];
    private int[] highlightedIndices = new int[0];
    private boolean sortDone = false;
    private int stepCount = 0;

    public VisualizationPage(Mediator mediator) {
        super(mediator);
        audio = new SortAudioPlayer();
        controller = new VisualizationController(this, audio);

        canvas = new Canvas(950, 430);

        lblAlgorithm = new Label("-");
        lblAlgorithm.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        lblStep = new Label("Etape : 0");

        btnBack = new Button("Retour");
        btnBack.setDisable(true);
        btnBack.setOnAction(e -> send(AppMediator.EVT_BACK));

        btnMute = new Button("Son ON");
        btnMute.setOnAction(e -> toggleMute());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, btnBack, lblAlgorithm, spacer, lblStep, btnMute);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 8, 12));

        StackPane canvasWrapper = new StackPane(canvas);

        root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(canvasWrapper);
    }

    public Pane getRoot() { return root; }

    public void prepare(SortingParameters params) {
        sortDone = false;
        stepCount = 0;
        lblAlgorithm.setText(params.getAlgorithmType().toString().replace("_", " "));
        lblStep.setText("Etape : 0");
        btnBack.setDisable(true);
        controller.prepare(params);
    }

    public void startSort() { controller.start(); }

    public void stopSort() {
        controller.stop();
        audio.reset();
    }

    public void onSortComplete() { btnBack.setDisable(false); }

    @Override
    public void reset(int[] array) {
        currentArray = array.clone();
        highlightedIndices = new int[0];
        stepCount = 0;
        sortDone = false;
        lblStep.setText("Etape : 0");
        redraw();
    }

    @Override
    public void updateDisplay(SortStep step) {
        currentArray = step.getArrayState();
        highlightedIndices = step.getHighlightedIndices();
        stepCount++;
        lblStep.setText("Etape : " + stepCount);
        redraw();
    }

    @Override
    public void notifySortComplete() {
        sortDone = true;
        highlightedIndices = new int[0];
        redraw();
        btnBack.setDisable(false);
        send(AppMediator.EVT_SORT_COMPLETE);
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        if (currentArray.length == 0) return;

        int max = Arrays.stream(currentArray).max().orElse(1);
        double slotW = w / currentArray.length;
        double barW = Math.max(slotW * 0.8, 2);
        double padding = 16;
        double drawH = h - padding;

        Set<Integer> hilightSet = new HashSet<>();
        for (int idx : highlightedIndices) hilightSet.add(idx);

        for (int i = 0; i < currentArray.length; i++) {
            double barH = (double) currentArray[i] / max * drawH;
            double x = slotW * i + (slotW - barW) / 2.0;
            double y = h - barH;

            Color barColor;
            if (sortDone) {
                barColor = BAR_SORTED;
            } else if (hilightSet.contains(i)) {
                int hi0 = highlightedIndices.length > 0 ? highlightedIndices[0] : -1;
                barColor = (i == hi0) ? BAR_HIGHLIGHT_A : BAR_HIGHLIGHT_B;
            } else {
                barColor = BAR_DEFAULT;
            }

            gc.setFill(barColor);
            gc.fillRect(x, y, barW, barH);
        }
    }

    private void toggleMute() {
        boolean nowMuted = !audio.isMuted();
        audio.setMuted(nowMuted);
        btnMute.setText(nowMuted ? "Son OFF" : "Son ON");
    }
}
