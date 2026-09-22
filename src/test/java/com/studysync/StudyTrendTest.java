package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudyTrendTest {
    @TempDir Path tempDir;

    private StudySyncService service() {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve("trend.db")));
    }

    @Test
    void emptyHistoryProducesSameZeroTrend() {
        StudyTrend trend = service().getStudyTrend(LocalDate.of(2026, 9, 22));
        assertEquals(LocalDate.of(2026, 9, 21), trend.currentWeekStart());
        assertEquals(0, trend.currentWeekMinutes());
        assertEquals(0, trend.previousWeekMinutes());
        assertEquals(0, trend.minuteChange());
        assertEquals(0.0, trend.percentageChange(), 0.001);
        assertEquals(StudyTrend.Direction.SAME, trend.direction());
    }

    @Test
    void calculatesUpwardWeeklyTrend() {
        StudySyncService service = service();
        Course course = service.createCourse("Linear Algebra", "MATH 2502");
        study(service, course, "2026-09-14T10:00", 60);
        study(service, course, "2026-09-16T10:00", 40);
        study(service, course, "2026-09-21T10:00", 90);
        study(service, course, "2026-09-22T10:00", 60);

        StudyTrend trend = service.getStudyTrend(LocalDate.of(2026, 9, 22));
        assertEquals(150, trend.currentWeekMinutes());
        assertEquals(100, trend.previousWeekMinutes());
        assertEquals(50, trend.minuteChange());
        assertEquals(50.0, trend.percentageChange(), 0.001);
        assertEquals(StudyTrend.Direction.UP, trend.direction());
    }

    @Test
    void calculatesDownwardWeeklyTrendAndWeekBoundaries() {
        StudySyncService service = service();
        Course course = service.createCourse("Data Structures", "CSCI 3412");
        study(service, course, "2026-09-13T23:59", 999);
        study(service, course, "2026-09-14T00:00", 100);
        study(service, course, "2026-09-20T23:59", 100);
        study(service, course, "2026-09-21T00:00", 50);
        study(service, course, "2026-09-27T23:59", 50);
        study(service, course, "2026-09-28T00:00", 999);

        StudyTrend trend = service.getStudyTrend(LocalDate.of(2026, 9, 22));
        assertEquals(100, trend.currentWeekMinutes());
        assertEquals(200, trend.previousWeekMinutes());
        assertEquals(-100, trend.minuteChange());
        assertEquals(-50.0, trend.percentageChange(), 0.001);
        assertEquals(StudyTrend.Direction.DOWN, trend.direction());
    }

    @Test
    void zeroPreviousWeekUsesZeroPercentageWithoutInfinity() {
        StudySyncService service = service();
        Course course = service.createCourse("Architecture", "CSCI 3320");
        study(service, course, "2026-09-22T10:00", 75);

        StudyTrend trend = service.getStudyTrend(LocalDate.of(2026, 9, 22));
        assertEquals(75, trend.currentWeekMinutes());
        assertEquals(0, trend.previousWeekMinutes());
        assertEquals(75, trend.minuteChange());
        assertEquals(0.0, trend.percentageChange(), 0.001);
        assertEquals(StudyTrend.Direction.UP, trend.direction());
    }

    @Test
    void rejectsNullReferenceDate() {
        assertThrows(IllegalArgumentException.class, () -> service().getStudyTrend(null));
    }

    @Test
    void modelValidatesStateAndDirection() {
        LocalDate monday = LocalDate.of(2026, 9, 21);
        assertThrows(IllegalArgumentException.class, () -> new StudyTrend(null, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new StudyTrend(monday, -1, 0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new StudyTrend(monday, 10, 5, 4, 100));
        assertThrows(IllegalArgumentException.class, () -> new StudyTrend(monday, 10, 5, 5, Double.POSITIVE_INFINITY));
        assertEquals(StudyTrend.Direction.SAME, new StudyTrend(monday, 10, 10, 0, 0).direction());
    }

    private void study(StudySyncService service, Course course, String start, int minutes) {
        service.recordStudySession(course.getId(), LocalDateTime.parse(start), minutes, "Study");
    }
}
