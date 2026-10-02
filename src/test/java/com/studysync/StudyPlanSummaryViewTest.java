package com.studysync;

import static org.junit.jupiter.api.Assertions.*;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

class StudyPlanSummaryViewTest {

    @Test
    void rejectsNullSummary() {
        assertThrows(IllegalArgumentException.class, () -> StudyPlanSummaryView.create(null));
    }

    @Test
    void showsCoveredPlanMessage() {
        VBox view = StudyPlanSummaryView.create(new StudyPlanSummary(90, 2, 120, 30, 0));

        assertTrue(text(view).contains("Studied Today"));
        assertTrue(text(view).contains("30 min"));
        assertTrue(text(view).contains("Planned"));
        assertTrue(text(view).contains("90 min"));
        assertTrue(text(view).contains("Plan Coverage"));
        assertTrue(text(view).contains("Covered"));
        assertTrue(text(view).contains("covers the rest of your daily study goal"));
    }

    @Test
    void showsUncoveredPlanMessage() {
        VBox view = StudyPlanSummaryView.create(new StudyPlanSummary(30, 1, 120, 45, 45));

        assertTrue(text(view).contains("Still Uncovered"));
        assertTrue(text(view).contains("45 min"));
        assertTrue(text(view).contains("Needs more time"));
        assertTrue(text(view).contains("leaves 45 minutes to schedule"));
    }

    @Test
    void showsCompletedGoalMessage() {
        VBox view = StudyPlanSummaryView.create(new StudyPlanSummary(0, 0, 120, 120, 0));

        assertTrue(text(view).contains("Today's study goal is complete."));
    }

    @Test
    void showsNoAvailableBlocksMessage() {
        VBox view = StudyPlanSummaryView.create(new StudyPlanSummary(0, 0, 120, 30, 90));

        assertTrue(text(view).contains("No study blocks are available yet"));
        assertTrue(text(view).contains("90 minutes"));
    }

    private String text(Node node) {
        StringBuilder result = new StringBuilder();
        collect(node, result);
        return result.toString();
    }

    private void collect(Node node, StringBuilder result) {
        if (node instanceof Label label) {
            result.append(label.getText()).append('\n');
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, result);
            }
        }
    }
}
