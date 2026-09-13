package com.studysync;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StudySessionTest {

    @Test
    void constructorCreatesValidStudySession() {
        LocalDateTime startTime =
                LocalDateTime.of(2026, 9, 13, 14, 30);

        StudySession session = new StudySession(
                1,
                startTime,
                90,
                "Reviewed data structures");

        assertEquals(1, session.getCourseId());
        assertEquals(startTime, session.getStartTime());
        assertEquals(90, session.getDurationMinutes());
        assertEquals(
                "Reviewed data structures",
                session.getNotes());
    }

    @Test
    void persistedStudySessionStoresId() {
        LocalDateTime startTime =
                LocalDateTime.of(2026, 9, 13, 10, 0);

        StudySession session = new StudySession(
                5,
                2,
                startTime,
                60,
                "Exam review");

        assertEquals(5, session.getId());
        assertEquals(2, session.getCourseId());
    }

    @Test
    void notesAreTrimmed() {
        StudySession session = new StudySession(
                1,
                LocalDateTime.now(),
                45,
                "  Reviewed Chapter 2  ");

        assertEquals(
                "Reviewed Chapter 2",
                session.getNotes());
    }

    @Test
    void nullNotesBecomeEmptyString() {
        StudySession session = new StudySession(
                1,
                LocalDateTime.now(),
                30,
                null);

        assertEquals("", session.getNotes());
    }

    @Test
    void durationMinutesConvertsToHours() {
        StudySession session = new StudySession(
                1,
                LocalDateTime.now(),
                90,
                "");

        assertEquals(
                1.5,
                session.getDurationHours(),
                0.001);
    }

    @Test
    void sixtyMinutesEqualsOneHour() {
        StudySession session = new StudySession(
                1,
                LocalDateTime.now(),
                60,
                "");

        assertEquals(
                1.0,
                session.getDurationHours(),
                0.001);
    }

    @Test
    void invalidCourseIdThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySession(
                        0,
                        LocalDateTime.now(),
                        60,
                        ""));
    }

    @Test
    void negativeCourseIdThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySession(
                        -1,
                        LocalDateTime.now(),
                        60,
                        ""));
    }

    @Test
    void nullStartTimeThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySession(
                        1,
                        null,
                        60,
                        ""));
    }

    @Test
    void zeroDurationThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySession(
                        1,
                        LocalDateTime.now(),
                        0,
                        ""));
    }

    @Test
    void negativeDurationThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StudySession(
                        1,
                        LocalDateTime.now(),
                        -30,
                        ""));
    }

    @Test
    void equalStudySessionsAreEqual() {
        LocalDateTime startTime =
                LocalDateTime.of(2026, 9, 13, 15, 0);

        StudySession first = new StudySession(
                10,
                1,
                startTime,
                120,
                "Final exam review");

        StudySession second = new StudySession(
                10,
                1,
                startTime,
                120,
                "Final exam review");

        assertEquals(first, second);
        assertEquals(
                first.hashCode(),
                second.hashCode());
    }

    @Test
    void toStringContainsSessionInformation() {
        LocalDateTime startTime =
                LocalDateTime.of(2026, 9, 13, 16, 0);

        StudySession session = new StudySession(
                1,
                startTime,
                75,
                "Practice problems");

        String result = session.toString();

        assertTrue(result.contains("Study Session"));
        assertTrue(result.contains("75 minutes"));
        assertTrue(result.contains("Practice problems"));
    }
}
