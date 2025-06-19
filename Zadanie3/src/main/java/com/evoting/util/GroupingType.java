package com.evoting.service;

public enum GroupingType {
    NONE("Без группировки"),
    BY_CITY("По городу"),
    BY_AGE_GROUP("По возрасту");

    private final String description;

    GroupingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}