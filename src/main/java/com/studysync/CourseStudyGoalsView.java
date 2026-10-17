package com.studysync;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** JavaFX presentation for configurable weekly study targets by course. */
public final class CourseStudyGoalsView {
    private static final int MIN_TARGET_MINUTES = 30;
    private static final int MAX_TARGET_MINUTES = 1200;
    private static final int TARGET_STEP_MINUTES = 30;

    private CourseStudyGoalsView() {}

    public static VBox create(StudySyncService service) {
        if (service == null) throw new IllegalArgumentException("StudySync service cannot be null.");

        VBox list = new VBox(14);
        if (service.getCourses().isEmpty()) {
            list.getChildren().add(styledLabel("Add a course to create a weekly study goal.", "assignment-meta"));
            return list;
        }

        for (Course course : service.getCourses()) {
            list.getChildren().add(courseGoalCard(service, course));
        }
        return list;
    }

    private static VBox courseGoalCard(StudySyncService service, Course course) {
        Label progress = styledLabel("", "assignment-title");
        Label status = styledLabel("", "assignment-meta");
        Label percent = styledLabel("", "assignment-meta");
        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);

        int saved = Math.max(MIN_TARGET_MINUTES,
                Math.min(MAX_TARGET_MINUTES, service.getCourseStudyTargetMinutes(course.getId())));
        Spinner<Integer> target = new Spinner<>(MIN_TARGET_MINUTES, MAX_TARGET_MINUTES, saved, TARGET_STEP_MINUTES);
        target.setEditable(true);
        target.setPrefWidth(110);

        Label courseName = styledLabel(course.getCode() + " — " + course.getName(), "assignment-title");
        HBox controls = new HBox(10,
                styledLabel("Weekly target (minutes)", "assignment-meta"), target);
        controls.setAlignment(Pos.CENTER_LEFT);
        HBox details = new HBox(12, status, percent);
        details.setAlignment(Pos.CENTER_LEFT);

        Runnable refresh = () -> update(service.getCourseStudyGoal(course.getId()), progress, bar, status, percent);
        target.valueProperty().addListener((observable, oldValue, newValue) -> {
            service.setCourseStudyTargetMinutes(course.getId(), newValue);
            refresh.run();
        });
        refresh.run();

        VBox card = new VBox(8, courseName, controls, progress, bar, details);
        card.setStyle("-fx-padding: 12;");
        card.getStyleClass().add("metric-card");
        return card;
    }

    private static void update(CourseStudyGoal goal, Label progress, ProgressBar bar, Label status, Label percent) {
        progress.setText(goal.studiedMinutes() + " / " + goal.targetMinutes() + " min this week");
        bar.setProgress(goal.completionPercentage() / 100.0);
        status.setText(goal.completed() ? "Weekly goal complete" : goal.remainingMinutes() + " min remaining");
        percent.setText(String.format("%.0f%%", goal.completionPercentage()));
    }

    private static Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
