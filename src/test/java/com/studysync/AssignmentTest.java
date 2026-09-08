package com.studysync;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {

    @Test
    void constructorCreatesValidAssignment() {
        LocalDateTime dueDate =
                LocalDateTime.of(2026, 9, 15, 23, 59);

        Assignment assignment = new Assignment(
                1,
                "Complete Homework",
                "Finish Chapter 2 problems",
                dueDate,
                Assignment.Priority.HIGH);

        assertEquals(1, assignment.getCourseId());
        assertEquals(
                "Complete Homework",
                assignment.getTitle());
        assertEquals(
                "Finish Chapter 2 problems",
                assignment.getDescription());
        assertEquals(dueDate, assignment.getDueDate());
        assertEquals(
                Assignment.Priority.HIGH,
                assignment.getPriority());
        assertFalse(assignment.isCompleted());
    }

    @Test
    void persistedAssignmentStoresIdAndCompletionStatus() {
        LocalDateTime dueDate =
                LocalDateTime.of(2026, 9, 20, 12, 0);

        Assignment assignment = new Assignment(
                5,
                2,
                "Study for Test",
                "Review lecture notes",
                dueDate,
                Assignment.Priority.MEDIUM,
                true);

        assertEquals(5, assignment.getId());
        assertEquals(2, assignment.getCourseId());
        assertTrue(assignment.isCompleted());
    }

    @Test
    void titleIsTrimmed() {
        Assignment assignment = createAssignment(
                "  Complete Project  ");

        assertEquals(
                "Complete Project",
                assignment.getTitle());
    }

    @Test
    void descriptionIsTrimmed() {
        Assignment assignment = new Assignment(
                1,
                "Homework",
                "  Complete problems 1-10  ",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);

        assertEquals(
                "Complete problems 1-10",
                assignment.getDescription());
    }

    @Test
    void nullDescriptionBecomesEmptyString() {
        Assignment assignment = new Assignment(
                1,
                "Homework",
                null,
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.MEDIUM);

        assertEquals("", assignment.getDescription());
    }

    @Test
    void assignmentCanBeMarkedCompleted() {
        Assignment assignment =
                createAssignment("Homework");

        assertFalse(assignment.isCompleted());

        assignment.markCompleted();

        assertTrue(assignment.isCompleted());
    }

    @Test
    void assignmentCanBeMarkedIncomplete() {
        Assignment assignment =
                createAssignment("Homework");

        assignment.markCompleted();
        assignment.markIncomplete();

        assertFalse(assignment.isCompleted());
    }

    @Test
    void pastIncompleteAssignmentIsOverdue() {
        Assignment assignment = new Assignment(
                1,
                "Late Homework",
                "",
                LocalDateTime.now().minusDays(1),
                Assignment.Priority.HIGH);

        assertTrue(assignment.isOverdue());
    }

    @Test
    void completedPastAssignmentIsNotOverdue() {
        Assignment assignment = new Assignment(
                1,
                "Finished Homework",
                "",
                LocalDateTime.now().minusDays(1),
                Assignment.Priority.HIGH);

        assignment.markCompleted();

        assertFalse(assignment.isOverdue());
    }

    @Test
    void futureAssignmentIsNotOverdue() {
        Assignment assignment = new Assignment(
                1,
                "Future Homework",
                "",
                LocalDateTime.now().plusDays(5),
                Assignment.Priority.LOW);

        assertFalse(assignment.isOverdue());
    }

    @Test
    void invalidCourseIdThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Assignment(
                        0,
                        "Homework",
                        "",
                        LocalDateTime.now().plusDays(1),
                        Assignment.Priority.MEDIUM));
    }

    @Test
    void blankTitleThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Assignment(
                        1,
                        "",
                        "",
                        LocalDateTime.now().plusDays(1),
                        Assignment.Priority.MEDIUM));
    }

    @Test
    void nullDueDateThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Assignment(
                        1,
                        "Homework",
                        "",
                        null,
                        Assignment.Priority.MEDIUM));
    }

    @Test
    void nullPriorityThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Assignment(
                        1,
                        "Homework",
                        "",
                        LocalDateTime.now().plusDays(1),
                        null));
    }

    @Test
    void equalAssignmentsAreEqual() {
        LocalDateTime dueDate =
                LocalDateTime.of(2026, 9, 30, 17, 0);

        Assignment first = new Assignment(
                10,
                1,
                "Project",
                "Complete final project",
                dueDate,
                Assignment.Priority.HIGH,
                false);

        Assignment second = new Assignment(
                10,
                1,
                "Project",
                "Complete final project",
                dueDate,
                Assignment.Priority.HIGH,
                false);

        assertEquals(first, second);
        assertEquals(
                first.hashCode(),
                second.hashCode());
    }

    private Assignment createAssignment(String title) {
        return new Assignment(
                1,
                title,
                "Test assignment",
                LocalDateTime.now().plusDays(3),
                Assignment.Priority.MEDIUM);
    }
}
