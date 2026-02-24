import javafx.application.Application;
import javafx.stage.Stage;
import mediator.AppMediator;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppMediator mediator = new AppMediator(primaryStage);
        mediator.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}