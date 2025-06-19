package com.evoting.model;

public class Administrator extends User {
    public Administrator(String login, String password) {
        super(login, password, "Администратор", Role.ADMINISTRATOR);
    }
}