package com.gestion.dao;

import com.gestion.database.DatabaseConnection;
import com.gestion.model.DossierMedical;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DossierMedicalDAO {

    public void create(DossierMedical dossier) {
        String sql = "INSERT INTO dossier_medical (patient_id, date_creation, antecedents, allergies, notes) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, dossier.getPatientId());
            stmt.setDate(2, Date.valueOf(dossier.getDateCreation()));
            stmt.setString(3, dossier.getAntecedents());
            stmt.setString(4, dossier.getAllergies());
            stmt.setString(5, dossier.getNotes());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                dossier.setId(rs.getLong(1));
            }
            rs.close();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error creating dossier medical: " + e.getMessage(), e);
        }
    }

    public List<DossierMedical> findAll() {
        List<DossierMedical> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier_medical";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                dossiers.add(mapResultSetToDossier(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching dossiers: " + e.getMessage(), e);
        }
        return dossiers;
    }

    public Optional<DossierMedical> findById(Long id) {
        String sql = "SELECT * FROM dossier_medical WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                DossierMedical dossier = mapResultSetToDossier(rs);
                rs.close();
                return Optional.of(dossier);
            }
            rs.close();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching dossier: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<DossierMedical> findByPatientId(Long patientId) {
        String sql = "SELECT * FROM dossier_medical WHERE patient_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, patientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                DossierMedical dossier = mapResultSetToDossier(rs);
                rs.close();
                return Optional.of(dossier);
            }
            rs.close();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching dossier by patient: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void update(DossierMedical dossier) {
        String sql = "UPDATE dossier_medical SET patient_id = ?, date_creation = ?, " +
                     "antecedents = ?, allergies = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, dossier.getPatientId());
            stmt.setDate(2, Date.valueOf(dossier.getDateCreation()));
            stmt.setString(3, dossier.getAntecedents());
            stmt.setString(4, dossier.getAllergies());
            stmt.setString(5, dossier.getNotes());
            stmt.setLong(6, dossier.getId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating dossier: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM dossier_medical WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting dossier: " + e.getMessage(), e);
        }
    }

    private DossierMedical mapResultSetToDossier(ResultSet rs) throws SQLException {
        DossierMedical dossier = new DossierMedical();
        dossier.setId(rs.getLong("id"));
        dossier.setPatientId(rs.getLong("patient_id"));
        dossier.setDateCreation(rs.getDate("date_creation").toLocalDate());
        dossier.setAntecedents(rs.getString("antecedents"));
        dossier.setAllergies(rs.getString("allergies"));
        dossier.setNotes(rs.getString("notes"));
        return dossier;
    }
}
