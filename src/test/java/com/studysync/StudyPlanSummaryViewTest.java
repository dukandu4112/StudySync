package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StudyPlanSummaryViewTest {

    @Test
    void rejectsNullSummaryWithoutStartingJavaFxToolkit() {
        assertThrows(IllegalArgumentException.class, () -> StudyPlanSummaryView.create(null));
    }

    @Test
    void summaryModelSupportsCoveredDashboardState() {
        StudyPlanSummary summary = new StudyPlanSummary(90, 2, 120, 30, 0);

        assertEquals(90, summary.plannedMinutes());
        assertEquals(2, summary.blockCount());
        assertEquals(30, summary.studiedTodayMinutes());
        assertEquals(0, summary.remainingAfterPlanMinutes());
        assertTrue(summary.coversRemainingDailyTarget());
    }

    @Test
    void summaryModelSupportsUncoveredDashboardState() {
        StudyPlanSummary summary = new StudyPlanSummary(30, 1, 120, 45, 45);

        assertEquals(45, summary.remainingAfterPlanMinutes());
        assertFalse(summary.coversRemainingDailyTarget());
    }

    @Test
    void summaryModelSupportsCompletedGoalState() {
        StudyPlanSummary summary = new StudyPlanSummary(0, 0, 120, 120, 0);

        assertEquals(0, summary.plannedMinutes());
        assertEquals(0, summary.blockCount());
        assertTrue(summary.coversRemainingDailyTarget());
    }

    @Test
    void summaryModelSupportsNoAvailableBlocksState() {
        StudyPlanSummary summary = new StudyPlanSummary(0, 0, 120, 30, 90);

        assertEquals(0, summary.plannedMinutes());
        assertEquals(90, summary.remainingAfterPlanMinutes());
        assertFalse(summary.coversRemainingDailyTarget());
    }
}
