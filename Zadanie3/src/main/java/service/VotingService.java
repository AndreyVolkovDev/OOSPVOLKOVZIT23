package com.evoting.service;

import com.evoting.model.*;
import com.evoting.storage.DataStorage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VotingService {
    private final DataStorage storage;

    public VotingService(DataStorage storage) {
        this.storage = storage;
    }

    // ... Все методы до generatePdfReport без изменений ...
    public User login(String login, String password) { return storage.getUsers().stream().filter(u -> u.getLogin().equals(login) && u.checkPassword(password)).findFirst().orElse(null); }
    public void saveData() { storage.saveAllData(); }
    public Optional<User> getUserById(UUID id) { return storage.getUsers().stream().filter(u -> u.getId().equals(id)).findFirst(); }
    public List<Election> getElections() { return storage.getElections(); }
    public List<User> getAllUsers() { return storage.getUsers(); }
    public boolean deleteUser(UUID userId) { return storage.getUsers().removeIf(u -> u.getId().equals(userId) && u.getRole() != Role.ADMINISTRATOR); }
    public List<CEC> getAllCECs() { return storage.getUsers().stream().filter(u -> u.getRole() == Role.CEC).map(u -> (CEC) u).collect(Collectors.toList()); }
    public boolean deleteCEC(UUID cecId) { return deleteUser(cecId); }
    public CEC createCEC(String login, String password) { if (storage.getUsers().stream().anyMatch(u -> u.getLogin().equals(login))) return null; CEC newCEC = new CEC(login, password); storage.getUsers().add(newCEC); return newCEC; }
    public List<Candidate> getAllCandidates() { return storage.getUsers().stream().filter(u -> u.getRole() == Role.CANDIDATE).map(u -> (Candidate) u).collect(Collectors.toList()); }
    public Election createElection(String name, LocalDateTime endDate) { Election election = new Election(name, endDate); storage.getElections().add(election); return election; }
    public Candidate addCandidate(String login, String password, String fullName) { if (storage.getUsers().stream().anyMatch(u -> u.getLogin().equals(login))) return null; Candidate candidate = new Candidate(login, password, fullName); storage.getUsers().add(candidate); return candidate; }
    public void addCandidateToElection(Election election, Candidate candidate) { if (election != null && candidate != null) election.addCandidate(candidate.getId()); }

    public void generatePdfReport(List<Election> electionsToExport, String directoryPath, String baseFileName, boolean singleFile, GroupingType groupingType) throws IOException {
        if (singleFile) {
            String finalFileName = (baseFileName == null || baseFileName.trim().isEmpty())
                    ? "report_" + System.currentTimeMillis() + ".pdf" : baseFileName + ".pdf";
            File file = new File(directoryPath, finalFileName);
            try (PDDocument doc = new PDDocument()) {
                for (Election election : electionsToExport) {
                    createPdfPageForElection(doc, election, groupingType);
                }
                doc.save(file);
                System.out.println("Общий отчет сохранен в: " + file.getAbsolutePath());
            }
        } else {
            for (Election election : electionsToExport) {
                String safeElectionName = election.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
                String finalFileName = (baseFileName == null || baseFileName.trim().isEmpty())
                        ? safeElectionName + "_" + System.currentTimeMillis() + ".pdf" : baseFileName + "_" + safeElectionName + ".pdf";
                File file = new File(directoryPath, finalFileName);
                try (PDDocument doc = new PDDocument()) {
                    createPdfPageForElection(doc, election, groupingType);
                    doc.save(file);
                    System.out.println("Отчет по голосованию '" + election.getName() + "' сохранен в: " + file.getAbsolutePath());
                }
            }
        }
    }

    public void generatePdfReport(List<Election> electionsToExport, String directoryPath, String baseFileName, boolean singleFile) throws IOException {
        generatePdfReport(electionsToExport, directoryPath, baseFileName, singleFile, GroupingType.NONE);
    }

    private void createPdfPageForElection(PDDocument doc, Election election, GroupingType groupingType) throws IOException {
        PDPage page = new PDPage();
        doc.addPage(page);

        InputStream fontStream = VotingService.class.getResourceAsStream("/fonts/DejaVuSans.ttf");
        if (fontStream == null) throw new IOException("Шрифт DejaVuSans.ttf не найден в ресурсах!");
        PDType0Font font = PDType0Font.load(doc, fontStream);

        PDPageContentStream contentStream = null;
        float yPosition = 750;

        try {
            contentStream = new PDPageContentStream(doc, page);

            contentStream.beginText();
            contentStream.setFont(font, 16);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Результаты голосования: " + election.getName());
            contentStream.endText();
            yPosition -= 40;

            if (groupingType == GroupingType.NONE) {
                Map<Candidate, Long> totalResults = getElectionResults(election, v -> true);
                drawResults(contentStream, font, "Общие результаты", totalResults, yPosition);
            } else {
                Function<Voter, String> classifier = groupingType == GroupingType.BY_CITY ? Voter::getCity : Voter::getAgeGroup;

                Set<String> groups = storage.getUsers().stream()
                        .filter(u -> u instanceof Voter)
                        .map(u -> classifier.apply((Voter) u))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                for (String group : groups) {
                    if (yPosition < 100) {
                        contentStream.close();
                        page = new PDPage();
                        doc.addPage(page);
                        contentStream = new PDPageContentStream(doc, page);
                        yPosition = 750;
                    }
                    Map<Candidate, Long> groupResults = getElectionResults(election, v -> group.equals(classifier.apply(v)));
                    yPosition = drawResults(contentStream, font, "Группа: " + group, groupResults, yPosition);
                }
            }
        } finally {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }

    private float drawResults(PDPageContentStream contentStream, PDType0Font font, String title, Map<Candidate, Long> results, float yStart) throws IOException {
        float y = yStart;
        contentStream.beginText();
        contentStream.setFont(font, 14);
        contentStream.newLineAtOffset(50, y);
        contentStream.showText(title);
        contentStream.endText();
        y -= 20;

        if (results.isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(70, y);
            contentStream.showText("Голоса отсутствуют.");
            contentStream.endText();
            y -= 15;
        } else {
            List<Map.Entry<Candidate, Long>> sortedResults = new ArrayList<>(results.entrySet());
            sortedResults.sort(Map.Entry.<Candidate, Long>comparingByValue().reversed());

            for(Map.Entry<Candidate, Long> entry : sortedResults){
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(70, y);
                String line = entry.getKey().getFullName() + " - Голосов: " + entry.getValue();
                contentStream.showText(line);
                contentStream.endText();
                y -= 15;
            }
        }
        return y - 20;
    }

    public Map<Candidate, Long> getElectionResults(Election election, Predicate<Voter> voterFilter) {
        Map<UUID, Long> votesCount = storage.getUsers().stream()
                .filter(u -> u instanceof Voter)
                .map(u -> (Voter) u)
                .filter(voterFilter)
                .map(v -> v.getVotes().get(election.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(candidateId -> candidateId, Collectors.counting()));
        Map<Candidate, Long> results = new HashMap<>();
        votesCount.forEach((candidateId, count) -> getUserById(candidateId).ifPresent(user -> {
            if (user instanceof Candidate) results.put((Candidate) user, count);
        }));
        return results;
    }

    public Voter registerVoter(String login, String password, String fullName, java.time.LocalDate dob, String snils, String city) {
        boolean loginExists = storage.getUsers().stream().anyMatch(u -> u.getLogin().equalsIgnoreCase(login));
        boolean snilsExists = storage.getUsers().stream().filter(u -> u instanceof Voter).anyMatch(u -> ((Voter) u).getSnils().equals(snils));
        if (loginExists || snilsExists) return null;
        Voter voter = new Voter(login, password, fullName, dob, snils, city);
        storage.getUsers().add(voter);
        return voter;
    }

    public List<Election> getActiveElections() { return storage.getElections().stream().filter(Election::isActive).collect(Collectors.toList()); }
    public void vote(Voter voter, Election election, Candidate candidate) { if (!election.isActive()) { System.out.println("Ошибка: Голосование уже завершено."); return; } if (!election.getCandidateIds().contains(candidate.getId())) { System.out.println("Ошибка: Данный кандидат не участвует в этом голосовании."); return; } if (voter.getVotes().containsKey(election.getId())) { System.out.println("Вы уже голосовали в этом голосовании."); return; } voter.addVote(election.getId(), candidate.getId()); System.out.println("Ваш голос за кандидата " + candidate.getFullName() + " в голосовании '" + election.getName() + "' принят!"); }
    public List<Candidate> getCandidatesForElection(Election election) { return storage.getUsers().stream().filter(u -> election.getCandidateIds().contains(u.getId())).map(u -> (Candidate) u).collect(Collectors.toList()); }
}