package com.evoting.model;

import java.time.LocalDate;
import java.time.Period; // Импортируем Period для вычисления возраста
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Voter extends User {
    private final LocalDate dateOfBirth;
    private final String snils;
    private String city; // <-- НОВОЕ ПОЛЕ
    private Map<UUID, UUID> votes;

    public Voter(String login, String password, String fullName, LocalDate dateOfBirth, String snils, String city) { // <-- ИЗМЕНЯЕМ КОНСТРУКТОР
        super(login, password, fullName, Role.VOTER);
        this.dateOfBirth = dateOfBirth;
        this.snils = snils;
        this.city = city; // <-- ИНИЦИАЛИЗИРУЕМ
        this.votes = new HashMap<>();
    }

    public void addVote(UUID electionId, UUID candidateId) {
        this.votes.put(electionId, candidateId);
    }

    // --- Getters & Setters ---
    public Map<UUID, UUID> getVotes() { return votes; }
    public String getSnils() { return snils; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getCity() { return city; } // <-- GETTER ДЛЯ ГОРОДА
    public void setCity(String city) { this.city = city; } // <-- SETTER ДЛЯ ГОРОДА

    /**
     * Вспомогательный метод для определения возрастной группы избирателя.
     * @return Строка с названием возрастной группы.
     */
    public String getAgeGroup() {
        if (dateOfBirth == null) return "Неизвестно";
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age >= 18 && age <= 30) return "18-30 лет";
        if (age >= 31 && age <= 50) return "31-50 лет";
        if (age >= 51) return "51+ лет";
        return "До 18 лет";
    }
}