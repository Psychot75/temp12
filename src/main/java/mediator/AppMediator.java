package mediator;

import javafx.scene.Scene;
import javafx.stage.Stage;
import model.SortingParameters;
import ui.ParameterPage;
import ui.VisualizationPage;

/**
 * Concrete Mediator.
 *
 * Owns the Stage and the two Colleague pages.
 * Reacts to events from colleagues and orchestrates transitions:
 *
 *   ParameterPage  → "START_SORT"    → switch to VisualizationPage, start algo
 *   VisualizationPage → "BACK"       → switch back to ParameterPage
 *   VisualizationPage → "SORT_COMPLETE" → enable back button on viz page
 *
 * Neither page knows the other exists.
 */
public class AppMediator implements Mediator {

    // ─── Events ──────────────────────────────────────────────────────────────
    public static final String EVT_START_SORT    = "START_SORT";
    public static final String EVT_BACK          = "BACK";
    public static final String EVT_SORT_COMPLETE = "SORT_COMPLETE";

    // ─── Colleagues ───────────────────────────────────────────────────────────
    private final Stage             stage;
    private final ParameterPage     parameterPage;
    private final VisualizationPage visualizationPage;

    // Cached scenes for fast switching (no re-layout)
    private final Scene paramScene;
    private final Scene vizScene;

    public AppMediator(Stage stage) {
        this.stage = stage;

        // Create colleagues, pass this mediator reference
        parameterPage     = new ParameterPage(this);
        visualizationPage = new VisualizationPage(this);

        paramScene = new Scene(parameterPage.getRoot(), 700, 480);
        vizScene   = new Scene(visualizationPage.getRoot(), 950, 580);

        // Attach CSS
        String css = getClass().getResource("/style.css") != null
                ? getClass().getResource("/style.css").toExternalForm()
                : null;
        if (css != null) {
            paramScene.getStylesheets().add(css);
            vizScene.getStylesheets().add(css);
        }
    }

    /** Show the first page. */
    public void start() {
        showParamPage();
        stage.setTitle("Tri Visuel — LOG121");
        stage.show();
    }

    // ─────────────────────────────────────────────
    //  MEDIATOR — event routing
    // ─────────────────────────────────────────────

    @Override
    public void notify(Colleague sender, String event, Object data) {
        switch (event) {

            case EVT_START_SORT -> {
                // ParameterPage → start a sort
                SortingParameters params = (SortingParameters) data;
                visualizationPage.prepare(params);
                showVizPage();
                visualizationPage.startSort();
            }

            case EVT_BACK -> {
                // VisualizationPage → go back to params
                visualizationPage.stopSort();
                showParamPage();
            }

            case EVT_SORT_COMPLETE -> {
                // VisualizationPage internal — enable its back button
                visualizationPage.onSortComplete();
            }
        }
    }

    // ─────────────────────────────────────────────
    //  PRIVATE
    // ─────────────────────────────────────────────

    private void showParamPage() {
        stage.setScene(paramScene);
        stage.setResizable(false);
    }

    private void showVizPage() {
        stage.setScene(vizScene);
        stage.setResizable(false);
    }
}
