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
    void courseFilteringSearchesCodeAndNameAndSortsByCode() {
        Course dataStructures = new Course(1, "Data Structures", "CSCI 3300");
        Course architecture = new Course(2, "Computer Organization & Architecture", "CSCI 3212");
        Course linearAlgebra = new Course(3, "Linear Algebra", "MATH 2502");
        List<Course> courses = List.of(linearAlgebra, dataStructures, architecture);

        assertEquals(List.of(architecture, dataStructures, linearAlgebra),
                UiSupport.filterCourses(courses, ""));
        assertEquals(List.of(dataStructures), UiSupport.filterCourses(courses, "3300"));
        assertEquals(List.of(linearAlgebra), UiSupport.filterCourses(courses, "linear"));
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
        completed.markCompleted();
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
    void studySessionFilteringSupportsCourseNotesAndNewestFirst() {
        LocalDateTime now = LocalDateTime.now();
        StudySession older = new StudySession(1, now.minusDays(2), 30, "Trees review");
        StudySession newest = new StudySession(1, now.minusHours(1), 45, "Graph practice");
        StudySession otherCourse = new StudySession(2, now.minusMinutes(30), 25, "Matrix practice");
        List<StudySession> sessions = List.of(older, otherCourse, newest);

        assertEquals(List.of(newest, older),
                UiSupport.filterStudySessions(sessions, 1, ""));
        assertEquals(List.of(newest),
                UiSupport.filterStudySessions(sessions, null, "graph"));
        assertEquals(List.of(otherCourse),
                UiSupport.filterStudySessions(sessions, 2, "practice"));
    }

    @Test
    void nullCollectionsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.filterCourses(null, ""));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.filterAssignments(null, "", "All", "All"));
        assertThrows(IllegalArgumentException.class,
                () -> UiSupport.filterStudySessions(null, null, ""));
    }
}
