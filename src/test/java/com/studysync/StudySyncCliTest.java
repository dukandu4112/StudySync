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
    void searchAssignmentsOptionDisplaysMatchingAssignment() {
        Course course = createCourseWithAssignments();
        service.createAssignment(
                course.getId(),
                "Binary Search Review",
                "Practice search algorithms",
                LocalDateTime.now().plusDays(3),
                Assignment.Priority.HIGH);

        runCli("9\nbinary\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- Search Results ---"));
        assertTrue(text.contains("Binary Search Review"));
        assertFalse(text.contains("No assignments found."));
    }

    @Test
    void priorityFilterOptionOnlyDisplaysSelectedPriority() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        service.createAssignment(
                course.getId(),
                "Critical Project",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.HIGH);
        service.createAssignment(
                course.getId(),
                "Optional Reading",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.LOW);

        runCli("10\nHIGH\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- HIGH Priority Assignments ---"));
        assertTrue(text.contains("Critical Project"));
        assertFalse(text.contains("Optional Reading"));
    }

    @Test
    void pendingAssignmentsOptionExcludesCompletedWork() {
        Course course = createCourseWithAssignments();
        Assignment completed = service.createAssignment(
                course.getId(),
                "Finished Homework",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);
        service.completeAssignment(completed.getId());

        runCli("11\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- Pending Assignments ---"));
        assertTrue(text.contains("Pending Homework"));
        assertFalse(text.contains("Finished Homework"));
    }

    @Test
    void completedAssignmentsOptionOnlyDisplaysCompletedWork() {
        Course course = createCourseWithAssignments();
        Assignment completed = service.createAssignment(
                course.getId(),
                "Finished Homework",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);
        service.completeAssignment(completed.getId());

        runCli("12\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- Completed Assignments ---"));
        assertTrue(text.contains("Finished Homework"));
        assertFalse(text.contains("Pending Homework"));
    }

    @Test
    void overdueAssignmentsOptionOnlyDisplaysOverdueWork() {
        Course course = service.createCourse(
                "Linear Algebra",
                "MATH 2502");

        service.createAssignment(
                course.getId(),
                "Late Matrix Homework",
                "",
                LocalDateTime.now().minusDays(1),
                Assignment.Priority.HIGH);
        service.createAssignment(
                course.getId(),
                "Future Matrix Homework",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);

        runCli("13\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- Overdue Assignments ---"));
        assertTrue(text.contains("Late Matrix Homework"));
        assertFalse(text.contains("Future Matrix Homework"));
    }

    @Test
    void searchWithNoMatchesShowsEmptyMessage() {
        createCourseWithAssignments();

        runCli("9\nnonexistent\n0\n");

        String text = output.toString();
        assertTrue(text.contains("--- Search Results ---"));
        assertTrue(text.contains("No assignments found."));
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

    private Course createCourseWithAssignments() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        service.createAssignment(
                course.getId(),
                "Pending Homework",
                "Practice trees",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.HIGH);

        return course;
    }

    private void runCli(String input) {
        try (Scanner scanner = new Scanner(input)) {
            StudySyncCli cli = new StudySyncCli(service, scanner);
            cli.run();
        }
    }
}
