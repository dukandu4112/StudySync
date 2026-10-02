package com.studysync;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** JavaFX presentation for a daily study-time goal. */
public final class DailyStudyGoalView {
    private DailyStudyGoalView() {}

    public static VBox create(DailyStudyGoal goal) {
        if (goal == null) throw new IllegalArgumentException("Daily study goal cannot be null.");

        Label progress = styledLabel(
                goal.studiedMinutes() + " / " + goal.targetMinutes() + " min",
                "metric-value");

        ProgressBar bar = new ProgressBar(goal.completionPercentage() / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);

        String statusText = goal.completed()
                ? "Goal complete"
                : goal.remainingMinutes() + " min remaining";
        Label status = styledLabel(statusText, "assignment-title");
        Label percent = styledLabel(
                String.format("%.0f%%", goal.completionPercentage()),
                "assignment-meta");

        HBox details = new HBox(12, status, percent);
        details.setAlignment(Pos.CENTER_LEFT);

        VBox view = new VBox(10, progress, bar, details);
        view.setFillWidth(true);
        return view;
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
