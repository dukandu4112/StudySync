package com.studysync;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX desktop entry point for the StudySync 2.0 user interface.
 *
 * <p>The desktop layer intentionally reuses the existing service and
 * persistence layers so the CLI and GUI share the same application logic.</p>
 */
public class StudySyncApplication extends Application {

    private StudySyncService service;

    @Override
    public void start(Stage stage) {
        DatabaseManager databaseManager = new DatabaseManager();
        service = new StudySyncService(databaseManager);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        root.setCenter(createDashboard());

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(
                getClass().getResource("/com/studysync/studysync.css")
                        .toExternalForm());

        stage.setTitle("StudySync");
        stage.setMinWidth(820);
        stage.setMinHeight(560);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {
        Label title = new Label("StudySync");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Academic productivity dashboard");
        subtitle.getStyleClass().add("app-subtitle");

        VBox branding = new VBox(3, title, subtitle);

        HBox header = new HBox(branding);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(22, 28, 22, 28));
        header.getStyleClass().add("app-header");
        HBox.setHgrow(branding, Priority.ALWAYS);
        return header;
    }

    private VBox createDashboard() {
        DashboardSummary summary = service.getDashboardSummary();

        Label heading = new Label("Dashboard");
        heading.getStyleClass().add("section-title");

        Label description = new Label(
                "A quick view of your courses, assignments, and study progress.");
        description.getStyleClass().add("section-description");

        GridPane metrics = new GridPane();
        metrics.setHgap(16);
        metrics.setVgap(16);
        metrics.add(createMetricCard(
                "Courses", Integer.toString(summary.totalCourses())), 0, 0);
        metrics.add(createMetricCard(
                "Pending", Integer.toString(summary.pendingAssignments())), 1, 0);
        metrics.add(createMetricCard(
                "Completed", Integer.toString(summary.completedAssignments())), 2, 0);
        metrics.add(createMetricCard(
                "Overdue", Integer.toString(summary.overdueAssignments())), 0, 1);
        metrics.add(createMetricCard(
                "Study Minutes", Integer.toString(summary.totalStudyMinutes())), 1, 1);
        metrics.add(createMetricCard(
                "Completion", String.format("%.1f%%", summary.completionPercentage())),
                2, 1);

        for (int column = 0; column < 3; column++) {
            javafx.scene.layout.ColumnConstraints constraints =
                    new javafx.scene.layout.ColumnConstraints();
            constraints.setPercentWidth(33.333);
            constraints.setHgrow(Priority.ALWAYS);
            metrics.getColumnConstraints().add(constraints);
        }

        Label foundationNote = new Label(
                "JavaFX foundation active — upcoming screens will use the existing "
                        + "StudySync service and SQLite data.");
        foundationNote.getStyleClass().add("foundation-note");

        VBox content = new VBox(12, heading, description, metrics, foundationNote);
        content.setPadding(new Insets(30));
        VBox.setMargin(metrics, new Insets(14, 0, 8, 0));
        return content;
    }

    private VBox createMetricCard(String labelText, String valueText) {
        Label value = new Label(valueText);
        value.getStyleClass().add("metric-value");

        Label label = new Label(labelText);
        label.getStyleClass().add("metric-label");

        VBox card = new VBox(6, value, label);
        card.setPadding(new Insets(20));
        card.setMinHeight(120);
        card.getStyleClass().add("metric-card");
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
