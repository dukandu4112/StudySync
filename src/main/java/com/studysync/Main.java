package com.studysync;

import java.util.Scanner;

/**
 * Entry point for the StudySync application.
 */
public class Main {

    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        StudySyncService service = new StudySyncService(databaseManager);

        try (Scanner scanner = new Scanner(System.in)) {
            StudySyncCli cli = new StudySyncCli(service, scanner);
            cli.run();
        }
    }
}
