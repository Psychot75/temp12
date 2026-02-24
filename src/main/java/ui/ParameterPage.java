package ui;

import factory.SortingAlgorithmFactory.AlgorithmType;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import mediator.Colleague;
import mediator.Mediator;
import mediator.AppMediator;
import model.SimulationSpeed;
import model.SortingParameters;

public class ParameterPage extends Colleague {

    private final BorderPane root;
    private ComboBox<String> cbAlgorithm;
    private TextField tfCollection;
    private ComboBox<SimulationSpeed> cbSpeed;
    private Label lblError;

    public ParameterPage(Mediator mediator) {
        super(mediator);
        root = buildUI();
    }

    public Pane getRoot() { return root; }

    private BorderPane buildUI() {
        BorderPane page = new BorderPane();
        page.setPadding(new Insets(20));

        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 0, 20, 0));

        Label title = new Label("Tri Visuel");
        title.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");
        Label subtitle = new Label("Visualiseur d'algorithmes de tri");
        header.getChildren().addAll(title, subtitle);

        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(20));
        form.setMaxWidth(420);

        cbAlgorithm = new ComboBox<>(FXCollections.observableArrayList("Quick Sort", "Merge Sort"));
        cbAlgorithm.setValue("Merge Sort");
        cbAlgorithm.setMaxWidth(Double.MAX_VALUE);

        tfCollection = new TextField("50,87,56,12,75,100,20,34,9");
        tfCollection.setMaxWidth(Double.MAX_VALUE);

        cbSpeed = new ComboBox<>(FXCollections.observableArrayList(SimulationSpeed.values()));
        cbSpeed.setValue(SimulationSpeed.FAST);
        cbSpeed.setMaxWidth(Double.MAX_VALUE);

        lblError = new Label("");
        lblError.setStyle("-fx-text-fill: red;");

        Button btnStart = new Button("Demarrer");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        btnStart.setOnAction(e -> onStart());

        form.getChildren().addAll(
                new Label("Algorithme de tri"), cbAlgorithm,
                new Label("Collection d'entiers (separes par virgules)"), tfCollection,
                new Label("Vitesse de simulation"), cbSpeed,
                lblError, btnStart
        );

        StackPane center = new StackPane(form);
        page.setTop(header);
        page.setCenter(center);
        return page;
    }

    private void onStart() {
        lblError.setText("");
        try {
            int[] collection = parseCollection(tfCollection.getText());
            AlgorithmType type = nameToType(cbAlgorithm.getValue());
            SimulationSpeed speed = cbSpeed.getValue();
            send(AppMediator.EVT_START_SORT, new SortingParameters(collection, type, speed));
        } catch (Exception ex) {
            lblError.setText(ex.getMessage());
        }
    }

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
        if (result.length < 2) throw new IllegalArgumentException("Minimum 2 elements requis.");
        return result;
    }

    private AlgorithmType nameToType(String name) {
        return switch (name) {
            case "Quick Sort" -> AlgorithmType.QUICK_SORT;
            case "Merge Sort" -> AlgorithmType.MERGE_SORT;
            default -> throw new IllegalArgumentException("Algorithme inconnu.");
        };
    }
}
