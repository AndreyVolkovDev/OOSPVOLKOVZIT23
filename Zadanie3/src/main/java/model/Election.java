package com.evoting.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Election {
    private UUID id;
    private String name;
    private LocalDateTime endDate;
    private Set<UUID> candidateIds;
    private boolean isFinished;

    public Election(String name, LocalDateTime endDate) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.endDate = endDate;
        this.candidateIds = new HashSet<>();
        this.isFinished = false;
    }

    public boolean isActive() {
        return LocalDateTime.now().isBefore(endDate) && !isFinished;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getEndDate() { return endDate; }
    public Set<UUID> getCandidateIds() { return candidateIds; }
    public void addCandidate(UUID candidateId) { candidateIds.add(candidateId); }

    @Override
    public String toString() {
        return "ID: " + id + ", Название: '" + name + "', Окончание: " + endDate + ", Статус: " + (isActive() ? "Активно" : "Завершено");
    }
}