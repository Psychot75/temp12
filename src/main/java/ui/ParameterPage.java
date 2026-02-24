package ui;

import factory.SortingAlgorithmFactory.AlgorithmType;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import mediator.Colleague;
import mediator.Mediator;
import mediator.AppMediator;
import model.SimulationSpeed;
import model.SortingParameters;

import java.util.Arrays;

/**
 * Page 1 — Parameter selection.
 * Communicates exclusively through the Mediator.
 *
 * Events emitted:
 *   AppMediator.EVT_START_SORT  with SortingParameters payload
 */
public class ParameterPage extends Colleague {

    private final BorderPane root;

    private ComboBox<String>          cbAlgorithm;
    private TextField                 tfCollection;
    private ComboBox<SimulationSpeed> cbSpeed;
    private Label                     lblError;

    public ParameterPage(Mediator mediator) {
        super(mediator);
        root = buildUI();
    }

    public Pane getRoot() { return root; }

    // ─────────────────────────────────────────────
    //  UI BUILD
    // ─────────────────────────────────────────────

    private BorderPane buildUI() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: #1a1a2e;");

        // ── Header ────────────────────────────────────────────────────────────
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(40, 0, 20, 0));

        Text title = new Text("TRI VISUEL");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 36));
        title.setFill(Color.web("#e94560"));

        Text subtitle = new Text("Visualiseur d'algorithmes de tri");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setFill(Color.web("#a8a8c0"));

        header.getChildren().addAll(title, subtitle);

        // ── Form card ─────────────────────────────────────────────────────────
        VBox card = new VBox(22);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(36, 44, 36, 44));
        card.setMaxWidth(480);
        card.setStyle(
                "-fx-background-color: #16213e;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 20, 0, 0, 4);"
        );

        // Algorithm
        cbAlgorithm = new ComboBox<>(FXCollections.observableArrayList(
                "Quick Sort", "Merge Sort"
        ));
        cbAlgorithm.setValue("Merge Sort");
        card.getChildren().addAll(
                label("Algorithme de tri"),
                styledCombo(cbAlgorithm)
        );

        // Collection input
        tfCollection = new TextField("50,87,56,12,75,100,20,34,9");
        tfCollection.setFont(Font.font("Monospace", 13));
        card.getChildren().addAll(
                label("Collection d'entiers (séparés par virgules)"),
                styledTextField(tfCollection)
        );

        // Speed
        cbSpeed = new ComboBox<>(FXCollections.observableArrayList(SimulationSpeed.values()));
        cbSpeed.setValue(SimulationSpeed.FAST);
        card.getChildren().addAll(
                label("Vitesse de simulation"),
                styledCombo(cbSpeed)
        );

        // Error label
        lblError = new Label("");
        lblError.setStyle("-fx-text-fill: #e94560; -fx-font-size: 12;");
        card.getChildren().add(lblError);

        // Start button
        Button btnStart = new Button("▶   DÉMARRER");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setStyle(
                "-fx-background-color: #e94560;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 12 0 12 0;"
        );
        btnStart.setOnMouseEntered(e ->
                btnStart.setStyle(btnStart.getStyle().replace("#e94560", "#ff6b80")));
        btnStart.setOnMouseExited(e ->
                btnStart.setStyle(btnStart.getStyle().replace("#ff6b80", "#e94560")));
        btnStart.setOnAction(e -> onStart());
        card.getChildren().add(btnStart);

        // ── Center card in page ───────────────────────────────────────────────
        StackPane center = new StackPane(card);
        center.setPadding(new Insets(0, 0, 40, 0));

        page.setTop(header);
        page.setCenter(center);

        // Fade in animation
        FadeTransition ft = new FadeTransition(Duration.millis(400), page);
        ft.setFromValue(0); ft.setToValue(1); ft.play();

        return page;
    }

    // ─────────────────────────────────────────────
    //  EVENT
    // ─────────────────────────────────────────────

    private void onStart() {
        lblError.setText("");
        try {
            int[]             collection = parseCollection(tfCollection.getText());
            AlgorithmType     type       = nameToType(cbAlgorithm.getValue());
            SimulationSpeed   speed      = cbSpeed.getValue();
            SortingParameters params     = new SortingParameters(collection, type, speed);
            send(AppMediator.EVT_START_SORT, params);
        } catch (Exception ex) {
            lblError.setText("⚠  " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────

    private int[] parseCollection(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("La collection est vide.");
        String[] tokens = trimmed.split(",");
        int[] result = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            try {
                result[i] = Integer.parseInt(tokens[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valeur invalide : \"" + tokens[i].trim() + "\"");
            }
        }
        if (result.length < 2) throw new IllegalArgumentException("Minimum 2 éléments requis.");
        return result;
    }

    private AlgorithmType nameToType(String name) {
        return switch (name) {
            case "Quick Sort" -> AlgorithmType.QUICK_SORT;
            case "Merge Sort" -> AlgorithmType.MERGE_SORT;
            default           -> throw new IllegalArgumentException("Algorithme inconnu.");
        };
    }

    // ── Style helpers ─────────────────────────────────────────────────────────

    private Label label(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #a8a8c0; -fx-font-size: 12;");
        return l;
    }

    private <T> ComboBox<T> styledCombo(ComboBox<T> cb) {
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(
                "-fx-background-color: #0f3460;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13;"
        );
        return cb;
    }

    private TextField styledTextField(TextField tf) {
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle(
                "-fx-background-color: #0f3460;" +
                        "-fx-text-fill: #e0e0ff;" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-size: 13;" +
                        "-fx-padding: 8 10 8 10;"
        );
        return tf;
    }
}