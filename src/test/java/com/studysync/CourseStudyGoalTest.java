package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CourseStudyGoalTest {
    @TempDir Path tempDir;

    @Test
    void newCourseUsesDefaultWeeklyTarget() {
        StudySyncService service = service("default.db");
        Course course = service.createCourse("Data Structures", "CSCI 2305");
        CourseStudyGoal goal = service.getCourseStudyGoal(course.getId(), LocalDate.of(2026, 9, 30));
        assertEquals(180, goal.targetMinutes());
        assertEquals(0, goal.studiedMinutes());
        assertEquals(180, goal.remainingMinutes());
        assertFalse(goal.completed());
    }

    @Test
    void countsOnlyRequestedCourseAndCurrentWeek() {
        StudySyncService service = service("weekly.db");
        Course data = service.createCourse("Data Structures", "CSCI 2305");
        Course architecture = service.createCourse("Architecture", "CSCI 2302");
        service.recordStudySession(data.getId(), LocalDateTime.of(2026, 9, 28, 9, 0), 60, "Monday");
        service.recordStudySession(data.getId(), LocalDateTime.of(2026, 10, 1, 18, 0), 45, "Thursday");
        service.recordStudySession(architecture.getId(), LocalDateTime.of(2026, 9, 30, 12, 0), 120, "Other course");
        service.recordStudySession(data.getId(), LocalDateTime.of(2026, 9, 27, 20, 0), 90, "Previous week");
        CourseStudyGoal goal = service.getCourseStudyGoal(data.getId(), LocalDate.of(2026, 10, 2));
        assertEquals(105, goal.studiedMinutes());
        assertEquals(75, goal.remainingMinutes());
        assertEquals(58.333, goal.completionPercentage(), 0.01);
    }

    @Test
    void targetPersistsForIndividualCourse() {
        String url = "jdbc:sqlite:" + tempDir.resolve("persistent.db");
        StudySyncService first = new StudySyncService(new DatabaseManager(url));
        Course course = first.createCourse("Linear Algebra", "MATH 2140");
        first.setCourseStudyTargetMinutes(course.getId(), 240);
        StudySyncService reopened = new StudySyncService(new DatabaseManager(url));
        assertEquals(240, reopened.getCourseStudyTargetMinutes(course.getId()));
        assertEquals(240, reopened.getCourseStudyGoal(course.getId()).targetMinutes());
    }

    @Test
    void completedGoalCapsProgressAtOneHundredPercent() {
        StudySyncService service = service("complete.db");
        Course course = service.createCourse("Architecture", "CSCI 2302");
        service.setCourseStudyTargetMinutes(course.getId(), 60);
        LocalDate date = LocalDate.of(2026, 10, 2);
        service.recordStudySession(course.getId(), date.atTime(10, 0), 90, "Long session");
        CourseStudyGoal goal = service.getCourseStudyGoal(course.getId(), date);
        assertTrue(goal.completed());
        assertEquals(0, goal.remainingMinutes());
        assertEquals(100.0, goal.completionPercentage(), 0.001);
    }

    private StudySyncService service(String fileName) {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve(fileName)));
    }
}
