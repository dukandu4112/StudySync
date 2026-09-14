package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpcomingWorkloadTest {

    private Path databasePath;
    private StudySyncService service;
    private Course course;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-workload-test-", ".db");
        service = new StudySyncService(
                new DatabaseManager("jdbc:sqlite:" + databasePath));
        course = service.createCourse("Data Structures", "CSCI 3300");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void upcomingAssignmentsOnlyIncludePendingWorkInsideWindow() {
        Assignment completed = service.createAssignment(
                course.getId(), "Completed Soon", "",
                LocalDateTime.now().plusDays(1), Assignment.Priority.LOW);
        service.completeAssignment(completed.getId());
        service.createAssignment(
                course.getId(), "Overdue Work", "",
                LocalDateTime.now().minusDays(1), Assignment.Priority.HIGH);
        service.createAssignment(
                course.getId(), "Due Soon", "",
                LocalDateTime.now().plusDays(2), Assignment.Priority.HIGH);
        service.createAssignment(
                course.getId(), "Due Later", "",
                LocalDateTime.now().plusDays(10), Assignment.Priority.MEDIUM);

        List<Assignment> upcoming = service.getUpcomingAssignments(7);

        assertEquals(1, upcoming.size());
        assertEquals("Due Soon", upcoming.get(0).getTitle());
    }

    @Test
    void upcomingAssignmentsAreOrderedByDueDate() {
        service.createAssignment(
                course.getId(), "Later", "",
                LocalDateTime.now().plusDays(5), Assignment.Priority.LOW);
        service.createAssignment(
                course.getId(), "Sooner", "",
                LocalDateTime.now().plusDays(1), Assignment.Priority.HIGH);

        List<Assignment> upcoming = service.getUpcomingAssignments(7);

        assertEquals("Sooner", upcoming.get(0).getTitle());
        assertEquals("Later", upcoming.get(1).getTitle());
    }

    @Test
    void invalidUpcomingWindowsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getUpcomingAssignments(0));
        assertThrows(IllegalArgumentException.class,
                () -> service.getUpcomingAssignments(-1));
    }
}
