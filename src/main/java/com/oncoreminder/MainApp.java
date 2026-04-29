package com.oncoreminder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Charger l'interface fusionnée
        URL fxmlUrl = getClass().getResource("/com/oncoreminder/views/dashboard.fxml");

        System.out.println("🔍 FXML URL : " + fxmlUrl);

        if (fxmlUrl == null) {
            System.err.println("❌ FXML introuvable !");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1300, 800);

        // Charger le CSS (thème)
        URL cssUrl = getClass().getResource("/styles/theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
            System.out.println("✅ CSS chargé");
        } else {
            System.out.println("⚠️ CSS non trouvé");
        }

        primaryStage.setTitle("OncoReminder Pro — Association Patient ↔ Cancer & Statistiques");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}