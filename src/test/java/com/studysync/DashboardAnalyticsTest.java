package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DashboardAnalyticsTest {
    private Path databasePath;
    private StudySyncService service;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-dashboard-analytics-", ".db");
        service = new StudySyncService(new DatabaseManager("jdbc:sqlite:" + databasePath));
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void emptyWorkspaceProducesEmptyAnalytics() {
        DashboardAnalytics analytics = service.getDashboardAnalytics();

        assertEquals(0, analytics.upcomingAssignments());
        assertEquals(0, analytics.highPriorityPendingAssignments());
        assertNull(analytics.nearestDeadline());
        assertNull(analytics.mostStudiedCourse());
        assertEquals(0, analytics.mostStudiedCourseMinutes());
    }

    @Test
    void analyticsCountUpcomingAndHighPriorityPendingAssignments() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        LocalDateTime now = LocalDateTime.now();
        service.createAssignment(course.getId(), "Near", "", now.plusDays(2), Assignment.Priority.HIGH);
        Assignment completed = service.createAssignment(course.getId(), "Done", "", now.plusDays(3), Assignment.Priority.HIGH);
        service.completeAssignment(completed.getId());
        service.createAssignment(course.getId(), "Later", "", now.plusDays(10), Assignment.Priority.HIGH);
        service.createAssignment(course.getId(), "Medium", "", now.plusDays(4), Assignment.Priority.MEDIUM);

        DashboardAnalytics analytics = service.getDashboardAnalytics();

        assertEquals(2, analytics.upcomingAssignments());
        assertEquals(2, analytics.highPriorityPendingAssignments());
    }

    @Test
    void analyticsSelectNearestUpcomingPendingDeadline() {
        Course course = service.createCourse("Linear Algebra", "MATH 2502");
        LocalDateTime now = LocalDateTime.now();
        service.createAssignment(course.getId(), "Later", "", now.plusDays(5), Assignment.Priority.LOW);
        Assignment nearest = service.createAssignment(course.getId(), "Nearest", "", now.plusHours(6), Assignment.Priority.MEDIUM);
        service.createAssignment(course.getId(), "Overdue", "", now.minusDays(1), Assignment.Priority.HIGH);

        DashboardAnalytics analytics = service.getDashboardAnalytics();

        assertNotNull(analytics.nearestDeadline());
        assertEquals(nearest.getId(), analytics.nearestDeadline().getId());
    }

    @Test
    void completedAssignmentIsNotNearestDeadline() {
        Course course = service.createCourse("Architecture", "CSCI 3212");
        LocalDateTime now = LocalDateTime.now();
        Assignment completed = service.createAssignment(course.getId(), "Completed Soon", "", now.plusHours(1), Assignment.Priority.HIGH);
        service.completeAssignment(completed.getId());
        Assignment pending = service.createAssignment(course.getId(), "Pending", "", now.plusHours(3), Assignment.Priority.MEDIUM);

        assertEquals(pending.getId(), service.getDashboardAnalytics().nearestDeadline().getId());
    }

    @Test
    void analyticsSelectMostStudiedCourseAndMinutes() {
        Course dataStructures = service.createCourse("Data Structures", "CSCI 3300");
        Course linearAlgebra = service.createCourse("Linear Algebra", "MATH 2502");
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        service.recordStudySession(dataStructures.getId(), start, 35, "Trees");
        service.recordStudySession(dataStructures.getId(), start.plusHours(1), 25, "Graphs");
        service.recordStudySession(linearAlgebra.getId(), start, 45, "Matrices");

        DashboardAnalytics analytics = service.getDashboardAnalytics();

        assertNotNull(analytics.mostStudiedCourse());
        assertEquals(dataStructures.getId(), analytics.mostStudiedCourse().getId());
        assertEquals(60, analytics.mostStudiedCourseMinutes());
    }

    @Test
    void dashboardAnalyticsRejectNegativeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new DashboardAnalytics(-1, 0, null, null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new DashboardAnalytics(0, -1, null, null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new DashboardAnalytics(0, 0, null, null, 5));
    }
}
