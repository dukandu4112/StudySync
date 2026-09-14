package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpcomingWorkloadCliTest {

    private Path databasePath;
    private StudySyncService service;
    private PrintStream originalOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-workload-cli-", ".db");
        service = new StudySyncService(
                new DatabaseManager("jdbc:sqlite:" + databasePath));
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        Files.deleteIfExists(databasePath);
    }

    @Test
    void upcomingWorkloadOptionDisplaysOnlyAssignmentsInsideWindow() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        service.createAssignment(course.getId(), "Due Soon", "",
                LocalDateTime.now().plusDays(2), Assignment.Priority.HIGH);
        service.createAssignment(course.getId(), "Due Later", "",
                LocalDateTime.now().plusDays(20), Assignment.Priority.LOW);

        new StudySyncCli(service, new Scanner("16\n7\n0\n")).run();

        String text = output.toString();
        assertTrue(text.contains("Upcoming Workload - Next 7 Days"));
        assertTrue(text.contains("Due Soon"));
        assertFalse(text.contains("Due Later"));
    }

    @Test
    void upcomingWorkloadOptionRetriesInvalidWindow() {
        new StudySyncCli(service, new Scanner("16\n0\n7\n0\n")).run();

        String text = output.toString();
        assertTrue(text.contains("Please enter a positive whole number."));
        assertTrue(text.contains("Upcoming Workload - Next 7 Days"));
        assertTrue(text.contains("No assignments found."));
    }
}
