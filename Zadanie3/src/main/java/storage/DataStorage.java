package com.evoting.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.evoting.model.*;
import com.evoting.util.LocalDateAdapter;
import com.evoting.util.LocalDateTimeAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataStorage {
    private static final String VOTERS_FILE = "voters.json";
    private static final String STAFF_FILE = "staff.json";
    private static final String ELECTIONS_FILE = "elections.json";

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private List<User> allUsers;
    private List<Election> elections;

    public DataStorage() {
        List<Voter> voters = loadVoters();
        List<User> staff = new ArrayList<>();
        this.allUsers = Stream.concat(voters.stream(), staff.stream()).collect(Collectors.toList());
        this.elections = loadElections();
        createDefaultUsersAndData();
    }

    /**
     * Метод для создания тестовых пользователей и данных, если они отсутствуют.
     * Он ничего не возвращает, только модифицирует поля класса.
     */
    private void createDefaultUsersAndData() { // <-- ИСПРАВЛЕНИЕ ЗДЕСЬ
        // Проверяем и создаем администратора
        if (this.allUsers.stream().noneMatch(u -> "admin".equals(u.getLogin()))) {
            System.out.println("Администратор не найден. Создается пользователь: admin/admin");
            this.allUsers.add(new Administrator("admin", "admin"));
        }

        // Проверяем и создаем ЦИК
        if (this.allUsers.stream().noneMatch(u -> "cec1".equals(u.getLogin()))) {
            System.out.println("Создается тестовый ЦИК: cec1/cec1");
            this.allUsers.add(new CEC("cec1", "cec1"));
        }

        // Проверяем и создаем кандидатов
        Candidate candidate1 = (Candidate) this.allUsers.stream()
                .filter(u -> "kandidat_ivanov".equals(u.getLogin()))
                .findFirst().orElse(null);
        if (candidate1 == null) {
            System.out.println("Создается тестовый кандидат: kandidat_ivanov/123");
            candidate1 = new Candidate("kandidat_ivanov", "123", "Иванов Иван Иванович");
            candidate1.setBiography("Опытный политик, обещает светлое будущее.");
            candidate1.setDateOfBirth(LocalDate.of(1980, 5, 15));
            this.allUsers.add(candidate1);
        }

        Candidate candidate2 = (Candidate) this.allUsers.stream()
                .filter(u -> "kandidat_petrov".equals(u.getLogin()))
                .findFirst().orElse(null);
        if (candidate2 == null) {
            System.out.println("Создается тестовый кандидат: kandidat_petrov/123");
            candidate2 = new Candidate("kandidat_petrov", "123", "Петров Петр Петрович");
            candidate2.setBiography("Молодой и амбициозный, выступает за перемены.");
            candidate2.setDateOfBirth(LocalDate.of(1992, 8, 22));
            this.allUsers.add(candidate2);
        }

        // Проверяем и создаем пользователей
        if (this.allUsers.stream().noneMatch(u -> "voter_sidorov".equals(u.getLogin()))) {
            System.out.println("Создается тестовый пользователь: voter_sidorov/pass из г. Москва");
            this.allUsers.add(new Voter("voter_sidorov", "pass", "Сидоров Сидор Сидорович",
                    LocalDate.of(1995, 1, 1), "12345678901", "Москва"));
        }
        if (this.allUsers.stream().noneMatch(u -> "voter_orlov".equals(u.getLogin()))) {
            System.out.println("Создается тестовый пользователь: voter_orlov/pass из г. Санкт-Петербург");
            this.allUsers.add(new Voter("voter_orlov", "pass", "Орлов Олег Олегович",
                    LocalDate.of(1970, 3, 10), "98765432109", "Санкт-Петербург"));
        }
        if (this.allUsers.stream().noneMatch(u -> "voter_erofeev".equals(u.getLogin()))) {
            System.out.println("Создается тестовый пользователь: voter_erofeev/pass из г. Москва");
            this.allUsers.add(new Voter("voter_erofeev", "pass", "Ерофеев Елисей Елисеевич",
                    LocalDate.of(2002, 11, 5), "55544433322", "Москва"));
        }


        // Создаем тестовое голосование, если его нет
        if (this.elections.stream().noneMatch(e -> "Выборы Президента 2025".equals(e.getName()))) {
            System.out.println("Создается тестовое голосование 'Выборы Президента 2025'");
            Election election = new Election("Выборы Президента 2025", LocalDateTime.now().plusDays(30));
            election.addCandidate(candidate1.getId());
            election.addCandidate(candidate2.getId());
            this.elections.add(election);
        }

        // Создаем завершенное голосование для проверки истории
        if (this.elections.stream().noneMatch(e -> "Выборы в Парламент 2023".equals(e.getName()))) {
            System.out.println("Создается тестовое завершенное голосование 'Выборы в Парламент 2023'");
            Election pastElection = new Election("Выборы в Парламент 2023", LocalDateTime.now().minusDays(10));
            pastElection.addCandidate(candidate1.getId());
            this.elections.add(pastElection);
        }
    }


    public List<User> getUsers() {
        return allUsers;
    }

    public List<Election> getElections() {
        return elections;
    }

    public void saveAllData() {
        List<Voter> votersToSave = allUsers.stream()
                .filter(u -> u.getRole() == Role.VOTER)
                .map(u -> (Voter) u)
                .collect(Collectors.toList());

        List<User> staffToSave = allUsers.stream()
                .filter(u -> u.getRole() != Role.VOTER)
                .collect(Collectors.toList());

        saveToFile(VOTERS_FILE, votersToSave);
        saveToFile(STAFF_FILE, staffToSave);
        saveToFile(ELECTIONS_FILE, elections);
        System.out.println("Все данные успешно сохранены.");
    }

    private void saveToFile(String fileName, Object data) {
        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла " + fileName + ": " + e.getMessage());
        }
    }

    private List<Voter> loadVoters() {
        File file = new File(VOTERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ArrayList<Voter>>() {}.getType();
            List<Voter> loadedData = gson.fromJson(reader, type);
            return loadedData != null ? loadedData : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла " + VOTERS_FILE + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Election> loadElections() {
        File file = new File(ELECTIONS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<ArrayList<Election>>() {}.getType();
            List<Election> loadedElections = gson.fromJson(reader, type);
            return loadedElections != null ? loadedElections : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла " + ELECTIONS_FILE + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}