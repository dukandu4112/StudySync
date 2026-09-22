package com.studysync;

import java.time.format.DateTimeFormatter;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** JavaFX presentation component for study-streak analytics. */
public final class StudyStreakView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");

    private StudyStreakView() {}

    public static VBox create(StudyStreak streak) {
        if (streak == null) throw new IllegalArgumentException("Study streak cannot be null.");

        HBox metrics = new HBox(16,
                metric("Current Streak", streak.currentStreakDays() + " day" + plural(streak.currentStreakDays())),
                metric("Longest Streak", streak.longestStreakDays() + " day" + plural(streak.longestStreakDays())));
        metrics.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));

        Label status = new Label(statusMessage(streak));
        status.getStyleClass().add("assignment-title");
        status.setWrapText(true);

        Label lastStudy = new Label(streak.lastStudyDate() == null
                ? "No study sessions recorded yet."
                : "Last study day: " + streak.lastStudyDate().format(DATE_FORMAT));
        lastStudy.getStyleClass().add("assignment-meta");

        return new VBox(10, metrics, status, lastStudy);
    }

    static String statusMessage(StudyStreak streak) {
        if (streak.lastStudyDate() == null) return "Record a study session to start your first streak.";
        if (streak.studiedToday()) {
            if (streak.currentStreakDays() == 1) return "You studied today — your streak has started.";
            return "You studied today — keep your " + streak.currentStreakDays() + "-day streak going.";
        }
        if (streak.lastStudyDate().equals(streak.referenceDate().minusDays(1))) {
            return "Study today to continue your " + streak.currentStreakDays() + "-day streak.";
        }
        return "Your most recent study streak ended before today. Start a new one with your next session.";
    }

    private static VBox metric(String title, String value) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("assignment-meta");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("assignment-title");
        VBox box = new VBox(3, titleLabel, valueLabel);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private static String plural(int days) {
        return days == 1 ? "" : "s";
    }
}
