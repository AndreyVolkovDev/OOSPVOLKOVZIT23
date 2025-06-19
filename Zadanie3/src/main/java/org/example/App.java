package com.evoting;

import com.evoting.service.VotingService;
import com.evoting.storage.DataStorage;
import com.evoting.ui.ConsoleUI;

public class App {
    public static void main(String[] args) {
        DataStorage dataStorage = new DataStorage();
        VotingService votingService = new VotingService(dataStorage);
        ConsoleUI consoleUI = new ConsoleUI(votingService);

        consoleUI.start();
    }
}