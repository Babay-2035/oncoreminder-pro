package com.oncoreminder.controllers;

import com.oncoreminder.models.Cancer;
import com.oncoreminder.services.ServiceCancer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
public class CancerController implements Initializable {

    @FXML private TableView<Cancer> tableCancer;
    @FXML private TableColumn<Cancer, Integer> colId;
    @FXML private TableColumn<Cancer, String> colNom;
    @FXML private TableColumn<Cancer, String> colClassification;
    @FXML private TableColumn<Cancer, String> colStade;
    @FXML private TableColumn<Cancer, String> colOrgane;

    @FXML private TextField tfRecherche;
    @FXML private Label lblTotal;
    @FXML private VBox panelDetail;
    @FXML private Label lblDetailNom, lblDetailDesc, lblDetailSymp;

    @FXML private Button btnAjouter, btnModifier, btnSupprimer, btnVider;

    private final ServiceCancer service = new ServiceCancer();
    private final ObservableList<Cancer> data = FXCollections.observableArrayList();
    private Cancer selectedCancer = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();

        tableCancer.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                selectedCancer = selected;
                showDetail(selected);
                btnModifier.setDisable(false);
                btnSupprimer.setDisable(false);
            } else {
                btnModifier.setDisable(true);
                btnSupprimer.setDisable(true);
            }
        });

        tfRecherche.textProperty().addListener((obs, old, val) -> onRechercher());
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClassification.setCellValueFactory(new PropertyValueFactory<>("classification"));
        colStade.setCellValueFactory(new PropertyValueFactory<>("stade"));
        colOrgane.setCellValueFactory(new PropertyValueFactory<>("organe"));
        tableCancer.setItems(data);
        tableCancer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellFactory(col -> createCenteredCell());
        colNom.setCellFactory(col -> createCenteredCell());
        colClassification.setCellFactory(col -> createCenteredCell());
        colStade.setCellFactory(col -> createCenteredCell());
        colOrgane.setCellFactory(col -> createCenteredCell());

        tableCancer.setRowFactory(tv -> new TableRow<Cancer>() {
            @Override
            protected void updateItem(Cancer item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                String bg = switch (item.getClassification()) {
                    case "Carcinome" -> "#FFF5F5"; case "Sarcome" -> "#F5F0FF";
                    case "Lymphome" -> "#F0FFF4"; case "Leucémie" -> "#FFFBEB";
                    case "Mélanome" -> "#FFF8F0"; case "Gliome" -> "#F0F8FF";
                    case "Myélome" -> "#F5FBFF"; default -> "#FFFFFF";
                };
                setStyle("-fx-background-color: " + bg + ";");
            }
        });
    }

    // ✅ Cellule centrée générique
    private <T> TableCell<Cancer, T> createCenteredCell() {
        return new TableCell<Cancer, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setStyle("-fx-alignment: CENTER;");
            }
        };
    }

    private void loadData() {
        try {
            data.setAll(service.afficherTous());
            updateStats();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    @FXML private void onAjouter() { openBodySelector(null); }

    @FXML private void onModifier() {
        // ✅ Correction du return invalide
        if (selectedCancer == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une ligne à modifier.");
            return;
        }
        openBodySelector(selectedCancer);
    }

    @FXML private void onSupprimer() {
        // ✅ Correction du return invalide
        if (selectedCancer == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Sélectionnez une ligne à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer : " + selectedCancer.getNom());
        confirm.setContentText("Cette action est irréversible. Continuer ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    service.supprimer(selectedCancer.getId());
                    loadData();
                    onVider();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML private void onVider() {
        selectedCancer = null;
        tableCancer.getSelectionModel().clearSelection();
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        if (panelDetail != null) panelDetail.setVisible(false);
    }

    @FXML private void onRechercher() {
        String kw = tfRecherche.getText().trim();
        try {
            data.setAll(kw.isEmpty() ? service.afficherTous() : service.rechercher(kw));
            updateStats();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    // ✅ Ouverture du formulaire modal (Stage corrigé)
    private void openBodySelector(Cancer cancer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oncoreminder/views/body_selector.fxml"));
            Parent root = loader.load();
            BodySelectorController bsc = loader.getController();

            bsc.setOnSave(cancerObj -> {
                try {
                    if (cancerObj.getId() == 0) service.ajouter(cancerObj);
                    else service.modifier(cancerObj);
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", cancerObj.getId() == 0 ? "Cancer ajouté !" : "Cancer modifié !");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
                }
            });

            if (cancer != null) bsc.setEditMode(cancer);
            else bsc.setAddMode();

            Stage dialog = new Stage();
            dialog.setTitle(cancer != null ? "✏️ Modifier le cancer" : "➕ Ajouter un cancer");

            // ✅ CSS appliqué sur la Scene, pas sur le Stage
            Scene scene = new Scene(root, 980, 620);
            scene.getStylesheets().add(getClass().getResource("/styles/theme.css").toExternalForm());
            dialog.setScene(scene);

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(tableCancer.getScene().getWindow());
            dialog.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void showDetail(Cancer c) {
        if (panelDetail != null) {
            lblDetailNom.setText(c.getNom() + " — Stade " + c.getStade());
            lblDetailDesc.setText(c.getDescription() != null && !c.getDescription().isBlank() ? c.getDescription() : "—");
            lblDetailSymp.setText(c.getSymptomes() != null && !c.getSymptomes().isBlank() ? c.getSymptomes() : "—");
            panelDetail.setVisible(true);
            panelDetail.setManaged(true);
        }
    }

    private void updateStats() {
        try { lblTotal.setText(String.valueOf(service.compter())); }
        catch (SQLException e) { lblTotal.setText("?"); }
    }

    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t, msg);
        a.setTitle(title);
        a.showAndWait();
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