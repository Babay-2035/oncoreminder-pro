package com.oncoreminder.services;

import com.oncoreminder.models.Cancer;
import com.oncoreminder.models.Patient;
import com.oncoreminder.models.PatientCancer;
import com.oncoreminder.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class ServicePatientCancer {

    private final Connection cnx = MyDataBase.getInstance().getCnx();
    private Object Date;

    // ══════════════════════════════════════════════════════════════════════════
    // CRUD — PatientCancer (CAN-03)
    // ══════════════════════════════════════════════════════════════════════════

    /** Associe un patient à un cancer */
    public void associer(PatientCancer pc) throws SQLException {
        String sql = "INSERT INTO patient_cancer (patient_id, cancer_id, date_association, stade_actuel) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, pc.getPatientId());
            ps.setInt(2, pc.getCancerId());
            ps.setDate(3, java.sql.Date.valueOf(pc.getDateAssociation()));
            ps.setString(4, pc.getStadeActuel());
            ps.executeUpdate();
        }
    }

    /** Met à jour le stade d'une association */
    public void modifierStade(int id, String nouveauStade) throws SQLException {
        String sql = "UPDATE patient_cancer SET stade_actuel = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, nouveauStade);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Supprime une association patient-cancer */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM patient_cancer WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Retourne toutes les associations avec noms joints */
    public List<PatientCancer> afficherTous() throws SQLException {
        List<PatientCancer> liste = new ArrayList<>();
        String sql = """
                SELECT pc.id, pc.patient_id, pc.cancer_id, pc.date_association, pc.stade_actuel,
                       p.nom, p.prenom, c.nom AS cancer_nom
                FROM patient_cancer pc
                JOIN patient p ON p.id = pc.patient_id
                JOIN cancer  c ON c.id = pc.cancer_id
                ORDER BY pc.date_association DESC
                """;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PatientCancer pc = new PatientCancer();
                pc.setId(rs.getInt("id"));
                pc.setPatientId(rs.getInt("patient_id"));
                pc.setCancerId(rs.getInt("cancer_id"));
                pc.setDateAssociation(rs.getDate("date_association").toLocalDate());
                pc.setStadeActuel(rs.getString("stade_actuel"));
                pc.setPatientNom(rs.getString("nom"));
                pc.setPatientPrenom(rs.getString("prenom"));
                pc.setCancerNom(rs.getString("cancer_nom"));
                liste.add(pc);
            }
        }
        return liste;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers pour ComboBox
    // ══════════════════════════════════════════════════════════════════════════

    public List<Patient> tousLesPatients() throws SQLException {
        List<Patient> liste = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nom, prenom, statut FROM patient ORDER BY nom")) {
            while (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getInt("id"));
                p.setNom(rs.getString("nom"));
                p.setPrenom(rs.getString("prenom"));
                p.setStatut(rs.getString("statut"));
                liste.add(p);
            }
        }
        return liste;
    }

    public List<Cancer> tousLesCancers() throws SQLException {
        List<Cancer> liste = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, nom, classification, stade FROM cancer ORDER BY nom")) {
            while (rs.next()) {
                Cancer c = new Cancer();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setClassification(rs.getString("classification"));
                c.setStade(rs.getString("stade"));
                liste.add(c);
            }
        }
        return liste;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Statistiques (CAN-05 / CAN-06 / CAN-07)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * CAN-05 — Nombre de patients par cancer
     * Retourne : Map<nomCancer, nbPatients>
     */
    public Map<String, Integer> statParCancer() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
                SELECT c.nom, COUNT(pc.id) AS nb
                FROM cancer c
                LEFT JOIN patient_cancer pc ON pc.cancer_id = c.id
                GROUP BY c.id, c.nom
                ORDER BY nb DESC
                """;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("nom"), rs.getInt("nb"));
            }
        }
        return map;
    }

    /**
     * CAN-06 — Top N cancers les plus fréquents
     */
    public Map<String, Integer> topCancers(int limite) throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
                SELECT c.nom, COUNT(pc.id) AS nb
                FROM cancer c
                JOIN patient_cancer pc ON pc.cancer_id = c.id
                GROUP BY c.id, c.nom
                ORDER BY nb DESC
                LIMIT ?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("nom"), rs.getInt("nb"));
                }
            }
        }
        return map;
    }

    /**
     * CAN-07 — Répartition par stade
     * Retourne : Map<stade, nbPatients>
     */
    public Map<String, Integer> repartitionParStade() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = """
                SELECT stade_actuel, COUNT(*) AS nb
                FROM patient_cancer
                WHERE stade_actuel IS NOT NULL
                GROUP BY stade_actuel
                ORDER BY stade_actuel
                """;
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("stade_actuel"), rs.getInt("nb"));
            }
        }
        return map;
    }

    /** Nombre total d'associations */
    public int compter() throws SQLException {
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patient_cancer")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}