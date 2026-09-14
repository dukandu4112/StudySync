package com.studysync;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CourseManagementServiceTest {

    private Path databasePath;
    private StudySyncService service;

    @BeforeEach
    void setUp() throws Exception {
        databasePath = Files.createTempFile("studysync-course-management-", ".db");
        service = new StudySyncService(
                new DatabaseManager("jdbc:sqlite:" + databasePath));
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(databasePath);
    }

    @Test
    void updateCourseChangesStoredNameAndCode() {
        Course course = service.createCourse("Old Course", "CSCI 1000");

        assertTrue(service.updateCourse(
                course.getId(), "Software Engineering", "CSCI 4200"));

        Course updated = service.getCourses().get(0);
        assertEquals("Software Engineering", updated.getName());
        assertEquals("CSCI 4200", updated.getCode());
    }

    @Test
    void updateCourseRejectsMissingCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateCourse(999, "Missing", "CSCI 9999"));
    }

    @Test
    void updateCourseRejectsInvalidFields() {
        Course course = service.createCourse("Algorithms", "CSCI 3320");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateCourse(course.getId(), "   ", "CSCI 3320"));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateCourse(course.getId(), "Algorithms", "   "));
    }

    @Test
    void deleteCourseRemovesCourse() {
        Course course = service.createCourse("Temporary Course", "TEMP 1000");

        assertTrue(service.deleteCourse(course.getId()));
        assertTrue(service.getCourses().isEmpty());
    }

    @Test
    void deleteCourseCascadesAssignmentsAndStudySessions() {
        Course course = service.createCourse("Data Structures", "CSCI 3300");
        service.createAssignment(
                course.getId(), "Homework", "Trees",
                LocalDateTime.now().plusDays(1), Assignment.Priority.HIGH);
        service.recordStudySession(
                course.getId(), LocalDateTime.now(), 45, "Tree review");

        assertTrue(service.deleteCourse(course.getId()));
        assertTrue(service.getAssignments().isEmpty());
        assertTrue(service.getStudySessions().isEmpty());
    }

    @Test
    void deleteCourseRejectsMissingCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteCourse(999));
    }
}
