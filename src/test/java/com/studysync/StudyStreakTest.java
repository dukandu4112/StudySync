package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;

class StudyStreakTest {
    @TempDir Path tempDir;

    private StudySyncService service() {
        DatabaseManager database = new DatabaseManager("jdbc:sqlite:" + tempDir.resolve("streak.db"));
        database.initializeDatabase();
        return new StudySyncService(database);
    }

    @Test
    void emptyHistoryHasZeroStreak() {
        StudyStreak streak = service().getStudyStreak(LocalDate.of(2026, 9, 21));
        assertEquals(0, streak.currentStreakDays());
        assertEquals(0, streak.longestStreakDays());
        assertFalse(streak.studiedToday());
        assertNull(streak.lastStudyDate());
    }

    @Test
    void calculatesCurrentAndLongestStreakFromDistinctStudyDays() {
        StudySyncService service = service();
        Course course = service.createCourse("Linear Algebra", "MATH 2502");
        study(service, course, "2026-09-10T10:00");
        study(service, course, "2026-09-11T10:00");
        study(service, course, "2026-09-12T10:00");
        study(service, course, "2026-09-18T10:00");
        study(service, course, "2026-09-19T10:00");
        study(service, course, "2026-09-20T10:00");
        study(service, course, "2026-09-21T09:00");
        study(service, course, "2026-09-21T18:00");

        StudyStreak streak = service.getStudyStreak(LocalDate.of(2026, 9, 21));
        assertEquals(4, streak.currentStreakDays());
        assertEquals(4, streak.longestStreakDays());
        assertTrue(streak.studiedToday());
        assertEquals(LocalDate.of(2026, 9, 21), streak.lastStudyDate());
    }

    @Test
    void currentStreakUsesMostRecentStudyRunWhenTodayHasNoSession() {
        StudySyncService service = service();
        Course course = service.createCourse("Data Structures", "CSCI 3412");
        study(service, course, "2026-09-17T10:00");
        study(service, course, "2026-09-18T10:00");
        study(service, course, "2026-09-19T10:00");

        StudyStreak streak = service.getStudyStreak(LocalDate.of(2026, 9, 21));
        assertEquals(3, streak.currentStreakDays());
        assertEquals(3, streak.longestStreakDays());
        assertFalse(streak.studiedToday());
        assertEquals(LocalDate.of(2026, 9, 19), streak.lastStudyDate());
    }

    @Test
    void ignoresStudySessionsAfterReferenceDate() {
        StudySyncService service = service();
        Course course = service.createCourse("Architecture", "CSCI 3320");
        study(service, course, "2026-09-20T10:00");
        study(service, course, "2026-09-21T10:00");
        study(service, course, "2026-09-22T10:00");

        StudyStreak streak = service.getStudyStreak(LocalDate.of(2026, 9, 21));
        assertEquals(2, streak.currentStreakDays());
        assertEquals(2, streak.longestStreakDays());
        assertEquals(LocalDate.of(2026, 9, 21), streak.lastStudyDate());
    }

    @Test
    void rejectsNullReferenceDate() {
        assertThrows(IllegalArgumentException.class, () -> service().getStudyStreak(null));
    }

    @Test
    void modelRejectsInvalidState() {
        LocalDate date = LocalDate.of(2026, 9, 21);
        assertThrows(IllegalArgumentException.class, () -> new StudyStreak(null, 0, 0, false, null));
        assertThrows(IllegalArgumentException.class, () -> new StudyStreak(date, -1, 0, false, date));
        assertThrows(IllegalArgumentException.class, () -> new StudyStreak(date, 2, 1, true, date));
        assertThrows(IllegalArgumentException.class, () -> new StudyStreak(date, 1, 1, false, date.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new StudyStreak(date, 1, 1, true, date.minusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new StudyStreak(date, 1, 1, false, null));
    }

    private void study(StudySyncService service, Course course, String start) {
        service.recordStudySession(course.getId(), LocalDateTime.parse(start), 30, "Study");
    }
}
