package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    private Path databasePath;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile(
                "studysync-test-",
                ".db");

        String databaseUrl =
                "jdbc:sqlite:" + databasePath;

        databaseManager =
                new DatabaseManager(databaseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void addCourseStoresCourse() {
        Course course = databaseManager.addCourse(
                "Data Structures",
                "CSCI 3300");

        assertTrue(course.getId() > 0);
        assertEquals(
                "Data Structures",
                course.getName());
        assertEquals(
                "CSCI 3300",
                course.getCode());
    }

    @Test
    void getAllCoursesReturnsStoredCourses() {
        databaseManager.addCourse(
                "Data Structures",
                "CSCI 3300");

        databaseManager.addCourse(
                "Linear Algebra",
                "MATH 2502");

        List<Course> courses =
                databaseManager.getAllCourses();

        assertEquals(2, courses.size());
    }

    @Test
    void findCourseByIdReturnsCourse() {
        Course saved = databaseManager.addCourse(
                "Computer Architecture",
                "CSCI 3212");

        Course found =
                databaseManager.findCourseById(
                        saved.getId());

        assertNotNull(found);
        assertEquals(saved, found);
    }

    @Test
    void updateCourseChangesStoredCourse() {
        Course course = databaseManager.addCourse(
                "Data Structures",
                "CSCI 3300");

        boolean updated =
                databaseManager.updateCourse(
                        course.getId(),
                        "Data Structures and Algorithms",
                        "CSCI 3300");

        assertTrue(updated);

        Course result =
                databaseManager.findCourseById(
                        course.getId());

        assertEquals(
                "Data Structures and Algorithms",
                result.getName());
    }

    @Test
    void deleteCourseRemovesCourse() {
        Course course = databaseManager.addCourse(
                "Linear Algebra",
                "MATH 2502");

        boolean deleted =
                databaseManager.deleteCourse(
                        course.getId());

        assertTrue(deleted);

        assertNull(
                databaseManager.findCourseById(
                        course.getId()));
    }

    @Test
    void addAssignmentStoresAssignment() {
        Course course = databaseManager.addCourse(
                "Data Structures",
                "CSCI 3300");

        LocalDateTime dueDate =
                LocalDateTime.of(
                        2026,
                        9,
                        20,
                        23,
                        59);

        Assignment assignment =
                databaseManager.addAssignment(
                        course.getId(),
                        "Homework 1",
                        "Complete problems",
                        dueDate,
                        Assignment.Priority.HIGH);

        assertTrue(assignment.getId() > 0);
        assertEquals(
                course.getId(),
                assignment.getCourseId());
        assertEquals(
                "Homework 1",
                assignment.getTitle());
    }

    @Test
    void assignmentCompletionStatusCanBeUpdated() {
        Course course = databaseManager.addCourse(
                "Data Structures",
                "CSCI 3300");

        Assignment assignment =
                databaseManager.addAssignment(
                        course.getId(),
                        "Homework",
                        "",
                        LocalDateTime.now().plusDays(2),
                        Assignment.Priority.MEDIUM);

        assertFalse(assignment.isCompleted());

        boolean updated =
                databaseManager.setAssignmentCompleted(
                        assignment.getId(),
                        true);

        assertTrue(updated);

        Assignment result =
                databaseManager.findAssignmentById(
                        assignment.getId());

        assertTrue(result.isCompleted());
    }

    @Test
    void getAssignmentsByCourseFiltersAssignments() {
        Course firstCourse =
                databaseManager.addCourse(
                        "Data Structures",
                        "CSCI 3300");

        Course secondCourse =
                databaseManager.addCourse(
                        "Linear Algebra",
                        "MATH 2502");

        databaseManager.addAssignment(
                firstCourse.getId(),
                "DSA Homework",
                "",
                LocalDateTime.now().plusDays(2),
                Assignment.Priority.HIGH);

        databaseManager.addAssignment(
                secondCourse.getId(),
                "Matrix Homework",
                "",
                LocalDateTime.now().plusDays(3),
                Assignment.Priority.MEDIUM);

        List<Assignment> assignments =
                databaseManager.getAssignmentsByCourse(
                        firstCourse.getId());

        assertEquals(1, assignments.size());
        assertEquals(
                "DSA Homework",
                assignments.get(0).getTitle());
    }

    @Test
    void addStudySessionStoresSession() {
        Course course = databaseManager.addCourse(
                "Computer Architecture",
                "CSCI 3212");

        LocalDateTime startTime =
                LocalDateTime.of(
                        2026,
                        9,
                        13,
                        14,
                        0);

        StudySession session =
                databaseManager.addStudySession(
                        course.getId(),
                        startTime,
                        90,
                        "Reviewed lecture notes");

        assertTrue(session.getId() > 0);
        assertEquals(
                course.getId(),
                session.getCourseId());
        assertEquals(
                90,
                session.getDurationMinutes());
    }

    @Test
    void getStudySessionsByCourseFiltersSessions() {
        Course firstCourse =
                databaseManager.addCourse(
                        "Data Structures",
                        "CSCI 3300");

        Course secondCourse =
                databaseManager.addCourse(
                        "Linear Algebra",
                        "MATH 2502");

        databaseManager.addStudySession(
                firstCourse.getId(),
                LocalDateTime.now(),
                60,
                "Reviewed trees");

        databaseManager.addStudySession(
                secondCourse.getId(),
                LocalDateTime.now(),
                45,
                "Reviewed matrices");

        List<StudySession> sessions =
                databaseManager.getStudySessionsByCourse(
                        firstCourse.getId());

        assertEquals(1, sessions.size());
        assertEquals(
                "Reviewed trees",
                sessions.get(0).getNotes());
    }

    @Test
    void deletingCourseCascadesRelatedData() {
        Course course = databaseManager.addCourse(
                "Data Structures",
                "CSCI 3300");

        databaseManager.addAssignment(
                course.getId(),
                "Homework",
                "",
                LocalDateTime.now().plusDays(1),
                Assignment.Priority.HIGH);

        databaseManager.addStudySession(
                course.getId(),
                LocalDateTime.now(),
                60,
                "Study session");

        databaseManager.deleteCourse(
                course.getId());

        assertTrue(
                databaseManager
                        .getAssignmentsByCourse(
                                course.getId())
                        .isEmpty());

        assertTrue(
                databaseManager
                        .getStudySessionsByCourse(
                                course.getId())
                        .isEmpty());
    }
}
