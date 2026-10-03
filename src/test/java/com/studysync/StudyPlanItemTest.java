package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StudyPlanItemTest {

    private final Course course = new Course(1, "Data Structures", "CSCI 2305");

    @Test
    void highPriorityStartsAtEighty() {
        assertEquals("High priority", item(80).priorityLabel());
        assertEquals("High priority", item(100).priorityLabel());
    }

    @Test
    void mediumPriorityRunsFromFortyThroughSeventyNine() {
        assertEquals("Medium priority", item(40).priorityLabel());
        assertEquals("Medium priority", item(79).priorityLabel());
    }

    @Test
    void lowPriorityIsBelowForty() {
        assertEquals("Low priority", item(0).priorityLabel());
        assertEquals("Low priority", item(39).priorityLabel());
    }

    @Test
    void rejectsInvalidPlanItems() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanItem(null, 30, "Review", 50));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanItem(course, 0, "Review", 50));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanItem(course, 30, "", 50));
        assertThrows(IllegalArgumentException.class,
                () -> new StudyPlanItem(course, 30, "Review", -1));
    }

    private StudyPlanItem item(int score) {
        return new StudyPlanItem(course, 30, "Review upcoming work", score);
    }
}
