package com.oncoreminder.models;

public class Patient {
    private int id;
    private String nom;
    private String prenom;
    private String statut;

    public Patient() {}

    public Patient(String nom, String prenom, String statut) {
        this.nom = nom;
        this.prenom = prenom;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    /** Utilisé dans les ComboBox */
    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() { return getNomComplet(); }
}