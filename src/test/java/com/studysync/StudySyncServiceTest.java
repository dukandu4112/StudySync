package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudySyncServiceTest {

    private Path databasePath;
    private StudySyncService service;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile(
                "studysync-service-test-",
                ".db");

        DatabaseManager databaseManager =
                new DatabaseManager(
                        "jdbc:sqlite:" + databasePath);

        service = new StudySyncService(databaseManager);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void nullDatabaseManagerIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySyncService(null));
    }

    @Test
    void createCourseStoresCourse() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        assertTrue(course.getId() > 0);
        assertEquals(1, service.getCourses().size());
    }

    @Test
    void assignmentRequiresExistingCourse() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createAssignment(
                        999,
                        "Homework",
                        "",
                        LocalDateTime.now().plusDays(1),
                        Assignment.Priority.HIGH));
    }

    @Test
    void pendingAssignmentsExcludeCompletedAssignments() {
        Course course = service.createCourse(
                "Algorithms",
                "CSCI 3320");

        Assignment completed = service.createAssignment(
                course.getId(),
                "Completed Work",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.LOW);

        service.createAssignment(
                course.getId(),
                "Pending Work",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.HIGH);

        service.completeAssignment(completed.getId());

        List<Assignment> pending =
                service.getPendingAssignments();

        assertEquals(1, pending.size());
        assertEquals(
                "Pending Work",
                pending.get(0).getTitle());
    }

    @Test
    void overdueAssignmentsOnlyReturnOverduePendingWork() {
        Course course = service.createCourse(
                "Linear Algebra",
                "MATH 2502");

        service.createAssignment(
                course.getId(),
                "Overdue Homework",
                "",
                LocalDateTime.now().minusDays(2),
                Assignment.Priority.HIGH);

        service.createAssignment(
                course.getId(),
                "Future Homework",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);

        List<Assignment> overdue =
                service.getOverdueAssignments();

        assertEquals(1, overdue.size());
        assertEquals(
                "Overdue Homework",
                overdue.get(0).getTitle());
    }

    @Test
    void completeAndReopenAssignmentUpdatesStatus() {
        Course course = service.createCourse(
                "Computer Architecture",
                "CSCI 3212");

        Assignment assignment = service.createAssignment(
                course.getId(),
                "Quiz Review",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.MEDIUM);

        assertTrue(
                service.completeAssignment(
                        assignment.getId()));
        assertTrue(
                service.getAssignments()
                        .get(0)
                        .isCompleted());

        assertTrue(
                service.reopenAssignment(
                        assignment.getId()));
        assertFalse(
                service.getAssignments()
                        .get(0)
                        .isCompleted());
    }

    @Test
    void recordStudySessionRequiresExistingCourse() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.recordStudySession(
                        999,
                        LocalDateTime.now(),
                        60,
                        "Review"));
    }

    @Test
    void totalStudyMinutesAreCalculated() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        service.recordStudySession(
                course.getId(),
                LocalDateTime.now(),
                60,
                "Trees");

        service.recordStudySession(
                course.getId(),
                LocalDateTime.now().plusHours(2),
                45,
                "Graphs");

        assertEquals(105, service.getTotalStudyMinutes());
        assertEquals(
                105,
                service.getTotalStudyMinutesForCourse(
                        course.getId()));
    }

    @Test
    void completionPercentageIsZeroWithoutAssignments() {
        assertEquals(
                0.0,
                service.getAssignmentCompletionPercentage(),
                0.001);
    }

    @Test
    void completionPercentageReflectsCompletedAssignments() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        Assignment first = service.createAssignment(
                course.getId(),
                "Homework 1",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.HIGH);

        service.createAssignment(
                course.getId(),
                "Homework 2",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);

        service.completeAssignment(first.getId());

        assertEquals(
                50.0,
                service.getAssignmentCompletionPercentage(),
                0.001);
    }

    @Test
    void completingMissingAssignmentIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.completeAssignment(999));
    }
}
