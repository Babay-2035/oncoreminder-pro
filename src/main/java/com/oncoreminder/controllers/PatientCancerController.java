package com.oncoreminder.controllers;

import com.oncoreminder.models.Cancer;
import com.oncoreminder.models.Patient;
import com.oncoreminder.models.PatientCancer;
import com.oncoreminder.services.ServicePatientCancer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientCancerController implements Initializable {

    // ── FXML injections ───────────────────────────────────────────────────────
    @FXML private ComboBox<Patient>  cbPatient;
    @FXML private ComboBox<Cancer>   cbCancer;
    @FXML private ComboBox<String>   cbStade;
    @FXML private DatePicker         dpDate;
    @FXML private Button             btnAjouter;
    @FXML private Button             btnModifier;
    @FXML private Label              lblStatut;
    @FXML private Label              lblCount;
    @FXML private TextField          tfRecherche;

    @FXML private TableView<PatientCancer>       tableView;
    @FXML private TableColumn<PatientCancer,Integer>    colId;
    @FXML private TableColumn<PatientCancer,String>     colPatient;
    @FXML private TableColumn<PatientCancer,String>     colCancer;
    @FXML private TableColumn<PatientCancer,String>     colStade;
    @FXML private TableColumn<PatientCancer,LocalDate>  colDate;

    // ── État interne ──────────────────────────────────────────────────────────
    private final ServicePatientCancer service = new ServicePatientCancer();
    private ObservableList<PatientCancer> masterList = FXCollections.observableArrayList();
    private FilteredList<PatientCancer>   filteredList;
    private PatientCancer selection = null;

    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Remplir le ComboBox des stades
        cbStade.setItems(FXCollections.observableArrayList("I", "II", "III", "IV"));

        configurerColonnes();
        chargerComboBox();
        chargerTable();
        dpDate.setValue(LocalDate.now());
    }

    // ── Configuration colonnes ────────────────────────────────────────────────
    private void configurerColonnes() {
        colId     .setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getPatientNomComplet()));
        colCancer .setCellValueFactory(new PropertyValueFactory<>("cancerNom"));
        colStade  .setCellValueFactory(new PropertyValueFactory<>("stadeActuel"));
        colDate   .setCellValueFactory(new PropertyValueFactory<>("dateAssociation"));
    }

    // ── Chargement ComboBox ───────────────────────────────────────────────────
    private void chargerComboBox() {
        try {
            cbPatient.setItems(FXCollections.observableArrayList(service.tousLesPatients()));
            cbCancer .setItems(FXCollections.observableArrayList(service.tousLesCancers()));
        } catch (Exception e) {
            afficherErreur("Impossible de charger les listes : " + e.getMessage());
        }
    }

    // ── Chargement tableau ────────────────────────────────────────────────────
    private void chargerTable() {
        try {
            masterList = FXCollections.observableArrayList(service.afficherTous());
            filteredList = new FilteredList<>(masterList, p -> true);
            tableView.setItems(filteredList);
            lblCount.setText("Total : " + masterList.size() + " association(s)");
        } catch (Exception e) {
            afficherErreur("Erreur chargement : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Actions
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAjouter() {
        if (!valider()) return;
        try {
            PatientCancer pc = new PatientCancer(
                    cbPatient.getValue().getId(),
                    cbCancer .getValue().getId(),
                    dpDate.getValue(),
                    cbStade.getValue()
            );
            service.associer(pc);
            afficherSucces("Association créée avec succès !");
            handleReset();
            chargerTable();
        } catch (Exception e) {
            afficherErreur("Erreur : " + e.getMessage());
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
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer() {
        PatientCancer sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            afficherErreur("Sélectionnez une ligne à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'association de " + sel.getPatientNomComplet() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation");
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    service.supprimer(sel.getId());
                    afficherSucces("Association supprimée.");
                    chargerTable();
                } catch (Exception e) {
                    afficherErreur("Erreur : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleReset() {
        cbPatient.setValue(null);
        cbCancer .setValue(null);
        cbStade  .setValue(null);
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
        lblStatut.setStyle("-fx-text-fill: #f59e0b;");
    }

    @FXML
    private void handleRecherche() {
        String q = tfRecherche.getText().toLowerCase().trim();
        filteredList.setPredicate(pc ->
                q.isEmpty()
                        || pc.getPatientNomComplet().toLowerCase().contains(q)
                        || pc.getCancerNom().toLowerCase().contains(q)
                        || (pc.getStadeActuel() != null && pc.getStadeActuel().toLowerCase().contains(q))
        );
        lblCount.setText("Affiché : " + filteredList.size() + " / " + masterList.size());
    }

    @FXML
    private void ouvrirStats() {
        try {
            URL fxmlUrl = getClass().getResource("/com/oncoreminder/views/statistiques.fxml");
            if (fxmlUrl == null) {
                afficherErreur("Fichier statistiques.fxml introuvable.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Statistiques — Cancers");
            stage.setScene(new Scene(root, 900, 600));
            stage.show();
        } catch (Exception e) {
            afficherErreur("Impossible d'ouvrir les statistiques : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Validation & messages
    // ══════════════════════════════════════════════════════════════════════════

    private boolean valider() {
        if (cbPatient.getValue() == null) { afficherErreur("Sélectionnez un patient."); return false; }
        if (cbCancer .getValue() == null) { afficherErreur("Sélectionnez un cancer.");  return false; }
        if (cbStade  .getValue() == null) { afficherErreur("Sélectionnez un stade.");   return false; }
        if (dpDate.getValue()    == null) { afficherErreur("Entrez une date.");          return false; }
        return true;
    }

    private void afficherSucces(String msg) {
        lblStatut.setText("✅ " + msg);
        lblStatut.setStyle("-fx-text-fill: #10b981;");
    }

    private void afficherErreur(String msg) {
        lblStatut.setText("❌ " + msg);
        lblStatut.setStyle("-fx-text-fill: #ef4444;");
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