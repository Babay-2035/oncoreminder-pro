package com.gestion.dao;

import com.gestion.database.DatabaseConnection;
import com.gestion.model.Patient;
import com.gestion.model.Patient.PatientStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAO {

    public void create(Patient patient) {
        String sql = "INSERT INTO patient (first_name, last_name, date_naissance, phone, address, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setDate(3, Date.valueOf(patient.getDateNaissance()));
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getStatus().name());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                patient.setId(rs.getLong(1));
            }
            rs.close();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error creating patient: " + e.getMessage(), e);
        }
    }

    public List<Patient> findAll() {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                patients.add(mapResultSetToPatient(rs));
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching patients: " + e.getMessage(), e);
        }
        return patients;
    }

    public Optional<Patient> findById(Long id) {
        String sql = "SELECT * FROM patient WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Patient patient = mapResultSetToPatient(rs);
                rs.close();
                return Optional.of(patient);
            }
            rs.close();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching patient: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void update(Patient patient) {
        String sql = "UPDATE patient SET first_name = ?, last_name = ?, date_naissance = ?, " +
                     "phone = ?, address = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setDate(3, Date.valueOf(patient.getDateNaissance()));
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getStatus().name());
            stmt.setLong(7, patient.getId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error updating patient: " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM patient WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting patient: " + e.getMessage(), e);
        }
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setId(rs.getLong("id"));
        patient.setFirstName(rs.getString("first_name"));
        patient.setLastName(rs.getString("last_name"));
        patient.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
        patient.setPhone(rs.getString("phone"));
        patient.setAddress(rs.getString("address"));
        patient.setStatus(PatientStatus.valueOf(rs.getString("status")));
        return patient;
    }
}
