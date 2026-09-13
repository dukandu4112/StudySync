package com.studysync;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DashboardSummaryTest {

    @Test
    void validSummaryStoresMetrics() {
        DashboardSummary summary = new DashboardSummary(
                3,
                10,
                6,
                4,
                2,
                180,
                40.0);

        assertEquals(3, summary.totalCourses());
        assertEquals(10, summary.totalAssignments());
        assertEquals(6, summary.pendingAssignments());
        assertEquals(4, summary.completedAssignments());
        assertEquals(2, summary.overdueAssignments());
        assertEquals(180, summary.totalStudyMinutes());
        assertEquals(40.0, summary.completionPercentage(), 0.001);
    }

    @Test
    void negativeCountsAreRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DashboardSummary(
                        -1, 0, 0, 0, 0, 0, 0.0));
    }

    @Test
    void completionPercentageBelowZeroIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DashboardSummary(
                        0, 0, 0, 0, 0, 0, -0.1));
    }

    @Test
    void completionPercentageAboveOneHundredIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DashboardSummary(
                        0, 0, 0, 0, 0, 0, 100.1));
    }
}
