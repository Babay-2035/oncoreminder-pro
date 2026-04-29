package com.oncoreminder.controllers;

import com.oncoreminder.models.Cancer;
import com.oncoreminder.models.Patient;
import com.oncoreminder.models.PatientCancer;
import com.oncoreminder.services.ServicePatientCancer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientCancerStatsController implements Initializable {

    // ========== PARTIE ASSOCIATION ==========
    @FXML private ComboBox<Patient> cbPatient;
    @FXML private ComboBox<Cancer> cbCancer;
    @FXML private ComboBox<String> cbStade;
    @FXML private DatePicker dpDate;
    @FXML private Button btnAjouter, btnModifier;
    @FXML private Label lblStatut, lblCount;
    @FXML private TextField tfRecherche;
    @FXML private TableView<PatientCancer> tableView;
    @FXML private TableColumn<PatientCancer, Integer> colId;
    @FXML private TableColumn<PatientCancer, String> colPatient;
    @FXML private TableColumn<PatientCancer, String> colCancer;
    @FXML private TableColumn<PatientCancer, String> colStade;
    @FXML private TableColumn<PatientCancer, LocalDate> colDate;

    private final ServicePatientCancer service = new ServicePatientCancer();
    private ObservableList<PatientCancer> masterList = FXCollections.observableArrayList();
    private FilteredList<PatientCancer> filteredList;
    private PatientCancer selection = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Association
        cbStade.setItems(FXCollections.observableArrayList("I", "II", "III", "IV"));
        configurerColonnes();
        chargerComboBox();
        chargerTable();
        dpDate.setValue(LocalDate.now());
    }

    private void configurerColonnes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPatientNomComplet()));
        colCancer.setCellValueFactory(new PropertyValueFactory<>("cancerNom"));
        colStade.setCellValueFactory(new PropertyValueFactory<>("stadeActuel"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateAssociation"));
    }

    private void chargerComboBox() {
        try {
            cbPatient.setItems(FXCollections.observableArrayList(service.tousLesPatients()));
            cbCancer.setItems(FXCollections.observableArrayList(service.tousLesCancers()));
        } catch (Exception e) {
            afficherErreur("Erreur chargement listes: " + e.getMessage());
        }
    }

    private void chargerTable() {
        try {
            masterList = FXCollections.observableArrayList(service.afficherTous());
            filteredList = new FilteredList<>(masterList, p -> true);
            tableView.setItems(filteredList);
            lblCount.setText("Total : " + masterList.size() + " association(s)");
        } catch (Exception e) {
            afficherErreur("Erreur chargement: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter() {
        if (!valider()) return;
        try {
            PatientCancer pc = new PatientCancer(
                    cbPatient.getValue().getId(),
                    cbCancer.getValue().getId(),
                    dpDate.getValue(),
                    cbStade.getValue()
            );
            service.associer(pc);
            afficherSucces("Association créée !");
            handleReset();
            chargerTable();
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleModifier() {
        if (selection == null || cbStade.getValue() == null) return;
        try {
            service.modifierStade(selection.getId(), cbStade.getValue());
            afficherSucces("Stade mis à jour !");
            handleReset();
            chargerTable();
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        PatientCancer sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherErreur("Sélectionnez une ligne.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'association ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    service.supprimer(sel.getId());
                    afficherSucces("Supprimé.");
                    chargerTable();
                } catch (Exception e) {
                    afficherErreur("Erreur: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleReset() {
        cbPatient.setValue(null);
        cbCancer.setValue(null);
        cbStade.setValue(null);
        dpDate.setValue(LocalDate.now());
        lblStatut.setText("");
        selection = null;
        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        tableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSelection() {
        PatientCancer sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        selection = sel;
        cbStade.setValue(sel.getStadeActuel());
        btnModifier.setDisable(false);
        btnAjouter.setDisable(true);
        lblStatut.setText("Mode modification — " + sel.getPatientNomComplet());
    }

    @FXML
    private void handleRecherche() {
        String q = tfRecherche.getText().toLowerCase().trim();
        filteredList.setPredicate(pc ->
                q.isEmpty() ||
                        pc.getPatientNomComplet().toLowerCase().contains(q) ||
                        pc.getCancerNom().toLowerCase().contains(q)
        );
        lblCount.setText("Affiché : " + filteredList.size() + " / " + masterList.size());
    }

    private boolean valider() {
        if (cbPatient.getValue() == null) {
            afficherErreur("Sélectionnez un patient.");
            return false;
        }
        if (cbCancer.getValue() == null) {
            afficherErreur("Sélectionnez un cancer.");
            return false;
        }
        if (cbStade.getValue() == null) {
            afficherErreur("Sélectionnez un stade.");
            return false;
        }
        if (dpDate.getValue() == null) {
            afficherErreur("Entrez une date.");
            return false;
        }
        return true;
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        NavigationHelper.goTo(event, "/com/oncoreminder/views/dashboard.fxml");
    }

    @FXML
    private void ouvrirStats(ActionEvent event) {
        NavigationHelper.goTo(event, "/com/oncoreminder/views/statistiques.fxml");
    }

    private void afficherSucces(String msg) {
        lblStatut.setText("✅ " + msg);
        lblStatut.setStyle("-fx-text-fill: #10b981;");
    }

    private void afficherErreur(String msg) {
        lblStatut.setText("❌ " + msg);
        lblStatut.setStyle("-fx-text-fill: #ef4444;");
    }
}