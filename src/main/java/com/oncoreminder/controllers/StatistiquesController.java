package com.oncoreminder.controllers;

import com.oncoreminder.services.ServicePatientCancer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
public class StatistiquesController implements Initializable {

    // ── CAN-05 ────────────────────────────────────────────────────────────────
    @FXML private BarChart<String, Number>   chartParCancer;
    @FXML private CategoryAxis xAxisBarChart;
    // ── CAN-06 ────────────────────────────────────────────────────────────────
    @FXML private PieChart                   chartTop;
    @FXML private Spinner<Integer>           spinnerTop;

    // ── Footer ────────────────────────────────────────────────────────────────
    @FXML private Label lblTotal;

    // ── CAN-07 ────────────────────────────────────────────────────────────────
    @FXML private PieChart                   chartStade;

    private final ServicePatientCancer service = new ServicePatientCancer();

    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        xAxisBarChart.setTickLabelRotation(-30);
        chargerTout();
    }


    // ══════════════════════════════════════════════════════════════════════════
    // Chargement
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleActualiser() { chargerTout(); }

    @FXML
    private void handleTopActualiser() { chargerTop(spinnerTop.getValue()); }

    private void chargerTout() {
        chargerParCancer();
        chargerTop(spinnerTop.getValue());
        chargerParStade();
        chargerTotal();
    }

    // ── CAN-05 ────────────────────────────────────────────────────────────────
    private void chargerParCancer() {
        try {
            Map<String, Integer> data = service.statParCancer();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Patients");
            data.forEach((nom, nb) -> series.getData().add(new XYChart.Data<>(nom, nb)));
            chartParCancer.getData().clear();
            chartParCancer.getData().add(series);
        } catch (Exception e) {
            showError("CAN-05 erreur : " + e.getMessage());
        }
    }

    // ── CAN-06 ────────────────────────────────────────────────────────────────
    private void chargerTop(int limite) {
        try {
            Map<String, Integer> data = service.topCancers(limite);
            ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();
            data.forEach((nom, nb) -> slices.add(new PieChart.Data(nom + " (" + nb + ")", nb)));
            chartTop.setData(slices);
            chartTop.setTitle("Top " + limite + " cancers");
        } catch (Exception e) {
            showError("CAN-06 erreur : " + e.getMessage());
        }
    }

    // ── CAN-07 ────────────────────────────────────────────────────────────────
    private void chargerParStade() {
        try {
            Map<String, Integer> data = service.repartitionParStade();
            int total = data.values().stream().mapToInt(Integer::intValue).sum();

            // Pie chart
            ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();
            data.forEach((stade, nb) -> slices.add(new PieChart.Data("Stade " + stade, nb)));
            chartStade.setData(slices);
            chartStade.setTitle("Répartition par stade");

        } catch (Exception e) {
            showError("CAN-07 erreur : " + e.getMessage());
        }
    }

    // ── Footer total ──────────────────────────────────────────────────────────
    private void chargerTotal() {
        try {
            lblTotal.setText("Total associations : " + service.compter());
        } catch (Exception e) {
            lblTotal.setText("Total : ?");
        }
    }

    private void showError(String msg) {
        System.err.println("⚠ StatistiquesController — " + msg);
    }
    @FXML
    private void handleRetour(ActionEvent event) {
        NavigationHelper.goTo(event, "/com/oncoreminder/views/dashboard.fxml");
    }

    // Note : si le contrôleur ouvre lui-même d'autres vues (ex: PatientCancerController
// qui ouvre les stats), utiliser le même pattern :
    @FXML
    private void ouvrirStats(ActionEvent event) {
        NavigationHelper.goTo(event, "/com/oncoreminder/views/statistiques.fxml");
    }
}