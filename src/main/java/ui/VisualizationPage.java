package ui;

import audio.SortAudioPlayer;
import controller.VisualizationController;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import mediator.AppMediator;
import mediator.Colleague;
import mediator.Mediator;
import model.SortingParameters;
import sort.SortStep;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Page 2 — Real-time sorting visualization with bar chart and audio.
 *
 * Implements VisualizationDisplay so the controller has no direct
 * dependency on this class (breaks circular import).
 *
 * Communicates exclusively through the Mediator (Colleague).
 *
 * Events emitted:
 *   AppMediator.EVT_BACK          — user clicks back button
 *   AppMediator.EVT_SORT_COMPLETE — sort has finished
 */
public class VisualizationPage extends Colleague implements VisualizationDisplay {

    // ─── Colors ───────────────────────────────────────────────────────────────
    private static final Color BG_COLOR           = Color.web("#1a1a2e");
    private static final Color BAR_DEFAULT         = Color.web("#0f3460");
    private static final Color BAR_HIGHLIGHTED_A   = Color.web("#e94560");  // primary compare
    private static final Color BAR_HIGHLIGHTED_B   = Color.web("#f5a623");  // secondary
    private static final Color BAR_SORTED          = Color.web("#4ecca3");
    private static final Color BAR_OUTLINE         = Color.web("#1a1a2e");

    // ─── Components ───────────────────────────────────────────────────────────
    private final BorderPane root;
    private final Canvas     canvas;
    private final Label      lblAlgorithm;
    private final Label      lblStep;
    private final Button     btnBack;
    private final Button     btnMute;

    // ─── Controller & Audio ──────────────────────────────────────────────────
    private final SortAudioPlayer        audio;
    private final VisualizationController controller;

    // ─── State ────────────────────────────────────────────────────────────────
    private int[]   currentArray       = new int[0];
    private int[]   highlightedIndices = new int[0];
    private boolean sortDone           = false;
    private int     stepCount          = 0;

    public VisualizationPage(Mediator mediator) {
        super(mediator);
        audio      = new SortAudioPlayer();
        controller = new VisualizationController(this, audio);  // passes interface

        // ── Canvas ────────────────────────────────────────────────────────────
        canvas = new Canvas(950, 430);

        // ── Labels ────────────────────────────────────────────────────────────
        lblAlgorithm = new Label("—");
        lblAlgorithm.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        lblAlgorithm.setStyle("-fx-text-fill: #e94560;");

        lblStep = new Label("Étape : 0");
        lblStep.setFont(Font.font("Monospace", 13));
        lblStep.setStyle("-fx-text-fill: #a8a8c0;");

        // ── Buttons ───────────────────────────────────────────────────────────
        btnBack = new Button("← Retour");
        styleButton(btnBack, "#0f3460");
        btnBack.setDisable(true);
        btnBack.setOnAction(e -> send(AppMediator.EVT_BACK));

        btnMute = new Button("🔊");
        styleButton(btnMute, "#0f3460");
        btnMute.setOnAction(e -> toggleMute());

        // ── Top bar ───────────────────────────────────────────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(16, btnBack, lblAlgorithm, spacer, lblStep, btnMute);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 16, 12, 16));
        topBar.setStyle("-fx-background-color: #16213e;");

        // ── Canvas wrapper ────────────────────────────────────────────────────
        StackPane canvasWrapper = new StackPane(canvas);
        canvasWrapper.setStyle("-fx-background-color: #1a1a2e;");

        // ── Root ─────────────────────────────────────────────────────────────
        root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(canvasWrapper);
        root.setStyle("-fx-background-color: #1a1a2e;");
    }

    public Pane getRoot() { return root; }

    // ─────────────────────────────────────────────
    //  API called by AppMediator
    // ─────────────────────────────────────────────

