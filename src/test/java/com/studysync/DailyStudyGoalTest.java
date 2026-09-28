package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DailyStudyGoalTest {
    @TempDir Path tempDir;

    private StudySyncService service() {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve("daily-goal.db")));
    }

    @Test
    void emptyDayStartsWithFullGoalRemaining() {
        DailyStudyGoal goal = service().getDailyStudyGoal(LocalDate.of(2026, 9, 28), 120);
        assertEquals(120, goal.targetMinutes());
        assertEquals(0, goal.studiedMinutes());
        assertEquals(120, goal.remainingMinutes());
        assertEquals(0.0, goal.completionPercentage(), 0.001);
        assertFalse(goal.completed());
    }

    @Test
    void goalUsesOnlySessionsFromRequestedDay() {
        StudySyncService service = service();
        Course course = service.createCourse("Data Structures", "CSCI 2305");
        LocalDate date = LocalDate.of(2026, 9, 28);
        service.recordStudySession(course.getId(), LocalDateTime.of(2026, 9, 27, 23, 30), 90, "Previous day");
        service.recordStudySession(course.getId(), date.atTime(9, 0), 35, "Morning");
        service.recordStudySession(course.getId(), date.atTime(18, 0), 40, "Evening");
        service.recordStudySession(course.getId(), LocalDateTime.of(2026, 9, 29, 0, 0), 60, "Next day");

        DailyStudyGoal goal = service.getDailyStudyGoal(date, 100);

        assertEquals(75, goal.studiedMinutes());
        assertEquals(25, goal.remainingMinutes());
        assertEquals(75.0, goal.completionPercentage(), 0.001);
        assertFalse(goal.completed());
    }

    @Test
    void completedGoalCapsPercentageAndRemainingMinutes() {
        StudySyncService service = service();
        Course course = service.createCourse("Architecture", "CSCI 2302");
        LocalDate date = LocalDate.of(2026, 9, 28);
        service.recordStudySession(course.getId(), date.atTime(10, 0), 80, "Session one");
        service.recordStudySession(course.getId(), date.atTime(14, 0), 70, "Session two");

        DailyStudyGoal goal = service.getDailyStudyGoal(date, 120);

        assertEquals(150, goal.studiedMinutes());
        assertEquals(0, goal.remainingMinutes());
        assertEquals(100.0, goal.completionPercentage(), 0.001);
        assertTrue(goal.completed());
    }

    @Test
    void rejectsInvalidGoalInputs() {
        StudySyncService service = service();
        assertThrows(IllegalArgumentException.class, () -> service.getDailyStudyGoal(null, 60));
        assertThrows(IllegalArgumentException.class, () -> service.getDailyStudyGoal(LocalDate.now(), 0));
        assertThrows(IllegalArgumentException.class, () -> service.getDailyStudyGoal(LocalDate.now(), -1));
    }
}
