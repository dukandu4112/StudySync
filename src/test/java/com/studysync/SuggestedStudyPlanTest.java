package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SuggestedStudyPlanTest {
    @TempDir Path tempDir;

    @Test
    void urgentHighPriorityCourseGetsSixtyMinuteBlock() {
        StudySyncService service = service("urgent.db");
        Course course = service.createCourse("Data Structures", "CSCI 2305");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.createAssignment(course.getId(), "Project", "", now.plusHours(12), Assignment.Priority.HIGH);

        List<StudyPlanItem> plan = service.getSuggestedStudyPlan(now);

        assertFalse(plan.isEmpty());
        assertEquals(course.getId(), plan.get(0).course().getId());
        assertEquals(60, plan.get(0).suggestedMinutes());
    }

    @Test
    void pendingCourseGetsFortyFiveMinuteBlock() {
        StudySyncService service = service("pending.db");
        Course course = service.createCourse("Linear Algebra", "MATH 2140");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.createAssignment(course.getId(), "Homework", "", now.plusDays(5), Assignment.Priority.LOW);

        assertEquals(45, service.getSuggestedStudyPlan(now).get(0).suggestedMinutes());
    }

    @Test
    void goalOnlyCourseGetsThirtyMinuteBlock() {
        StudySyncService service = service("goal.db");
        service.createCourse("Architecture", "CSCI 2302");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);

        assertEquals(30, service.getSuggestedStudyPlan(now).get(0).suggestedMinutes());
    }

    @Test
    void planNeverExceedsRemainingDailyGoal() {
        StudySyncService service = service("limit.db");
        Course first = service.createCourse("Data Structures", "CSCI 2305");
        Course second = service.createCourse("Architecture", "CSCI 2302");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.setDailyStudyTargetMinutes(90);
        service.createAssignment(first.getId(), "Project", "", now.plusHours(12), Assignment.Priority.HIGH);
        service.createAssignment(second.getId(), "Lab", "", now.plusHours(20), Assignment.Priority.HIGH);

        int total = service.getSuggestedStudyPlan(now).stream().mapToInt(StudyPlanItem::suggestedMinutes).sum();
        assertEquals(90, total);
    }

    @Test
    void completedDailyGoalProducesNoPlan() {
        StudySyncService service = service("complete.db");
        Course course = service.createCourse("Data Structures", "CSCI 2305");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.recordStudySession(course.getId(), now.minusHours(1), 120, "Daily target complete");

        assertTrue(service.getSuggestedStudyPlan(now).isEmpty());
    }

    private StudySyncService service(String fileName) {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve(fileName)));
    }
}
