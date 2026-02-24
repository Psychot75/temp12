import javafx.application.Application;
import javafx.stage.Stage;
import mediator.AppMediator;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        new AppMediator(primaryStage).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
