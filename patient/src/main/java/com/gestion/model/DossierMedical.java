package com.gestion.model;

import java.time.LocalDate;

public class DossierMedical {
    private Long id;
    private Long patientId;
    private LocalDate dateCreation;
    private String antecedents;
    private String allergies;
    private String notes;
    private Patient patient;

    public DossierMedical() {}

    public DossierMedical(Long patientId, LocalDate dateCreation, String antecedents, 
                          String allergies, String notes) {
        this.patientId = patientId;
        this.dateCreation = dateCreation;
        this.antecedents = antecedents;
        this.allergies = allergies;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public String getAntecedents() { return antecedents; }
    public void setAntecedents(String antecedents) { this.antecedents = antecedents; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    @Override
    public String toString() {
        return String.format("DossierMedical[id=%d, patientId=%d, dateCreation=%s, antecedents=%s, allergies=%s, notes=%s]",
                id, patientId, dateCreation, antecedents, allergies, notes);
    }
}
