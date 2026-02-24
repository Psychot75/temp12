package mediator;

import javafx.scene.Scene;
import javafx.stage.Stage;
import model.SortingParameters;
import ui.ParameterPage;
import ui.VisualizationPage;

public class AppMediator implements Mediator {

    public static final String EVT_START_SORT = "START_SORT";
    public static final String EVT_BACK = "BACK";
    public static final String EVT_SORT_COMPLETE = "SORT_COMPLETE";

    private final Stage stage;
    private final ParameterPage parameterPage;
    private final VisualizationPage visualizationPage;
    private final Scene paramScene;
    private final Scene vizScene;

    public AppMediator(Stage stage) {
        this.stage = stage;
        parameterPage = new ParameterPage(this);
        visualizationPage = new VisualizationPage(this);
        paramScene = new Scene(parameterPage.getRoot(), 700, 480);
        vizScene = new Scene(visualizationPage.getRoot(), 950, 580);
    }

    public void start() {
        stage.setScene(paramScene);
        stage.setResizable(false);
        stage.setTitle("Tri Visuel");
        stage.show();
    }

    @Override
    public void notify(Colleague sender, String event, Object data) {
        switch (event) {
            case EVT_START_SORT -> {
                SortingParameters params = (SortingParameters) data;
                visualizationPage.prepare(params);
                stage.setScene(vizScene);
                visualizationPage.startSort();
            }
            case EVT_BACK -> {
                visualizationPage.stopSort();
                stage.setScene(paramScene);
            }
            case EVT_SORT_COMPLETE -> visualizationPage.onSortComplete();
        }
    }
}
