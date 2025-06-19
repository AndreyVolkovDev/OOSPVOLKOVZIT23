package com.evoting.model;

import java.util.UUID;

public abstract class User {
    protected UUID id;
    protected String login;
    protected String passwordHash; // Храним хеш, а не пароль
    protected String fullName;
    protected Role role;

    public User(String login, String password, String fullName, Role role) {
        this.id = UUID.randomUUID();
        this.login = login;
        // В реальном приложении используйте bcrypt
        this.passwordHash = String.valueOf(password.hashCode());
        this.fullName = fullName;
        this.role = role;
    }

    // Getters
    public UUID getId() { return id; }
    public String getLogin() { return login; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }

    public boolean checkPassword(String password) {
        return this.passwordHash.equals(String.valueOf(password.hashCode()));
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Логин: " + login + ", ФИО: " + fullName + ", Роль: " + role;
    }
}