package com.gestion.model;

import java.time.LocalDate;

public class Patient {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateNaissance;
    private String phone;
    private String address;
    private PatientStatus status;

    public enum PatientStatus {
        ACTIVE, FOLLOW_UP
    }

    public Patient() {}

    public Patient(String firstName, String lastName, LocalDate dateNaissance, 
                   String phone, String address, PatientStatus status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateNaissance = dateNaissance;
        this.phone = phone;
        this.address = address;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Patient[id=%d, %s %s, born=%s, phone=%s, address=%s, status=%s]",
                id, firstName, lastName, dateNaissance, phone, address, status);
    }
}
