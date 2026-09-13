package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class StudySyncCliTest {

    private Path databasePath;
    private StudySyncService service;
    private PrintStream originalOut;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile(
                "studysync-cli-test-",
                ".db");

        DatabaseManager databaseManager =
                new DatabaseManager(
                        "jdbc:sqlite:" + databasePath);

        service = new StudySyncService(databaseManager);
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
    void nullServiceIsRejected() {
        Scanner scanner = new Scanner("0\n");

        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySyncCli(null, scanner));
    }

    @Test
    void nullScannerIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySyncCli(service, null));
    }

    @Test
    void exitOptionClosesApplication() {
        runCli("0\n");

        String text = output.toString();
        assertTrue(text.contains("StudySync"));
        assertTrue(text.contains("StudySync closed"));
    }

    @Test
    void invalidMenuOptionShowsValidationMessage() {
        runCli("99\n0\n");

        assertTrue(output.toString().contains(
                "Invalid option. Please try again."));
    }

    @Test
    void addCourseThroughCliPersistsCourse() {
        runCli("2\nData Structures\nCSCI 3300\n0\n");

        assertEquals(1, service.getCourses().size());
        assertEquals(
                "CSCI 3300",
                service.getCourses().get(0).getCode());
        assertTrue(output.toString().contains("Added course"));
    }

    @Test
    void dashboardOptionDisplaysMetrics() {
        service.createCourse("Data Structures", "CSCI 3300");

        runCli("1\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- Dashboard ---"));
        assertTrue(text.contains("Courses: 1"));
        assertTrue(text.contains("Completion: 0.0%"));
    }

    @Test
    void positiveNumberInputRetriesAfterInvalidValues() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        runCli(
                "7\n"
                        + "abc\n"
                        + "0\n"
                        + course.getId() + "\n"
                        + "2026-09-13T10:00\n"
                        + "60\n"
                        + "Graphs\n"
                        + "0\n");

        assertEquals(1, service.getStudySessions().size());
        assertTrue(output.toString().contains(
                "Please enter a positive whole number."));
    }

    @Test
    void dateTimeInputRetriesAfterInvalidValue() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        runCli(
                "7\n"
                        + course.getId() + "\n"
                        + "not-a-date\n"
                        + "2026-09-13T10:00\n"
                        + "45\n"
                        + "Trees\n"
                        + "0\n");

        assertEquals(1, service.getStudySessions().size());
        assertTrue(output.toString().contains(
                "Invalid date/time."));
    }

    @Test
    void priorityInputRetriesAfterInvalidValue() {
        Course course = service.createCourse(
                "Linear Algebra",
                "MATH 2502");

        runCli(
                "4\n"
                        + course.getId() + "\n"
                        + "Homework\n"
                        + "Matrix practice\n"
                        + "2026-09-23T09:00\n"
                        + "urgent\n"
                        + "HIGH\n"
                        + "0\n");

        assertEquals(1, service.getAssignments().size());
        assertEquals(
                Assignment.Priority.HIGH,
                service.getAssignments().get(0).getPriority());
        assertTrue(output.toString().contains(
                "Please enter LOW, MEDIUM, or HIGH."));
    }

    @Test
    void applicationErrorReturnsUserToMenu() {
        runCli(
                "4\n"
                        + "999\n"
                        + "Homework\n"
                        + "Missing course\n"
                        + "2026-09-23T09:00\n"
                        + "HIGH\n"
                        + "0\n");

        String text = output.toString();
        assertTrue(text.contains("Error: Course does not exist: 999"));
        assertTrue(text.contains("StudySync closed"));
    }

    private void runCli(String input) {
        try (Scanner scanner = new Scanner(input)) {
            StudySyncCli cli = new StudySyncCli(service, scanner);
            cli.run();
        }
    }
}
