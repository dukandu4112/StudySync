package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StudyPlanItemTest {

    private final Course course = new Course(1, "Data Structures", "CSCI 2305");

    @Test
    void highPriorityStartsAtEighty() {
        assertEquals("High priority", item(30, 80).priorityLabel());
        assertEquals("High priority", item(30, 100).priorityLabel());
    }

    @Test
    void mediumPriorityRunsFromFortyThroughSeventyNine() {
        assertEquals("Medium priority", item(30, 40).priorityLabel());
        assertEquals("Medium priority", item(30, 79).priorityLabel());
    }

    @Test
    void lowPriorityIsBelowForty() {
        assertEquals("Low priority", item(30, 0).priorityLabel());
        assertEquals("Low priority", item(30, 39).priorityLabel());
    }

    @Test
    void focusGuidanceUsesFocusedSprintThroughThirtyMinutes() {
        assertEquals("Focused sprint — work on the highest-priority task for this course.",
                item(15, 50).focusGuidance());
        assertEquals("Focused sprint — work on the highest-priority task for this course.",
                item(30, 50).focusGuidance());
    }

    @Test
    void focusGuidanceUsesDeepStudyFromThirtyOneThroughSixtyMinutes() {
        assertEquals("Deep study block — focus on one major task and avoid switching topics.",
                item(31, 50).focusGuidance());
        assertEquals("Deep study block — focus on one major task and avoid switching topics.",
                item(60, 50).focusGuidance());
    }

    @Test
    void focusGuidanceUsesExtendedStudyAboveSixtyMinutes() {
        assertEquals("Extended study block — split the time into focused segments with a short break.",
                item(61, 50).focusGuidance());
        assertEquals("Extended study block — split the time into focused segments with a short break.",
                item(120, 50).focusGuidance());
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

    private StudyPlanItem item(int minutes, int score) {
        return new StudyPlanItem(course, minutes, "Review upcoming work", score);
    }
}
