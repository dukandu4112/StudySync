package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StudyPlanItemTest {

    private final Course course = new Course(1, "Data Structures", "CSCI 2305");

    @Test void highPriorityStartsAtEighty() { assertEquals("High priority", item(30,80).priorityLabel()); assertEquals("High priority", item(30,100).priorityLabel()); }
    @Test void mediumPriorityRunsFromFortyThroughSeventyNine() { assertEquals("Medium priority", item(30,40).priorityLabel()); assertEquals("Medium priority", item(30,79).priorityLabel()); }
    @Test void lowPriorityIsBelowForty() { assertEquals("Low priority", item(30,0).priorityLabel()); assertEquals("Low priority", item(30,39).priorityLabel()); }

    @Test void focusGuidanceUsesFocusedSprintThroughThirtyMinutes() { assertEquals("Focused sprint — work on the highest-priority task for this course.", item(15,50).focusGuidance()); assertEquals("Focused sprint — work on the highest-priority task for this course.", item(30,50).focusGuidance()); }
    @Test void focusGuidanceUsesDeepStudyFromThirtyOneThroughSixtyMinutes() { assertEquals("Deep study block — focus on one major task and avoid switching topics.", item(31,50).focusGuidance()); assertEquals("Deep study block — focus on one major task and avoid switching topics.", item(60,50).focusGuidance()); }
    @Test void focusGuidanceUsesExtendedStudyAboveSixtyMinutes() { assertEquals("Extended study block — split the time into focused segments with a short break.", item(61,50).focusGuidance()); assertEquals("Extended study block — split the time into focused segments with a short break.", item(120,50).focusGuidance()); }

    @Test void breakGuidanceIsOptionalThroughThirtyMinutes() { assertEquals("Break: optional 5-minute reset after the block.", item(15,50).breakGuidance()); assertEquals("Break: optional 5-minute reset after the block.", item(30,50).breakGuidance()); }
    @Test void breakGuidanceUsesPostBlockBreakFromThirtyOneThroughSixtyMinutes() { assertEquals("Break: take 5–10 minutes after the block.", item(31,50).breakGuidance()); assertEquals("Break: take 5–10 minutes after the block.", item(60,50).breakGuidance()); }
    @Test void breakGuidanceUsesMidpointBreakAboveSixtyMinutes() { assertEquals("Break: take 5–10 minutes near the midpoint, then reset before continuing.", item(61,50).breakGuidance()); assertEquals("Break: take 5–10 minutes near the midpoint, then reset before continuing.", item(120,50).breakGuidance()); }

    @Test void effortLabelUsesLightFocusOnlyForShortLowUrgencyBlocks() { assertEquals("Light focus", item(30,0).effortLabel()); assertEquals("Light focus", item(30,39).effortLabel()); }
    @Test void effortLabelUsesModerateFocusForMediumDurationOrUrgency() { assertEquals("Moderate focus", item(31,0).effortLabel()); assertEquals("Moderate focus", item(60,79).effortLabel()); assertEquals("Moderate focus", item(30,40).effortLabel()); }
    @Test void effortLabelUsesHeavyFocusForLongOrHighUrgencyBlocks() { assertEquals("Heavy focus", item(61,0).effortLabel()); assertEquals("Heavy focus", item(30,80).effortLabel()); assertEquals("Heavy focus", item(120,100).effortLabel()); }

    @Test
    void actionCueStartsHighPriorityWorkNext() {
        assertEquals("Start next — protect this block from interruptions.", item(30,80).actionCue());
        assertEquals("Start next — protect this block from interruptions.", item(30,100).actionCue());
    }

    @Test
    void actionCueSchedulesMediumPriorityWorkToday() {
        assertEquals("Schedule today — complete it after higher-priority work.", item(30,40).actionCue());
        assertEquals("Schedule today — complete it after higher-priority work.", item(30,79).actionCue());
    }

    @Test
    void actionCueFitsLowPriorityWorkAroundHigherPriorityBlocks() {
        assertEquals("Fit in when available — use it to maintain course momentum.", item(30,0).actionCue());
        assertEquals("Fit in when available — use it to maintain course momentum.", item(30,39).actionCue());
    }

    @Test
    void planLabelSummarizesCourseDurationPriorityAndEffort() {
        assertEquals("CSCI 2305 — 30 min — High priority — Heavy focus", item(30, 80).planLabel());
        assertEquals("CSCI 2305 — 45 min — Medium priority — Moderate focus", item(45, 50).planLabel());
        assertEquals("CSCI 2305 — 20 min — Low priority — Light focus", item(20, 20).planLabel());
    }

    @Test void rejectsInvalidPlanItems() { assertThrows(IllegalArgumentException.class,()->new StudyPlanItem(null,30,"Review",50)); assertThrows(IllegalArgumentException.class,()->new StudyPlanItem(course,0,"Review",50)); assertThrows(IllegalArgumentException.class,()->new StudyPlanItem(course,30,"",50)); assertThrows(IllegalArgumentException.class,()->new StudyPlanItem(course,30,"Review",-1)); }

    private StudyPlanItem item(int minutes, int score) { return new StudyPlanItem(course, minutes, "Review upcoming work", score); }
}
