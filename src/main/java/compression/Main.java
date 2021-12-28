package compression;

import compression.mainView.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
        Parent parent = loader.load();
        MainViewController controller = loader.getController();
        controller.init(primaryStage);

        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(600);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Image compression");

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
        //System.out.println("java.jpg".matches(".*\\.jpg"));
    }
}
