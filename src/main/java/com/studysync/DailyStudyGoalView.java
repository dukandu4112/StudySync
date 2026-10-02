package com.studysync;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** JavaFX presentation for a configurable daily study-time goal. */
public final class DailyStudyGoalView {
    private static final int DEFAULT_TARGET_MINUTES = 120;
    private static final int MIN_TARGET_MINUTES = 15;
    private static final int MAX_TARGET_MINUTES = 480;
    private static final int TARGET_STEP_MINUTES = 15;

    private DailyStudyGoalView() {}

    public static VBox create(StudySyncService service) {
        if (service == null) throw new IllegalArgumentException("StudySync service cannot be null.");

        Label progress = styledLabel("", "metric-value");
        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        Label status = styledLabel("", "assignment-title");
        Label percent = styledLabel("", "assignment-meta");

        Spinner<Integer> target = new Spinner<>(
                MIN_TARGET_MINUTES,
                MAX_TARGET_MINUTES,
                DEFAULT_TARGET_MINUTES,
                TARGET_STEP_MINUTES);
        target.setEditable(true);
        target.setPrefWidth(110);

        Label targetLabel = styledLabel("Daily target (minutes)", "assignment-meta");
        HBox targetControls = new HBox(10, targetLabel, target);
        targetControls.setAlignment(Pos.CENTER_LEFT);

        HBox details = new HBox(12, status, percent);
        details.setAlignment(Pos.CENTER_LEFT);

        Runnable refresh = () -> updateGoal(
                service.getDailyStudyGoal(target.getValue()),
                progress,
                bar,
                status,
                percent);

        target.valueProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        refresh.run();

        VBox view = new VBox(10, targetControls, progress, bar, details);
        view.setFillWidth(true);
        return view;
    }

    private static void updateGoal(
            DailyStudyGoal goal,
            Label progress,
            ProgressBar bar,
            Label status,
            Label percent) {
        progress.setText(goal.studiedMinutes() + " / " + goal.targetMinutes() + " min");
        bar.setProgress(goal.completionPercentage() / 100.0);
        status.setText(goal.completed()
                ? "Goal complete"
                : goal.remainingMinutes() + " min remaining");
        percent.setText(String.format("%.0f%%", goal.completionPercentage()));
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
