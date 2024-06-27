import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.*;

public class Calculator extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Calculator.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, Color.TRANSPARENT);// side ko corners transparent
        primaryStage.initStyle(StageStyle.TRANSPARENT);// application closing haru transparent
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
