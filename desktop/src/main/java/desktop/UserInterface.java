package desktop;
import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.transform.*;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;
import core.AppEngine;

// This is where most of the UI is handled
public class UserInterface extends Application {

    private static UserInterface instance;
    private Stage primaryStage;
    private StackPane mainContainer;

    public UserInterface() {
        instance = this;
    }
    public static UserInterface getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        // AppEngine engine = new AppEngine();
        // engine.start();
        System.out.println("This is JavaFX UI thread");

        this.primaryStage = primaryStage;
        this.primaryStage.setMinHeight(550);
        this.primaryStage.setMinWidth(650);

        mainContainer = new StackPane();
        Scene mainScene = new Scene(mainContainer);

        String cssPath = Objects.requireNonNull(getClass().getResource("/desktop/styles.css")).toExternalForm();
        mainScene.getStylesheets().add(cssPath);

        Font.loadFont(getClass().getResourceAsStream("/desktop/fonts/Inter-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/desktop/fonts/Inter-Bold.ttf"), 14);

        this.primaryStage.setScene(mainScene);
        this.primaryStage.setMaximized(true);

        showMainUI(false);
        this.primaryStage.show();
    }

    public void showLoadingScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("loading.fxml"));
                Parent root = loader.load();

                mainContainer.getChildren().setAll(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void showMainUI(boolean loggedIn) {
        Platform.runLater(() -> {
            String fxmlFile = loggedIn ? "main.fxml" : "onboarding.fxml";
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/desktop/" + fxmlFile));
                Parent root = loader.load();
                double scaleFactor = 1.0; // Adjust this value to scale the UI

                Scale scale = new Scale(scaleFactor, scaleFactor);
                scale.setPivotX(0);
                scale.setPivotY(0);
                root.getTransforms().add(scale);

                mainContainer.getChildren().setAll(root);

                if (primaryStage != null) {
                    primaryStage.setTitle("Paged - " + (loggedIn ? "Chat" : "Onboarding"));
                }
            } catch (IOException e) {
                System.err.println("Failed to load FXML: " + fxmlFile + e.getMessage());
                e.printStackTrace();
            }
        });
    }

}
