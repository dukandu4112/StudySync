package com.studysync;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void constructorCreatesValidCourse() {
        Course course = new Course(
                "Data Structures and Algorithms",
                "csci 3300");

        assertEquals(
                "Data Structures and Algorithms",
                course.getName());

        assertEquals("CSCI 3300", course.getCode());
    }

    @Test
    void persistedCourseStoresId() {
        Course course = new Course(
                1,
                "Computer Organization and Architecture",
                "CSCI 3212");

        assertEquals(1, course.getId());
        assertEquals(
                "Computer Organization and Architecture",
                course.getName());
        assertEquals("CSCI 3212", course.getCode());
    }

    @Test
    void courseCodeIsConvertedToUppercase() {
        Course course = new Course(
                "Introductory Linear Algebra",
                "math 2502");

        assertEquals("MATH 2502", course.getCode());
    }

    @Test
    void courseNameIsTrimmed() {
        Course course = new Course(
                "  Data Structures  ",
                "CSCI 3300");

        assertEquals(
                "Data Structures",
                course.getName());
    }

    @Test
    void blankCourseNameThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Course("", "CSCI 3300"));
    }

    @Test
    void blankCourseCodeThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Course(
                        "Data Structures",
                        ""));
    }

    @Test
    void nullCourseNameThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Course(null, "CSCI 3300"));
    }

    @Test
    void nullCourseCodeThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Course(
                        "Data Structures",
                        null));
    }

    @Test
    void toStringReturnsReadableCourse() {
        Course course = new Course(
                "Data Structures",
                "CSCI 3300");

        assertEquals(
                "CSCI 3300 - Data Structures",
                course.toString());
    }

    @Test
    void equalCoursesAreEqual() {
        Course first = new Course(
                1,
                "Data Structures",
                "CSCI 3300");

        Course second = new Course(
                1,
                "Data Structures",
                "CSCI 3300");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
