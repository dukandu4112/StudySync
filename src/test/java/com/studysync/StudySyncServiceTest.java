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
        assertEquals("Pending Work", pending.get(0).getTitle());
    }

    @Test
    void completedAssignmentsExcludePendingAssignments() {
        Course course = service.createCourse(
                "Algorithms",
                "CSCI 3320");

        Assignment completed = service.createAssignment(
                course.getId(),
                "Finished Project",
                "Implementation complete",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.HIGH);

        service.createAssignment(
                course.getId(),
                "Pending Project",
                "Still working",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.MEDIUM);

        service.completeAssignment(completed.getId());

        List<Assignment> assignments =
                service.getCompletedAssignments();

        assertEquals(1, assignments.size());
        assertEquals("Finished Project", assignments.get(0).getTitle());
        assertTrue(assignments.get(0).isCompleted());
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
        assertEquals("Overdue Homework", overdue.get(0).getTitle());
    }

    @Test
    void assignmentsCanBeFilteredByPriority() {
        Course course = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        service.createAssignment(
                course.getId(),
                "Low Priority",
                "",
                LocalDateTime.now().plusDays(3),
                Assignment.Priority.LOW);

        service.createAssignment(
                course.getId(),
                "High Priority Later",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.HIGH);

        service.createAssignment(
                course.getId(),
                "High Priority First",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.HIGH);

        List<Assignment> highPriority =
                service.getAssignmentsByPriority(
                        Assignment.Priority.HIGH);

        assertEquals(2, highPriority.size());
        assertEquals("High Priority First", highPriority.get(0).getTitle());
        assertEquals("High Priority Later", highPriority.get(1).getTitle());
    }

    @Test
    void nullPriorityFilterIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getAssignmentsByPriority(null));
    }

    @Test
    void assignmentSearchMatchesTitleCaseInsensitively() {
        Course course = service.createCourse(
                "Computer Architecture",
                "CSCI 3212");

        service.createAssignment(
                course.getId(),
                "Pipeline Review",
                "Study processor stages",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.HIGH);

        service.createAssignment(
                course.getId(),
                "Memory Homework",
                "Cache hierarchy",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);

        List<Assignment> results =
                service.searchAssignments("PIPELINE");

        assertEquals(1, results.size());
        assertEquals("Pipeline Review", results.get(0).getTitle());
    }

    @Test
    void assignmentSearchMatchesDescriptionCaseInsensitively() {
        Course course = service.createCourse(
                "Linear Algebra",
                "MATH 2502");

        service.createAssignment(
                course.getId(),
                "Homework 1",
                "Practice MATRIX operations",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.MEDIUM);

        service.createAssignment(
                course.getId(),
                "Homework 2",
                "Vector practice",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.LOW);

        List<Assignment> results =
                service.searchAssignments("matrix");

        assertEquals(1, results.size());
        assertEquals("Homework 1", results.get(0).getTitle());
    }

    @Test
    void blankAssignmentSearchIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.searchAssignments("   "));
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

        assertTrue(service.completeAssignment(assignment.getId()));
        assertTrue(service.getAssignments().get(0).isCompleted());

        assertTrue(service.reopenAssignment(assignment.getId()));
        assertFalse(service.getAssignments().get(0).isCompleted());
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
                service.getTotalStudyMinutesForCourse(course.getId()));
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

    @Test
    void dashboardSummaryIsEmptyForNewDatabase() {
        DashboardSummary summary = service.getDashboardSummary();

        assertEquals(0, summary.totalCourses());
        assertEquals(0, summary.totalAssignments());
        assertEquals(0, summary.pendingAssignments());
        assertEquals(0, summary.completedAssignments());
        assertEquals(0, summary.overdueAssignments());
        assertEquals(0, summary.totalStudyMinutes());
        assertEquals(0.0, summary.completionPercentage(), 0.001);
    }

    @Test
    void dashboardSummaryReflectsAcademicProgress() {
        Course firstCourse = service.createCourse(
                "Data Structures",
                "CSCI 3300");

        service.createCourse(
                "Linear Algebra",
                "MATH 2502");

        Assignment completed = service.createAssignment(
                firstCourse.getId(),
                "Completed Homework",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.MEDIUM);

        service.createAssignment(
                firstCourse.getId(),
                "Overdue Homework",
                "",
                LocalDateTime.now().minusDays(1),
                Assignment.Priority.HIGH);

        service.completeAssignment(completed.getId());

        service.recordStudySession(
                firstCourse.getId(),
                LocalDateTime.now(),
                60,
                "Trees");

        service.recordStudySession(
                firstCourse.getId(),
                LocalDateTime.now().plusHours(2),
                30,
                "Graphs");

        DashboardSummary summary = service.getDashboardSummary();

        assertEquals(2, summary.totalCourses());
        assertEquals(2, summary.totalAssignments());
        assertEquals(1, summary.pendingAssignments());
        assertEquals(1, summary.completedAssignments());
        assertEquals(1, summary.overdueAssignments());
        assertEquals(90, summary.totalStudyMinutes());
        assertEquals(50.0, summary.completionPercentage(), 0.001);
    }
}
