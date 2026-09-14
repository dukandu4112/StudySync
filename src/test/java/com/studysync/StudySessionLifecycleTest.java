package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StudySessionLifecycleTest {
    private Path databasePath;
    private StudySyncService service;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-session-lifecycle-", ".db");
        service = new StudySyncService(new DatabaseManager("jdbc:sqlite:" + databasePath));
    }

    @AfterEach
    void tearDown() throws Exception { Files.deleteIfExists(databasePath); }

    @Test
    void updateStudySessionChangesStoredDetailsAndTotals() {
        Course first = service.createCourse("Data Structures", "CSCI 3300");
        Course second = service.createCourse("Linear Algebra", "MATH 2502");
        StudySession session = service.recordStudySession(first.getId(), LocalDateTime.of(2026, 9, 14, 10, 0), 30, "Trees");
        LocalDateTime revisedStart = LocalDateTime.of(2026, 9, 15, 14, 30);

        assertTrue(service.updateStudySession(session.getId(), second.getId(), revisedStart, 75, "Matrix review"));

        StudySession updated = service.getStudySessions().get(0);
        assertEquals(second.getId(), updated.getCourseId());
        assertEquals(revisedStart, updated.getStartTime());
        assertEquals(75, updated.getDurationMinutes());
        assertEquals("Matrix review", updated.getNotes());
        assertEquals(75, service.getTotalStudyMinutes());
        assertEquals(0, service.getTotalStudyMinutesForCourse(first.getId()));
        assertEquals(75, service.getTotalStudyMinutesForCourse(second.getId()));
    }

    @Test
    void updateStudySessionRejectsMissingSession() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        assertThrows(IllegalArgumentException.class, () -> service.updateStudySession(999, course.getId(), LocalDateTime.now(), 60, "Review"));
    }

    @Test
    void updateStudySessionRejectsMissingDestinationCourse() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        StudySession session = service.recordStudySession(course.getId(), LocalDateTime.now(), 60, "Review");
        assertThrows(IllegalArgumentException.class, () -> service.updateStudySession(session.getId(), 999, LocalDateTime.now(), 45, "Updated"));
    }

    @Test
    void updateStudySessionRejectsInvalidValues() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        StudySession session = service.recordStudySession(course.getId(), LocalDateTime.now(), 60, "Review");
        assertThrows(IllegalArgumentException.class, () -> service.updateStudySession(session.getId(), course.getId(), null, 60, "Review"));
        assertThrows(IllegalArgumentException.class, () -> service.updateStudySession(session.getId(), course.getId(), LocalDateTime.now(), 0, "Review"));
    }

    @Test
    void deleteStudySessionRemovesSessionAndUpdatesTotal() {
        Course course = service.createCourse("Computer Architecture", "CSCI 3212");
        StudySession first = service.recordStudySession(course.getId(), LocalDateTime.now(), 40, "Binary review");
        service.recordStudySession(course.getId(), LocalDateTime.now().plusHours(1), 20, "IEEE 754");

        assertTrue(service.deleteStudySession(first.getId()));
        assertEquals(1, service.getStudySessions().size());
        assertEquals(20, service.getTotalStudyMinutes());
    }

    @Test
    void deleteStudySessionRejectsMissingSession() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteStudySession(999));
    }
}
