package com.studysync;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UiSupportTest {

    @Test
    void parseDateTimeCombinesDateAndTime() {
        assertEquals(LocalDateTime.of(2026, 9, 14, 14, 30),
                UiSupport.parseDateTime(LocalDate.of(2026, 9, 14), "14:30"));
    }

    @Test
    void parseDateTimeRejectsMissingOrInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.parseDateTime(null, "14:30"));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.parseDateTime(LocalDate.now(), ""));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.parseDateTime(LocalDate.now(), "2:30 PM"));
    }

    @Test
    void positiveMinutesAreParsedAndValidated() {
        assertEquals(45, UiSupport.parsePositiveMinutes(" 45 "));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.parsePositiveMinutes("0"));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.parsePositiveMinutes("-5"));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.parsePositiveMinutes("forty"));
    }

    @Test
    void assignmentFilteringSupportsSearchStatusAndPriority() {
        LocalDateTime now = LocalDateTime.now();
        Assignment highPending = new Assignment(1, "Architecture Review",
                "Study pipeline", now.plusDays(2), Assignment.Priority.HIGH);
        Assignment lowOverdue = new Assignment(1, "Old Homework",
                "Matrix practice", now.minusDays(2), Assignment.Priority.LOW);
        Assignment completed = new Assignment(1, "Finished Project",
                "Java implementation", now.plusDays(3), Assignment.Priority.HIGH);
        completed.complete();
        List<Assignment> assignments = List.of(highPending, lowOverdue, completed);

        assertEquals(List.of(highPending),
                UiSupport.filterAssignments(assignments, "pipeline", "All", "All"));
        assertEquals(List.of(lowOverdue),
                UiSupport.filterAssignments(assignments, "", "Overdue", "All"));
        assertEquals(List.of(highPending),
                UiSupport.filterAssignments(assignments, "", "Pending", "HIGH"));
        assertEquals(List.of(completed),
                UiSupport.filterAssignments(assignments, "", "Completed", "HIGH"));
    }

    @Test
    void nullAssignmentsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.filterAssignments(null, "", "All", "All"));
    }
}
