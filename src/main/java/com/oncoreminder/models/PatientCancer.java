package com.oncoreminder.models;

import java.time.LocalDate;

public class PatientCancer {
    private int id;
    private int patientId;
    private int cancerId;
    private LocalDate dateAssociation;
    private String stadeActuel;

    // Champs joints (pour affichage)
    private String patientNom;
    private String patientPrenom;
    private String cancerNom;

    public PatientCancer() {}

    public PatientCancer(int patientId, int cancerId, LocalDate dateAssociation, String stadeActuel) {
        this.patientId = patientId;
        this.cancerId = cancerId;
        this.dateAssociation = dateAssociation;
        this.stadeActuel = stadeActuel;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getCancerId() { return cancerId; }
    public void setCancerId(int cancerId) { this.cancerId = cancerId; }

    public LocalDate getDateAssociation() { return dateAssociation; }
    public void setDateAssociation(LocalDate dateAssociation) { this.dateAssociation = dateAssociation; }

    public String getStadeActuel() { return stadeActuel; }
    public void setStadeActuel(String stadeActuel) { this.stadeActuel = stadeActuel; }

    public String getPatientNom() { return patientNom; }
    public void setPatientNom(String patientNom) { this.patientNom = patientNom; }

    public String getPatientPrenom() { return patientPrenom; }
    public void setPatientPrenom(String patientPrenom) { this.patientPrenom = patientPrenom; }

    public String getCancerNom() { return cancerNom; }
    public void setCancerNom(String cancerNom) { this.cancerNom = cancerNom; }

    /** Nom complet du patient (utile dans les ComboBox) */
    public String getPatientNomComplet() {
        return patientPrenom + " " + patientNom;
    }

    @Override
    public String toString() {
        return getPatientNomComplet() + " → " + cancerNom + " (Stade " + stadeActuel + ")";
    }
}