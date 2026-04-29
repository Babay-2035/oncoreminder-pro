package com.gestion;

import com.gestion.dao.DossierMedicalDAO;
import com.gestion.dao.PatientDAO;
import com.gestion.model.DossierMedical;
import com.gestion.database.DatabaseConnection;
import com.gestion.model.Patient;
import com.gestion.model.Patient.PatientStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final PatientDAO patientDAO = new PatientDAO();
    private static final DossierMedicalDAO dossierDAO = new DossierMedicalDAO();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        // Test database connection
        testConnection();
        
        while (true) {
            printMenu();
            int choice = readInt("Choice: ");

            switch (choice) {
                case 1 -> addPatient();
                case 2 -> listPatients();
                case 3 -> updatePatient();
                case 4 -> deletePatient();
                case 5 -> addDossier();
                case 6 -> listDossiers();
                case 7 -> updateDossier();
                case 8 -> deleteDossier();
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
            System.out.println();
        }
    }

    private static void printMenu() {
        System.out.println("=== GESTION PATIENT ===");
        System.out.println("-- PATIENT --");
        System.out.println("1. Add Patient");
        System.out.println("2. List All Patients");
        System.out.println("3. Update Patient");
        System.out.println("4. Delete Patient");
        System.out.println("-- DOSSIER MEDICAL --");
        System.out.println("5. Add Dossier Medical");
        System.out.println("6. List All Dossiers");
        System.out.println("7. Update Dossier");
        System.out.println("8. Delete Dossier");
        System.out.println("0. Exit");
        System.out.println("=======================");
    }

    private static void testConnection() {
        System.out.println("=== Testing Database Connection ===");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✓ Connection successful!");
            System.out.println("  Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("  Version: " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("  URL: " + conn.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("✗ Connection failed: " + e.getMessage());
            System.exit(1);
        }
        System.out.println("===================================\n");
    }

    private static void addPatient() {
        System.out.println("\n--- Add Patient ---");
        
        String firstName = readString("First Name: ");
        String lastName = readString("Last Name: ");
        LocalDate dateNaissance = readDate("Date of Birth (yyyy-MM-dd): ");
        String phone = readString("Phone: ");
        String address = readString("Address: ");
        PatientStatus status = readStatus("Status (ACTIVE/FOLLOW_UP): ");

        Patient patient = new Patient(firstName, lastName, dateNaissance, phone, address, status);
        patientDAO.create(patient);
        System.out.println("Patient added successfully! ID: " + patient.getId());
    }

    private static void listPatients() {
        System.out.println("\n--- Patient List ---");
        List<Patient> patients = patientDAO.findAll();
        
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
            return;
        }
        
        for (Patient p : patients) {
            System.out.println(p);
        }
        System.out.println("Total: " + patients.size() + " patients");
    }

    private static void updatePatient() {
        System.out.println("\n--- Update Patient ---");
        Long id = readLong("Patient ID: ");
        
        Optional<Patient> existing = patientDAO.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Patient not found.");
            return;
        }
        
        Patient patient = existing.get();
        System.out.println("Current: " + patient);
        System.out.println("(Press Enter to keep current value)");
        
        String firstName = readStringWithDefault("First Name", patient.getFirstName());
        String lastName = readStringWithDefault("Last Name", patient.getLastName());
        LocalDate dateNaissance = readDateWithDefault("Date of Birth (yyyy-MM-dd)", patient.getDateNaissance());
        String phone = readStringWithDefault("Phone", patient.getPhone());
        String address = readStringWithDefault("Address", patient.getAddress());
        PatientStatus status = readStatusWithDefault("Status (ACTIVE/FOLLOW_UP)", patient.getStatus());

        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateNaissance(dateNaissance);
        patient.setPhone(phone);
        patient.setAddress(address);
        patient.setStatus(status);
        
        patientDAO.update(patient);
        System.out.println("Patient updated successfully!");
    }

    private static void deletePatient() {
        System.out.println("\n--- Delete Patient ---");
        Long id = readLong("Patient ID: ");
        
        Optional<Patient> existing = patientDAO.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Patient not found.");
            return;
        }
        
        System.out.println("Patient to delete: " + existing.get());
        String confirm = readString("Confirm deletion (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes")) {
            patientDAO.delete(id);
            System.out.println("Patient deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ==================== DOSSIER MEDICAL CRUD ====================

    private static void addDossier() {
        System.out.println("\n--- Add Dossier Medical ---");
        
        Long patientId = readLong("Patient ID: ");
        Optional<Patient> patient = patientDAO.findById(patientId);
        if (patient.isEmpty()) {
            System.out.println("Patient not found.");
            return;
        }
        
        LocalDate dateCreation = readDate("Date Creation (yyyy-MM-dd): ");
        String antecedents = readString("Antecedents: ");
        String allergies = readString("Allergies: ");
        String notes = readString("Notes: ");

        DossierMedical dossier = new DossierMedical(patientId, dateCreation, antecedents, allergies, notes);
        dossierDAO.create(dossier);
        System.out.println("Dossier medical added successfully! ID: " + dossier.getId());
    }

    private static void listDossiers() {
        System.out.println("\n--- Dossier Medical List ---");
        List<DossierMedical> dossiers = dossierDAO.findAll();
        
        if (dossiers.isEmpty()) {
            System.out.println("No dossiers found.");
            return;
        }
        
        for (DossierMedical d : dossiers) {
            System.out.println(d);
        }
        System.out.println("Total: " + dossiers.size() + " dossiers");
    }

    private static void updateDossier() {
        System.out.println("\n--- Update Dossier Medical ---");
        Long id = readLong("Dossier ID: ");
        
        Optional<DossierMedical> existing = dossierDAO.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Dossier not found.");
            return;
        }
        
        DossierMedical dossier = existing.get();
        System.out.println("Current: " + dossier);
        System.out.println("(Press Enter to keep current value)");
        
        Long patientId = readLongWithDefault("Patient ID", dossier.getPatientId());
        Optional<Patient> patient = patientDAO.findById(patientId);
        if (patient.isEmpty()) {
            System.out.println("Patient not found. Keeping current value.");
        } else {
            dossier.setPatientId(patientId);
        }
        
        LocalDate dateCreation = readDateWithDefault("Date Creation (yyyy-MM-dd)", dossier.getDateCreation());
        String antecedents = readStringWithDefault("Antecedents", dossier.getAntecedents());
        String allergies = readStringWithDefault("Allergies", dossier.getAllergies());
        String notes = readStringWithDefault("Notes", dossier.getNotes());

        dossier.setDateCreation(dateCreation);
        dossier.setAntecedents(antecedents);
        dossier.setAllergies(allergies);
        dossier.setNotes(notes);
        
        dossierDAO.update(dossier);
        System.out.println("Dossier updated successfully!");
    }

    private static void deleteDossier() {
        System.out.println("\n--- Delete Dossier Medical ---");
        Long id = readLong("Dossier ID: ");
        
        Optional<DossierMedical> existing = dossierDAO.findById(id);
        if (existing.isEmpty()) {
            System.out.println("Dossier not found.");
            return;
        }
        
        System.out.println("Dossier to delete: " + existing.get());
        String confirm = readString("Confirm deletion (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes")) {
            dossierDAO.delete(id);
            System.out.println("Dossier deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // ==================== HELPER METHODS ====================

    private static Long readLongWithDefault(String prompt, Long currentValue) {
        System.out.printf("%s [%d]: ", prompt, currentValue);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return currentValue;
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Keeping current value.");
            return currentValue;
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static Long readLong(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine().trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd. Try again.");
            }
        }
    }

    private static PatientStatus readStatus(String prompt) {
        while (true) {
            String input = readString(prompt).toUpperCase();
            try {
                return PatientStatus.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status. Use ACTIVE or FOLLOW_UP.");
            }
        }
    }

    private static String readStringWithDefault(String prompt, String currentValue) {
        System.out.printf("%s [%s]: ", prompt, currentValue);
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? currentValue : input;
    }

    private static LocalDate readDateWithDefault(String prompt, LocalDate currentValue) {
        System.out.printf("%s [%s]: ", prompt, currentValue);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return currentValue;
        try {
            return LocalDate.parse(input, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date. Keeping current value.");
            return currentValue;
        }
    }

    private static PatientStatus readStatusWithDefault(String prompt, PatientStatus currentValue) {
        System.out.printf("%s [%s]: ", prompt, currentValue);
        String input = scanner.nextLine().trim().toUpperCase();
        if (input.isEmpty()) return currentValue;
        try {
            return PatientStatus.valueOf(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status. Keeping current value.");
            return currentValue;
        }
    }
}
