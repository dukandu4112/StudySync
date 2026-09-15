package com.studysync;

import java.time.format.DateTimeFormatter;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Builds the StudySync 2.2 dashboard from service-backed analytics and planning data. */
public final class DashboardView {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    private DashboardView() {}

    public static VBox create(StudySyncService service) {
        if (service == null) throw new IllegalArgumentException("StudySync service cannot be null.");

        DashboardSummary summary = service.getDashboardSummary();
        DashboardAnalytics analytics = service.getDashboardAnalytics();
        StudyProgressAnalytics progress = service.getStudyProgressAnalytics();

        GridPane metrics = new GridPane();
        metrics.setHgap(16);
        metrics.setVgap(16);
        metrics.add(metricCard("Courses", summary.totalCourses()), 0, 0);
        metrics.add(metricCard("Pending", summary.pendingAssignments()), 1, 0);
        metrics.add(metricCard("Completed", summary.completedAssignments()), 2, 0);
        metrics.add(metricCard("Overdue", summary.overdueAssignments()), 0, 1);
        metrics.add(metricCard("Study Minutes", summary.totalStudyMinutes()), 1, 1);
        metrics.add(metricCard("Completion", String.format("%.1f%%", summary.completionPercentage())), 2, 1);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(33.333);
            column.setHgrow(Priority.ALWAYS);
            metrics.getColumnConstraints().add(column);
        }

        String nearest = analytics.nearestDeadline() == null
                ? "No upcoming deadline"
                : analytics.nearestDeadline().getTitle() + " — " + analytics.nearestDeadline().getDueDate().format(DATE_TIME_FORMAT);
        String mostStudied = analytics.mostStudiedCourse() == null
                ? "No study sessions yet"
                : analytics.mostStudiedCourse().getCode() + " — " + analytics.mostStudiedCourseMinutes() + " minutes";
        VBox insights = new VBox(10,
                styledLabel("Next 7 days: " + analytics.upcomingAssignments() + " assignment" + (analytics.upcomingAssignments() == 1 ? "" : "s"), "assignment-title"),
                styledLabel("High-priority pending: " + analytics.highPriorityPendingAssignments(), "assignment-title"),
                styledLabel("Nearest deadline: " + nearest, "assignment-meta"),
                styledLabel("Most studied course: " + mostStudied, "assignment-meta"));

        return new VBox(18,
                metrics,
                card("This Week", StudyProgressView.create(progress)),
                card("What Needs Attention", AssignmentPlanView.create(service.getAssignmentPlan())),
                card("Planning Insights", insights));
    }

    private static VBox metricCard(String title, Object value) {
        VBox box = new VBox(8, styledLabel(title, "metric-title"), styledLabel(String.valueOf(value), "metric-value"));
        box.setStyle("-fx-padding: 18;");
        box.getStyleClass().add("metric-card");
        return box;
    }

    private static VBox card(String title, javafx.scene.Node content) {
        VBox box = new VBox(12, styledLabel(title, "section-title"), content);
        box.setStyle("-fx-padding: 18;");
        box.getStyleClass().add("card");
        return box;
    }

    private static Label styledLabel(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        return label;
    }
}
