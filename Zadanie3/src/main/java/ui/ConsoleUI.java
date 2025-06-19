package com.evoting.ui;

import com.evoting.model.*;
import com.evoting.service.GroupingType;
import com.evoting.service.VotingService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ConsoleUI {
    private final VotingService service;
    private final Scanner scanner;
    private User currentUser = null;

    public ConsoleUI(VotingService service) {
        this.service = service;
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    public void start() {
        System.out.println("Добро пожаловать в систему электронного голосования!");
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showUserMenu();
            }
        }
    }

    // --- Главное меню и Авторизация ---

    private void showLoginMenu() {
        System.out.println("\n--- Главное меню ---");
        System.out.println("1. Войти");
        System.out.println("2. Зарегистрироваться (как пользователь)");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1": handleLogin(); break;
            case "2": handleRegister(); break;
            case "0":
                System.out.println("Сохранение данных...");
                service.saveData();
                System.out.println("Выход.");
                System.exit(0);
                break;
            default: System.out.println("Неверный ввод.");
        }
    }

    private void handleLogin() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();
        currentUser = service.login(login, password);
        if (currentUser != null) {
            System.out.println("Вход выполнен успешно! Добро пожаловать, " + currentUser.getFullName());
        } else {
            System.out.println("Неверный логин или пароль.");
        }
    }

    private void handleRegister() {
        System.out.println("\n--- Регистрация нового пользователя ---");
        System.out.print("Введите ФИО: ");
        String fullName = scanner.nextLine();
        System.out.print("Введите дату рождения (ГГГГ-ММ-ДД): ");
        String dobString = scanner.nextLine();
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobString);
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты. Регистрация отменена.");
            return;
        }
        System.out.print("Введите СНИЛС (11 цифр без тире и пробелов): ");
        String snils = scanner.nextLine();
        System.out.print("Введите ваш город: ");
        String city = scanner.nextLine();
        System.out.print("Придумайте логин: ");
        String login = scanner.nextLine();
        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine();

        Voter newVoter = service.registerVoter(login, password, fullName, dob, snils, city);
        if (newVoter != null) {
            System.out.println("Регистрация прошла успешно! Теперь вы можете войти, используя свой логин и пароль.");
        } else {
            System.out.println("Ошибка: пользователь с таким логином или СНИЛС уже существует.");
        }
    }

    private void showUserMenu() {
        switch (currentUser.getRole()) {
            case ADMINISTRATOR: showAdminMenu(); break;
            case CEC: showCECMenu(); break;
            case CANDIDATE: showCandidateMenu(); break;
            case VOTER: showVoterMenu(); break;
        }
    }

    private String getRoleDisplayName(Role role) {
        switch (role) {
            case VOTER: return "Пользователи";
            case CEC: return "ЦИК";
            case CANDIDATE: return "Кандидаты";
            case ADMINISTRATOR: return "Администраторы";
            default: return role.name();
        }
    }

    // --- Меню Администратора ---

    private void showAdminMenu() {
        while (currentUser != null) {
            System.out.println("\n--- Меню Администратора ---");
            System.out.println("1. Просмотр и удаление Пользователей");
            System.out.println("2. Просмотр и удаление ЦИК");
            System.out.println("3. Создать ЦИК");
            System.out.println("4. Просмотр и удаление кандидатов");
            System.out.println("0. Выйти из аккаунта");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": manageUsers(Role.VOTER); break;
                case "2": manageUsers(Role.CEC); break;
                case "3": createCEC(); break;
                case "4": manageUsers(Role.CANDIDATE); break;
                case "0": currentUser = null; return;
                default: System.out.println("Неверный ввод.");
            }
        }
    }

    // ИСПРАВЛЕННЫЙ МЕТОД
    private void manageUsers(Role role) {
        List<User> users = service.getAllUsers().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());

        System.out.println("\n--- Список: " + getRoleDisplayName(role) + " ---");
        printList(users);

        if (!users.isEmpty()) {
            System.out.print("\nВведите номер для удаления (или 0 для отмены): ");
            int choice = readInt();
            if (choice > 0 && choice <= users.size()) {
                User userToDelete = users.get(choice - 1);
                if (service.deleteUser(userToDelete.getId())) {
                    System.out.println("Пользователь " + userToDelete.getLogin() + " успешно удален.");
                } else {
                    System.out.println("Не удалось удалить пользователя. Возможно, у вас нет прав на это действие.");
                }
            }
        }
    }

    private void createCEC() {
        System.out.println("\n--- Создание нового ЦИК ---");
        System.out.print("Введите логин для нового ЦИК: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();
        CEC newCEC = service.createCEC(login, password);
        if (newCEC != null) {
            System.out.println("ЦИК успешно создан: " + newCEC);
        } else {
            System.out.println("Ошибка: пользователь с таким логином уже существует.");
        }
    }

    // --- Меню ЦИК ---

    private void showCECMenu() {
        while (currentUser != null) {
            System.out.println("\n--- Меню ЦИК ---");
            System.out.println("1. Создать голосование");
            System.out.println("2. Создать профиль кандидата");
            System.out.println("3. Добавить кандидата в голосование");
            System.out.println("4. Печать результатов в PDF");
            System.out.println("0. Выйти из аккаунта");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": createElection(); break;
                case "2": createCandidateByCEC(); break;
                case "3": addCandidateToElection(); break;
                case "4": handlePdfExport(); break;
                case "0": currentUser = null; return;
                default: System.out.println("Неверный ввод.");
            }
        }
    }

    private void createElection() {
        System.out.println("\n--- Создание нового голосования ---");
        System.out.print("Введите название голосования: ");
        String name = scanner.nextLine();
        System.out.print("Введите дату и время окончания (ГГГГ-ММ-ДДTЧЧ:ММ): ");
        String endDateStr = scanner.nextLine();
        try {
            LocalDateTime endDate = LocalDateTime.parse(endDateStr);
            Election election = service.createElection(name, endDate);
            System.out.println("Голосование '" + election.getName() + "' успешно создано.");
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты. Используйте формат ГГГГ-ММ-ДДTЧЧ:ММ (например, 2024-12-31T23:59).");
        }
    }

    private void createCandidateByCEC() {
        System.out.println("\n--- Создание профиля кандидата ---");
        System.out.print("Введите ФИО кандидата: ");
        String fullName = scanner.nextLine();
        System.out.print("Введите логин для кандидата: ");
        String login = scanner.nextLine();
        System.out.print("Введите пароль для кандидата: ");
        String password = scanner.nextLine();
        Candidate candidate = service.addCandidate(login, password, fullName);
        if (candidate != null) {
            System.out.println("Кандидат " + candidate.getFullName() + " успешно создан.");
        } else {
            System.out.println("Ошибка: кандидат с таким логином уже существует.");
        }
    }

    private void addCandidateToElection() {
        System.out.println("\n--- Добавление кандидата в голосование ---");
        Election election = selectElection(service.getElections());
        if (election == null) return;

        Candidate candidate = selectUser(service.getAllCandidates(), "кандидата");
        if (candidate == null) return;

        service.addCandidateToElection(election, candidate);
        System.out.println("Кандидат " + candidate.getFullName() + " добавлен в голосование '" + election.getName() + "'.");
    }

    private void handlePdfExport() {
        List<Election> elections = service.getElections();
        if (elections.isEmpty()) {
            System.out.println("Нет доступных голосований для экспорта.");
            return;
        }

        System.out.println("Выберите голосования для экспорта (введите номера через запятую, например, 1,3, или all для всех):");
        printList(elections);
        String input = scanner.nextLine();

        List<Election> electionsToExport = new ArrayList<>();
        if ("all".equalsIgnoreCase(input)) {
            electionsToExport.addAll(elections);
        } else {
            try {
                String[] choices = input.split(",");
                for (String choice : choices) {
                    int index = Integer.parseInt(choice.trim()) - 1;
                    if (index >= 0 && index < elections.size()) {
                        electionsToExport.add(elections.get(index));
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод.");
                return;
            }
        }

        if (electionsToExport.isEmpty()) {
            System.out.println("Не выбрано ни одного голосования.");
            return;
        }

        boolean singleFile = false;
        if (electionsToExport.size() > 1) {
            System.out.print("Выгрузить все в один файл? (yes/no): ");
            singleFile = scanner.nextLine().equalsIgnoreCase("yes");
        }

        System.out.print("Введите путь к папке для сохранения (оставьте пустым для текущей папки): ");
        String dirPath = scanner.nextLine();
        if (dirPath == null || dirPath.trim().isEmpty()) {
            dirPath = ".";
        }
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Ошибка: указанный путь не существует или не является папкой.");
            return;
        }

        System.out.print("Введите имя файла (без расширения, оставьте пустым для авто-генерации): ");
        String fileName = scanner.nextLine();

        System.out.println("\nВыберите тип группировки результатов:");
        System.out.println("1. " + GroupingType.NONE.getDescription());
        System.out.println("2. " + GroupingType.BY_CITY.getDescription());
        System.out.println("3. " + GroupingType.BY_AGE_GROUP.getDescription());
        System.out.print("Введите номер (по умолчанию 1): ");

        String groupingChoice = scanner.nextLine();
        GroupingType groupingType = GroupingType.NONE;
        switch (groupingChoice) {
            case "2": groupingType = GroupingType.BY_CITY; break;
            case "3": groupingType = GroupingType.BY_AGE_GROUP; break;
        }

        try {
            service.generatePdfReport(electionsToExport, dirPath, fileName, singleFile, groupingType);
        } catch (IOException e) {
            System.out.println("Ошибка при создании PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // --- Меню Кандидата ---

    private void showCandidateMenu() {
        Candidate self = (Candidate) currentUser;
        while (currentUser != null) {
            System.out.println("\n--- Меню Кандидата ---");
            System.out.println("1. Заполнить/Изменить данные о себе");
            System.out.println("2. Результаты последнего голосования с моим участием");
            System.out.println("3. Все голосования, в которых я принимал участие");
            System.out.println("0. Выйти из аккаунта");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": editCandidateProfile(self); break;
                case "2": showLastElectionResultForCandidate(self); break;
                case "3": showAllElectionsForCandidate(self); break;
                case "0": currentUser = null; return;
                default: System.out.println("Неверный ввод.");
            }
        }
    }

    private void editCandidateProfile(Candidate self) {
        System.out.println("\n--- Редактирование профиля ---");
        System.out.println("Текущая биография: " + (self.getBiography() != null ? self.getBiography() : "не заполнено"));
        System.out.print("Введите новую биографию (или оставьте пустым, чтобы не менять): ");
        String bio = scanner.nextLine();
        if(!bio.trim().isEmpty()) {
            self.setBiography(bio);
        }

        System.out.println("Текущая дата рождения: " + (self.getDateOfBirth() != null ? self.getDateOfBirth() : "не заполнено"));
        System.out.print("Введите новую дату рождения (ГГГГ-ММ-ДД, или оставьте пустым): ");
        String dobStr = scanner.nextLine();
        if(!dobStr.trim().isEmpty()) {
            try {
                self.setDateOfBirth(LocalDate.parse(dobStr));
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Дата рождения не изменена.");
            }
        }
        System.out.println("Профиль обновлен.");
    }

    private void showLastElectionResultForCandidate(Candidate candidate) {
        Election lastElection = service.getElections().stream()
                .filter(e -> e.getCandidateIds().contains(candidate.getId()) && !e.isActive())
                .max(Comparator.comparing(Election::getEndDate))
                .orElse(null);

        if (lastElection == null) {
            System.out.println("Нет завершенных голосований с вашим участием.");
            return;
        }

        System.out.println("\n--- Результаты голосования: " + lastElection.getName() + " ---");
        Map<Candidate, Long> results = service.getElectionResults(lastElection, v -> true);
        printResults(results);
    }

    private void showAllElectionsForCandidate(Candidate candidate) {
        List<Election> participatedElections = service.getElections().stream()
                .filter(e -> e.getCandidateIds().contains(candidate.getId()))
                .collect(Collectors.toList());

        System.out.println("\n--- Голосования с вашим участием ---");
        printList(participatedElections);
    }

    // --- Меню Пользователя (Voter) ---

    private void showVoterMenu() {
        Voter self = (Voter) currentUser;
        while (currentUser != null) {
            System.out.println("\n--- Меню Пользователя ---");
            System.out.println("1. Проголосовать");
            System.out.println("2. Просмотреть список всех кандидатов");
            System.out.println("3. Моя история голосований");
            System.out.println("0. Выйти из аккаунта");
            System.out.print("Выберите действие: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": handleVote(self); break;
                case "2": showAllCandidates(); break;
                case "3": showVotingHistory(self); break;
                case "0": currentUser = null; return;
                default: System.out.println("Неверный ввод.");
            }
        }
    }

    private void handleVote(Voter voter) {
        System.out.println("\n--- Доступные для голосования выборы ---");
        List<Election> activeElections = service.getActiveElections().stream()
                .filter(e -> !voter.getVotes().containsKey(e.getId()))
                .collect(Collectors.toList());

        Election election = selectElection(activeElections);
        if (election == null) return;

        System.out.println("\n--- Кандидаты в голосовании: " + election.getName() + " ---");
        List<Candidate> candidates = service.getCandidatesForElection(election);
        Candidate candidate = selectUser(candidates, "кандидата");
        if(candidate == null) return;

        service.vote(voter, election, candidate);
    }

    private void showAllCandidates() {
        System.out.println("\n--- Список всех кандидатов в системе ---");
        List<Candidate> candidates = service.getAllCandidates();
        printList(candidates);
    }

    private void showVotingHistory(Voter voter) {
        System.out.println("\n--- Ваша история голосований ---");
        List<Election> allElections = service.getElections();
        if (allElections.isEmpty()) {
            System.out.println("В системе еще не было голосований.");
            return;
        }

        allElections.forEach(election -> {
            UUID votedForCandidateId = voter.getVotes().get(election.getId());
            if (votedForCandidateId != null) {
                service.getUserById(votedForCandidateId).ifPresent(user -> {
                    String candidateName = user.getFullName();
                    System.out.println("- " + election.getName() + ": Проголосовал(а) за " + candidateName);
                });
            } else {
                System.out.println("- " + election.getName() + ": Не голосовал(а)");
            }
        });
    }

    // --- Вспомогательные методы ---

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private <T> void printList(List<T> list) {
        if(list == null || list.isEmpty()) {
            System.out.println("Список пуст.");
            return;
        }
        AtomicInteger counter = new AtomicInteger(1);
        list.forEach(item -> System.out.println(counter.getAndIncrement() + ". " + item.toString()));
    }

    private <T extends User> T selectUser(List<T> users, String userType) {
        printList(users);
        if (users == null || users.isEmpty()) {
            System.out.println("Нет доступных " + userType + " для выбора.");
            return null;
        }
        System.out.print("\nВыберите " + userType + " (введите номер или 0 для отмены): ");
        int choice = readInt();
        if (choice > 0 && choice <= users.size()) {
            return users.get(choice - 1);
        }
        return null;
    }

    private Election selectElection(List<Election> elections) {
        printList(elections);
        if (elections == null || elections.isEmpty()) {
            System.out.println("Нет доступных голосований для выбора.");
            return null;
        }
        System.out.print("\nВыберите голосование (введите номер или 0 для отмены): ");
        int choice = readInt();
        if (choice > 0 && choice <= elections.size()) {
            return elections.get(choice - 1);
        }
        return null;
    }

    private void printResults(Map<Candidate, Long> results) {
        if (results.isEmpty()) {
            System.out.println("Нет голосов.");
            return;
        }
        results.entrySet().stream()
                .sorted(Map.Entry.<Candidate, Long>comparingByValue().reversed())
                .forEach(entry -> System.out.println("Кандидат: " + entry.getKey().getFullName() + " - Голосов: " + entry.getValue()));
    }
}