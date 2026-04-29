package com.oncoreminder.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class DashboardController {

    /**
     * Navigation single-window : récupère le Stage depuis l'événement,
     * remplace juste le root de la scène existante — pas de nouvelle fenêtre.
     */
    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openCancer(ActionEvent event) {
        navigateTo(event, "/com/oncoreminder/views/cancer.fxml");
    }

    @FXML
    private void openPatientCancer(ActionEvent event) {
        navigateTo(event, "/com/oncoreminder/views/patient_cancer_stats.fxml");
    }


}