    /** Called by mediator before switching to this page. */
    public void prepare(SortingParameters params) {
        sortDone  = false;
        stepCount = 0;
        lblAlgorithm.setText(
                params.getAlgorithmType().toString().replace("_", " ")
        );
        lblStep.setText("Étape : 0");
        btnBack.setDisable(true);
        controller.prepare(params);

        FadeTransition ft = new FadeTransition(Duration.millis(350), root);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Called by mediator after prepare(). */
    public void startSort() {
        controller.start();
    }

    /** Called by mediator on BACK event. */
    public void stopSort() {
        controller.stop();
        audio.reset();
    }

    /** Called by mediator when SORT_COMPLETE is received — enables back button. */
    public void onSortComplete() {
        btnBack.setDisable(false);
    }

    // ─────────────────────────────────────────────
    //  VisualizationDisplay — called by controller
    // ─────────────────────────────────────────────

    @Override
    public void reset(int[] array) {
        currentArray       = array.clone();
        highlightedIndices = new int[0];
        stepCount          = 0;
        sortDone           = false;
        lblStep.setText("Étape : 0");
        redraw();
    }

    @Override
    public void updateDisplay(SortStep step) {
        currentArray       = step.getArrayState();
        highlightedIndices = step.getHighlightedIndices();
        stepCount++;
        lblStep.setText("Étape : " + stepCount);
        redraw();
    }

    @Override
    public void notifySortComplete() {
        sortDone           = true;
        highlightedIndices = new int[0];
        redraw();
        btnBack.setDisable(false);
        send(AppMediator.EVT_SORT_COMPLETE);
    }

    // ─────────────────────────────────────────────
    //  DRAWING
    // ─────────────────────────────────────────────

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Background
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);

        if (currentArray.length == 0) return;

        int    max        = Arrays.stream(currentArray).max().orElse(1);
        double slotW      = w / currentArray.length;
        double barW       = Math.max(slotW * 0.72, 2);
        double labelAreaH = (slotW >= 20) ? 22 : 0;
        double drawH      = h - labelAreaH - 24;

        Set<Integer> hilightSet = new HashSet<>();
        for (int idx : highlightedIndices) hilightSet.add(idx);

        gc.setFont(Font.font("Monospace", Math.min(slotW * 0.55, 11)));

        for (int i = 0; i < currentArray.length; i++) {
            double barH = (double) currentArray[i] / max * drawH;
            double x    = slotW * i + (slotW - barW) / 2.0;
            double y    = h - labelAreaH - barH - 8;

            Color barColor;
            if (sortDone) {
                barColor = BAR_SORTED;
            } else if (hilightSet.contains(i)) {
                int hi0 = highlightedIndices.length > 0 ? highlightedIndices[0] : -1;
                barColor = (i == hi0) ? BAR_HIGHLIGHTED_A : BAR_HIGHLIGHTED_B;
            } else {
                barColor = BAR_DEFAULT;
            }

            // Gradient bar (lighter at top)
            LinearGradient gradient = new LinearGradient(
                    0, y, 0, y + barH, false, CycleMethod.NO_CYCLE,
                    new Stop(0, barColor.brighter()),
                    new Stop(1, barColor)
            );
            gc.setFill(gradient);
            gc.fillRoundRect(x, y, barW, barH, 4, 4);

            // Thin separator outline
            gc.setStroke(BAR_OUTLINE);
            gc.setLineWidth(0.5);
            gc.strokeRoundRect(x, y, barW, barH, 4, 4);

            // Value label beneath bar
            if (labelAreaH > 0) {
                gc.setFill(Color.web("#a8a8c0"));
                String label = String.valueOf(currentArray[i]);
                double lw    = label.length() * (Math.min(slotW * 0.55, 11) * 0.6);
                gc.fillText(label, x + barW / 2.0 - lw / 2.0, h - 6);
            }
        }

        // Glow halo pass over highlighted bars
        if (!sortDone) {
            for (int idx : hilightSet) {
                if (idx >= currentArray.length) continue;
                double barH = (double) currentArray[idx] / max * drawH;
                double x    = slotW * idx + (slotW - barW) / 2.0;
                double y    = h - labelAreaH - barH - 8;
                gc.setFill(Color.web("#e94560", 0.15));
                gc.fillRoundRect(x - 3, y - 3, barW + 6, barH + 6, 6, 6);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private void toggleMute() {
        boolean nowMuted = !audio.isMuted();
        audio.setMuted(nowMuted);
        btnMute.setText(nowMuted ? "🔇" : "🔊");
    }

    private void styleButton(Button btn, String bg) {
        btn.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: #e0e0ff;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 6 14 6 14;"
        );
    }
}