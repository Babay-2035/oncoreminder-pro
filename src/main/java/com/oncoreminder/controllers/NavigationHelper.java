package com.oncoreminder.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * Utilitaire de navigation single-window.
 *
 * Usage dans chaque contrôleur :
 *   @FXML
 *   private void handleRetour(ActionEvent event) {
 *       NavigationHelper.goTo(event, "/com/oncoreminder/views/dashboard.fxml");
 *   }
 */
public class NavigationHelper {

    public static void goTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(NavigationHelper.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}