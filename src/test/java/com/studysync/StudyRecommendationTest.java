package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StudyRecommendationTest {
    @TempDir Path tempDir;

    @Test
    void overdueCourseRanksAheadOfGoalOnlyCourse() {
        StudySyncService service = service();
        Course urgent = service.createCourse("Data Structures", "CSCI 2305");
        service.createCourse("Linear Algebra", "MATH 2140");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.createAssignment(urgent.getId(), "Project", "", now.minusHours(2), Assignment.Priority.HIGH);

        List<StudyRecommendation> recommendations = service.getStudyRecommendations(now);

        assertEquals(urgent.getId(), recommendations.get(0).course().getId());
        assertTrue(recommendations.get(0).score() > recommendations.get(1).score());
        assertEquals("Overdue work needs attention", recommendations.get(0).reason());
    }

    @Test
    void dueSoonAndHighPriorityIncreaseRecommendation() {
        StudySyncService service = service();
        Course architecture = service.createCourse("Architecture", "CSCI 2302");
        Course algebra = service.createCourse("Linear Algebra", "MATH 2140");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.createAssignment(architecture.getId(), "Exam review", "", now.plusHours(20), Assignment.Priority.HIGH);
        service.createAssignment(algebra.getId(), "Homework", "", now.plusDays(5), Assignment.Priority.LOW);

        List<StudyRecommendation> recommendations = service.getStudyRecommendations(now);

        StudyRecommendation first = recommendations.get(0);
        assertEquals(architecture.getId(), first.course().getId());
        assertTrue(first.hasDueSoonAssignment());
        assertEquals(1, first.highPriorityAssignments());
        assertEquals("Assignment due within 48 hours", first.reason());
    }

    @Test
    void studyProgressReducesCourseNeedScore() {
        StudySyncService service = service();
        Course studied = service.createCourse("Architecture", "CSCI 2302");
        Course untouched = service.createCourse("Data Structures", "CSCI 2305");
        LocalDateTime now = LocalDateTime.of(2026, 10, 2, 12, 0);
        service.recordStudySession(studied.getId(), LocalDateTime.of(2026, 10, 1, 10, 0), 180, "Weekly goal complete");

        List<StudyRecommendation> recommendations = service.getStudyRecommendations(now);

        assertEquals(untouched.getId(), recommendations.get(0).course().getId());
        StudyRecommendation completed = recommendations.stream().filter(r -> r.course().getId() == studied.getId()).findFirst().orElseThrow();
        assertEquals(0, completed.weeklyRemainingMinutes());
        assertEquals("Weekly goal is on track", completed.reason());
    }

    @Test
    void noCoursesProducesNoRecommendations() {
        assertTrue(service().getStudyRecommendations(LocalDateTime.of(2026, 10, 2, 12, 0)).isEmpty());
    }

    private StudySyncService service() {
        return new StudySyncService(new DatabaseManager("jdbc:sqlite:" + tempDir.resolve("recommendations.db")));
    }
}
