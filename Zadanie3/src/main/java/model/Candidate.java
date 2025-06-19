package com.evoting.model;

import java.time.LocalDate;

public class Candidate extends User {
    private String biography;
    private LocalDate dateOfBirth;

    public Candidate(String login, String password, String fullName) {
        super(login, password, fullName, Role.CANDIDATE);
    }

    // Getters and Setters
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    @Override
    public String toString() {
        return super.toString() + (biography != null ? ", Биография: " + biography : "");
    }
}