package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentPlanTest {
    private Path databasePath;
    private StudySyncService service;
    private Course course;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-assignment-plan-", ".db");
        service = new StudySyncService(new DatabaseManager("jdbc:sqlite:" + databasePath));
        course = service.createCourse("Computer Science", "CSCI 3300");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void planClassifiesUrgencyAndExcludesCompletedAssignments() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 15, 10, 0);
        Assignment overdue = service.createAssignment(course.getId(), "Overdue", "", now.minusHours(2), Assignment.Priority.LOW);
        Assignment today = service.createAssignment(course.getId(), "Today", "", now.plusHours(4), Assignment.Priority.MEDIUM);
        Assignment soon = service.createAssignment(course.getId(), "Soon", "", now.plusHours(30), Assignment.Priority.HIGH);
        Assignment upcoming = service.createAssignment(course.getId(), "Upcoming", "", now.plusDays(5), Assignment.Priority.HIGH);
        Assignment completed = service.createAssignment(course.getId(), "Done", "", now.minusDays(1), Assignment.Priority.HIGH);
        service.completeAssignment(completed.getId());

        List<AssignmentPlanItem> plan = service.getAssignmentPlan(now);

        assertEquals(List.of(overdue.getId(), today.getId(), soon.getId(), upcoming.getId()),
                plan.stream().map(item -> item.assignment().getId()).toList());
        assertEquals(AssignmentPlanItem.Urgency.OVERDUE, plan.get(0).urgency());
        assertEquals(AssignmentPlanItem.Urgency.DUE_TODAY, plan.get(1).urgency());
        assertEquals(AssignmentPlanItem.Urgency.DUE_SOON, plan.get(2).urgency());
        assertEquals(AssignmentPlanItem.Urgency.UPCOMING, plan.get(3).urgency());
        assertEquals(-120, plan.get(0).minutesUntilDue());
        assertEquals(240, plan.get(1).minutesUntilDue());
    }

    @Test
    void planUsesPriorityThenDeadlineInsideSameUrgencyGroup() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 15, 8, 0);
        Assignment lowEarlier = service.createAssignment(course.getId(), "Low earlier", "", now.plusDays(4), Assignment.Priority.LOW);
        Assignment highLater = service.createAssignment(course.getId(), "High later", "", now.plusDays(6), Assignment.Priority.HIGH);
        Assignment highEarlier = service.createAssignment(course.getId(), "High earlier", "", now.plusDays(5), Assignment.Priority.HIGH);
        Assignment medium = service.createAssignment(course.getId(), "Medium", "", now.plusDays(3), Assignment.Priority.MEDIUM);

        List<AssignmentPlanItem> plan = service.getAssignmentPlan(now);

        assertEquals(List.of(highEarlier.getId(), highLater.getId(), medium.getId(), lowEarlier.getId()),
                plan.stream().map(item -> item.assignment().getId()).toList());
    }

    @Test
    void planRejectsNullReferenceTime() {
        assertThrows(IllegalArgumentException.class, () -> service.getAssignmentPlan(null));
    }

    @Test
    void planItemRejectsInvalidState() {
        LocalDateTime due = LocalDateTime.of(2026, 9, 20, 12, 0);
        Assignment assignment = new Assignment(1, "Pending", "", due, Assignment.Priority.MEDIUM);
        assertThrows(IllegalArgumentException.class,
                () -> new AssignmentPlanItem(null, AssignmentPlanItem.Urgency.UPCOMING, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new AssignmentPlanItem(assignment, null, 10));
        assignment.markCompleted();
        assertThrows(IllegalArgumentException.class,
                () -> new AssignmentPlanItem(assignment, AssignmentPlanItem.Urgency.UPCOMING, 10));
    }
}
