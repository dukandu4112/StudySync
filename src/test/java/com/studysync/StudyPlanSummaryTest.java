package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudyPlanSummaryTest {
    @TempDir Path tempDir;

    @Test
    void summaryReportsPlanCoverage() {
        StudySyncService service = service("coverage.db");
        Course first = service.createCourse("Data Structures", "CSCI 2305");
        Course second = service.createCourse("Architecture", "CSCI 2302");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.setDailyStudyTargetMinutes(90);
        service.createAssignment(first.getId(), "Project", "", now.plusHours(12), Assignment.Priority.HIGH);
        service.createAssignment(second.getId(), "Lab", "", now.plusHours(20), Assignment.Priority.HIGH);

        StudyPlanSummary summary = service.getStudyPlanSummary(now);

        assertEquals(90, summary.plannedMinutes());
        assertEquals(2, summary.blockCount());
        assertEquals(90, summary.dailyTargetMinutes());
        assertEquals(0, summary.studiedTodayMinutes());
        assertEquals(0, summary.remainingAfterPlanMinutes());
        assertEquals(90, summary.accountedMinutes());
        assertEquals(90, summary.remainingStudyMinutes());
        assertEquals(100.0, summary.coveragePercentage(), 0.001);
        assertEquals("Fully planned", summary.workloadStatus());
        assertTrue(summary.coversRemainingDailyTarget());
    }

    @Test
    void summaryIncludesMinutesAlreadyStudiedAndUncoveredTime() {
        StudySyncService service = service("studied.db");
        Course course = service.createCourse("Linear Algebra", "MATH 2140");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.recordStudySession(course.getId(), now.minusHours(1), 45, "Morning study");

        StudyPlanSummary summary = service.getStudyPlanSummary(now);

        assertEquals(120, summary.dailyTargetMinutes());
        assertEquals(45, summary.studiedTodayMinutes());
        assertEquals(30, summary.plannedMinutes());
        assertEquals(1, summary.blockCount());
        assertEquals(45, summary.remainingAfterPlanMinutes());
        assertEquals(75, summary.accountedMinutes());
        assertEquals(75, summary.remainingStudyMinutes());
        assertEquals(62.5, summary.coveragePercentage(), 0.001);
        assertEquals("Partially planned", summary.workloadStatus());
        assertFalse(summary.coversRemainingDailyTarget());
    }

    @Test
    void completedDailyGoalNeedsNoPlanAndCapsCoverageAtOneHundredPercent() {
        StudySyncService service = service("complete.db");
        Course course = service.createCourse("Architecture", "CSCI 2302");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.recordStudySession(course.getId(), now.minusMinutes(30), 150, "Done plus extra study");

        StudyPlanSummary summary = service.getStudyPlanSummary(now);

        assertEquals(0, summary.plannedMinutes());
        assertEquals(0, summary.blockCount());
        assertEquals(0, summary.remainingAfterPlanMinutes());
        assertEquals(120, summary.accountedMinutes());
        assertEquals(0, summary.remainingStudyMinutes());
        assertEquals(100.0, summary.coveragePercentage(), 0.001);
        assertEquals("Goal complete", summary.workloadStatus());
        assertTrue(summary.coversRemainingDailyTarget());
    }

    @Test
    void noAvailablePlanReportsNeedsPlanning() {
        StudyPlanSummary summary = new StudyPlanSummary(0, 0, 120, 30, 90);

        assertEquals(90, summary.remainingStudyMinutes());
        assertEquals("Needs planning", summary.workloadStatus());
        assertFalse(summary.coversRemainingDailyTarget());
    }

    private StudySyncService service(String fileName) {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve(fileName)));
    }
}
