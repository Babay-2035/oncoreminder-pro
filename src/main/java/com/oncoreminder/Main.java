package com.oncoreminder;

import com.oncoreminder.models.Cancer;
import com.oncoreminder.services.ServiceCancer;
import com.oncoreminder.utils.MyDataBase;

public class Main {

    public static void main(String[] args) {

        // Test connexion
        MyDataBase.getInstance().getCnx();

        ServiceCancer sc = new ServiceCancer();

        // ── Ajouter un cancer ──────────────────────────
        Cancer c = new Cancer();
        c.setNom("Cancer du sein");
        c.setClassification("Carcinome");
        c.setStade("II");
        c.setOrgane("Sein");
        c.setDescription("Tumeur maligne du tissu mammaire");
        c.setSymptomes("Boule dans le sein, douleur, écoulement");

        try {
            sc.ajouter(c);
            System.out.println("✅ Cancer ajouté avec succès !");
        } catch (Exception e) {
            System.out.println("❌ Erreur ajout : " + e.getMessage());
        }

        // ── Afficher tous les cancers ──────────────────
        try {
            System.out.println("\n📋 Liste des cancers :");
            for (Cancer cancer : sc.afficherTous()) {
                System.out.println("   " + cancer.getId() + " - " + cancer.getNom()
                        + " | " + cancer.getClassification()
                        + " | Stade " + cancer.getStade()
                        + " | " + cancer.getOrgane());
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur affichage : " + e.getMessage());
        }

        // ── Compter ────────────────────────────────────
        try {
            System.out.println("\n🔢 Total : " + sc.compter() + " type(s) de cancer");
        } catch (Exception e) {
            System.out.println("❌ Erreur comptage : " + e.getMessage());
        }

    }

}