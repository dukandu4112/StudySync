package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StudyProgressAnalyticsTest {
    private Path databasePath;
    private StudySyncService service;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-study-progress-", ".db");
        service = new StudySyncService(new DatabaseManager("jdbc:sqlite:" + databasePath));
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void emptyWeekProducesZeroValuedProgress() {
        LocalDate referenceDate = LocalDate.of(2026, 9, 16);

        StudyProgressAnalytics analytics = service.getStudyProgressAnalytics(referenceDate);

        assertEquals(LocalDate.of(2026, 9, 14), analytics.weekStart());
        assertEquals(LocalDate.of(2026, 9, 20), analytics.weekEnd());
        assertEquals(0, analytics.totalStudyMinutes());
        assertEquals(0, analytics.sessionCount());
        assertEquals(0, analytics.studyDays());
        assertEquals(0, analytics.longestSessionMinutes());
        assertEquals(0.0, analytics.averageMinutesPerStudyDay());
    }

    @Test
    void analyticsSummarizeOnlySessionsInsideRequestedWeek() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        LocalDate referenceDate = LocalDate.of(2026, 9, 16);
        LocalDateTime monday = LocalDate.of(2026, 9, 14).atTime(9, 0);

        service.recordStudySession(course.getId(), monday, 30, "Stacks");
        service.recordStudySession(course.getId(), monday.plusHours(2), 45, "Queues");
        service.recordStudySession(course.getId(), monday.plusDays(2), 60, "Trees");
        service.recordStudySession(course.getId(), monday.minusDays(1), 120, "Previous week");
        service.recordStudySession(course.getId(), monday.plusDays(7), 90, "Next week");

        StudyProgressAnalytics analytics = service.getStudyProgressAnalytics(referenceDate);

        assertEquals(135, analytics.totalStudyMinutes());
        assertEquals(3, analytics.sessionCount());
        assertEquals(2, analytics.studyDays());
        assertEquals(60, analytics.longestSessionMinutes());
        assertEquals(67.5, analytics.averageMinutesPerStudyDay(), 0.001);
    }

    @Test
    void weekUsesMondayThroughSundayBoundaries() {
        Course course = service.createCourse("Linear Algebra", "MATH 2502");
        LocalDate sunday = LocalDate.of(2026, 9, 20);

        service.recordStudySession(course.getId(), LocalDate.of(2026, 9, 14).atStartOfDay(), 20, "Monday");
        service.recordStudySession(course.getId(), sunday.atTime(23, 59), 40, "Sunday");
        service.recordStudySession(course.getId(), LocalDate.of(2026, 9, 21).atStartOfDay(), 80, "Next Monday");

        StudyProgressAnalytics analytics = service.getStudyProgressAnalytics(sunday);

        assertEquals(60, analytics.totalStudyMinutes());
        assertEquals(2, analytics.sessionCount());
        assertEquals(2, analytics.studyDays());
    }

    @Test
    void analyticsRejectNullReferenceDate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getStudyProgressAnalytics(null));
    }

    @Test
    void analyticsRecordRejectsInvalidValues() {
        LocalDate start = LocalDate.of(2026, 9, 14);
        LocalDate end = LocalDate.of(2026, 9, 20);

        assertThrows(IllegalArgumentException.class,
                () -> new StudyProgressAnalytics(null, end, 0, 0, 0, 0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyProgressAnalytics(start, start.minusDays(1), 0, 0, 0, 0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyProgressAnalytics(start, end, -1, 0, 0, 0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyProgressAnalytics(start, end, 10, 0, 0, 0, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyProgressAnalytics(start, end, 10, 1, 8, 10, 10.0));
    }
}
