package com.studysync;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** A focused daily workspace backed by the same persistent planning data as the other pages. */
public final class DashboardView {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d · h:mm a");
    public record Actions(Runnable courses, Runnable assignments, Runnable sessions, Runnable calendar) { }
    private DashboardView() { }

    public static VBox create(StudySyncService service) { return create(service, null); }
    public static VBox create(StudySyncService service, Runnable refresh) { return create(service, refresh, null); }

    public static VBox create(StudySyncService service, Runnable refresh, Actions actions) {
        if (service == null) throw new IllegalArgumentException("StudySync service cannot be null.");
        DashboardSummary summary = service.getDashboardSummary();
        DashboardAnalytics analytics = service.getDashboardAnalytics();
        VBox dashboard = new VBox(20);
        dashboard.setId("dashboard-workspace");
        VBox hero = new VBox(8,
            label(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), "hero-eyebrow"),
            label(summary.totalCourses() == 0 ? "Make room for your next big idea." : "A little focus. Real progress.", "hero-title"),
            label(summary.totalCourses() == 0
                ? "Start with a course, add your deadlines, and build a study plan that fits your day."
                : summary.pendingAssignments() + " pending · " + analytics.upcomingAssignments() + " due in the next 7 days · " + summary.overdueAssignments() + " overdue", "hero-description"));
        hero.getStyleClass().add("dashboard-hero");
        if (actions != null) {
            FlowPane quick = new FlowPane(10, 10);
            quick.getChildren().addAll(action("Add course", actions.courses(), "hero-button"),
                action("Add assignment", actions.assignments(), "hero-button"),
                action("Log study session", actions.sessions(), "hero-button"),
                action("Open calendar", actions.calendar(), "hero-button"));
            hero.getChildren().add(quick);
        }
        dashboard.getChildren().add(hero);
        if (summary.totalCourses() == 0) {
            VBox start = new VBox(12, label("Your workspace starts here", "section-title"),
                label("1  Add your courses", "assignment-title"),
                label("Keep each class and its weekly study goal in one place.", "assignment-meta"),
                label("2  Give your deadlines a home", "assignment-title"),
                label("Add assignments with a due date and priority. Your calendar and study plan update from them.", "assignment-meta"),
                label("3  Record a little progress", "assignment-title"),
                label("Log a study session to see your daily goal, weekly trend, and course progress grow.", "assignment-meta"));
            if (actions != null) start.getChildren().add(action("Create your first course", actions.courses(), "primary-button"));
            start.getStyleClass().add("card"); start.setId("dashboard-onboarding");
            dashboard.getChildren().add(start);
        } else {
            GridPane metrics = new GridPane(); metrics.setHgap(12); metrics.setVgap(12);
            metrics.add(metric("Courses", summary.totalCourses(), "Across your workspace"), 0, 0);
            metrics.add(metric("Pending", summary.pendingAssignments(), "Assignments to finish"), 1, 0);
            metrics.add(metric("Completed", summary.completedAssignments(), String.format("%.0f%% of all assignments", summary.completionPercentage())), 2, 0);
            for (int i = 0; i < 3; i++) { ColumnConstraints column = new ColumnConstraints(); column.setPercentWidth(100.0 / 3); column.setHgrow(Priority.ALWAYS); metrics.getColumnConstraints().add(column); }
            dashboard.getChildren().add(metrics);
        }
        VBox goal = card("Today's Study Goal", DailyStudyGoalView.create(service));
        VBox deadlines = card("Deadline queue", deadlineQueue(service, refresh, actions));
        GridPane focus = new GridPane(); focus.setHgap(16); focus.setVgap(16);
        for (int i = 0; i < 2; i++) { ColumnConstraints column = new ColumnConstraints(); column.setPercentWidth(50); column.setHgrow(Priority.ALWAYS); focus.getColumnConstraints().add(column); }
        focus.add(goal, 0, 0); focus.add(deadlines, 1, 0);
        goal.setMinWidth(0); deadlines.setMinWidth(0);
        // Stack at smaller desktop widths; do not shrink forms into unreadable columns.
        dashboard.widthProperty().addListener((observable, before, width) -> {
            boolean narrow = width.doubleValue() < 850;
            GridPane.setColumnIndex(deadlines, narrow ? 0 : 1); GridPane.setRowIndex(deadlines, narrow ? 1 : 0);
            GridPane.setColumnSpan(goal, narrow ? 2 : 1); GridPane.setColumnSpan(deadlines, narrow ? 2 : 1);
        });
        dashboard.getChildren().add(focus);
        if (summary.totalCourses() > 0) {
            dashboard.getChildren().add(card("Today's Study Plan", new VBox(14,
                StudyPlanSummaryView.create(service.getStudyPlanSummary()),
                SuggestedStudyPlanView.create(service.getSuggestedStudyPlan(), service, refresh))));
            dashboard.getChildren().add(card("Recommended Next", StudyRecommendationsView.create(service.getStudyRecommendations())));
            VBox details = new VBox(16,
                card("Course Study Goals", CourseStudyGoalsView.create(service)),
                card("This Week", StudyProgressView.create(service.getStudyProgressAnalytics())),
                card("Weekly Trend", StudyTrendView.create(service.getStudyTrend())),
                card("Study Streak", StudyStreakView.create(service.getStudyStreak())),
                card("What Needs Attention", AssignmentPlanView.create(service.getAssignmentPlan())),
                card("Planning Insights", new VBox(10,
                    label("High-priority pending: " + analytics.highPriorityPendingAssignments(), "assignment-title"),
                    label("Total study time: " + summary.totalStudyMinutes() + " minutes", "assignment-meta"),
                    label("Most studied course: " + (analytics.mostStudiedCourse() == null ? "No study sessions yet" : analytics.mostStudiedCourse().getCode() + " · " + analytics.mostStudiedCourseMinutes() + " minutes"), "assignment-meta"))));
            TitledPane insights = new TitledPane("Progress & course insights", details);
            insights.setExpanded(false); insights.setAnimated(false);
            dashboard.getChildren().add(insights);
        }
        dashboard.getChildren().add(label("Your workspace is stored on this computer. Use Data & Settings to create a backup.", "workspace-note"));
        return dashboard;
    }

    private static VBox deadlineQueue(StudySyncService service, Runnable refresh, Actions actions) {
        VBox queue = new VBox(12);
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            queue.getChildren().clear();
            var pending = service.getPendingAssignments();
            Map<Integer, String> codes = service.getCourses().stream().collect(Collectors.toMap(Course::getId, Course::getCode));
            if (pending.isEmpty()) queue.getChildren().add(label("No deadlines waiting. Add an assignment when you're ready.", "assignment-meta"));
            for (Assignment assignment : pending.stream().limit(5).toList()) {
                Label timing = label((assignment.isOverdue() ? "Overdue · " : "Due · ") + assignment.getDueDate().format(DATE_TIME), assignment.isOverdue() ? "deadline-overdue" : "assignment-meta");
                VBox row = new VBox(5, label(assignment.getTitle(), "assignment-title"),
                    label(codes.getOrDefault(assignment.getCourseId(), "Course") + " · " + assignment.getPriority(), "assignment-meta"), timing);
                Button done = action("Mark done", () -> { service.completeAssignment(assignment.getId()); if (refresh != null) refresh.run(); else render[0].run(); }, "secondary-button");
                done.setId("dashboard-complete-" + assignment.getId());
                done.setAccessibleText("Mark " + assignment.getTitle() + " complete");
                row.getChildren().add(done); row.getStyleClass().add("deadline-row"); queue.getChildren().add(row);
            }
            if (pending.size() > 5) queue.getChildren().add(label("Showing 5 of " + pending.size() + " pending assignments", "assignment-meta"));
            if (actions != null) queue.getChildren().add(action("Open assignments", actions.assignments(), "secondary-button"));
        };
        render[0].run(); return queue;
    }
    private static Button action(String text, Runnable handler, String style) {
        Button button = new Button(text); button.getStyleClass().add(style);
        button.setOnAction(event -> handler.run()); return button;
    }
    private static VBox metric(String title, Object value, String caption) {
        VBox box = new VBox(6, label(title, "metric-title"), label(String.valueOf(value), "metric-value"), label(caption, "assignment-meta"));
        box.getStyleClass().add("metric-card"); box.setMinWidth(0); return box;
    }
    private static VBox card(String title, Node content) {
        VBox box = new VBox(14, label(title, "section-title"), content); box.getStyleClass().add("card"); return box;
    }
    private static Label label(String text, String style) {
        Label label = new Label(text); label.getStyleClass().add(style); label.setWrapText(true); label.setMinWidth(0); return label;
    }
}
