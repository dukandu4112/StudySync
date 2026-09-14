package com.studysync;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX desktop entry point for the StudySync 2.0 user interface.
 * The GUI reuses the existing service and persistence layers.
 */
public class StudySyncApplication extends Application {

    private StudySyncService service;
    private BorderPane root;
    private final VBox navigation = new VBox(8);

    @Override
    public void start(Stage stage) {
        DatabaseManager databaseManager = new DatabaseManager();
        service = new StudySyncService(databaseManager);

        root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        root.setLeft(createNavigation());
        showDashboard();

        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(
                getClass().getResource("/com/studysync/studysync.css")
                        .toExternalForm());

        stage.setTitle("StudySync");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {
        Label title = new Label("StudySync");
        title.getStyleClass().add("app-title");
        Label subtitle = new Label("Plan. Study. Progress.");
        subtitle.getStyleClass().add("app-subtitle");
        VBox branding = new VBox(3, title, subtitle);
        HBox header = new HBox(branding);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.getStyleClass().add("app-header");
        HBox.setHgrow(branding, Priority.ALWAYS);
        return header;
    }

    private VBox createNavigation() {
        navigation.getChildren().setAll(
                navigationButton("Dashboard", this::showDashboard),
                navigationButton("Courses", () -> showPlaceholder(
                        "Courses", "Manage your classes and course information.")),
                navigationButton("Assignments", () -> showPlaceholder(
                        "Assignments", "Manage deadlines, priorities, and completion status.")),
                navigationButton("Study Sessions", () -> showPlaceholder(
                        "Study Sessions", "Record and review focused study time.")),
                navigationButton("Workload", () -> showPlaceholder(
                        "Workload", "Plan upcoming assignments and academic workload.")));
        navigation.setPadding(new Insets(24, 14, 24, 14));
        navigation.setPrefWidth(205);
        navigation.getStyleClass().add("navigation");
        return navigation;
    }

    private Button navigationButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("navigation-button");
        button.setOnAction(event -> {
            setActiveNavigation(button);
            action.run();
        });
        if (navigation.getChildren().isEmpty()) {
            button.getStyleClass().add("navigation-button-active");
        }
        return button;
    }

    private void setActiveNavigation(Button activeButton) {
        navigation.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button -> button.getStyleClass()
                        .remove("navigation-button-active"));
        activeButton.getStyleClass().add("navigation-button-active");
    }

    private void showDashboard() {
        DashboardSummary summary = service.getDashboardSummary();
        GridPane metrics = new GridPane();
        metrics.setHgap(16);
        metrics.setVgap(16);
        metrics.add(createMetricCard("Courses", summary.totalCourses()), 0, 0);
        metrics.add(createMetricCard("Pending", summary.pendingAssignments()), 1, 0);
        metrics.add(createMetricCard("Completed", summary.completedAssignments()), 2, 0);
        metrics.add(createMetricCard("Overdue", summary.overdueAssignments()), 0, 1);
        metrics.add(createMetricCard("Study Minutes", summary.totalStudyMinutes()), 1, 1);
        metrics.add(createMetricCard("Completion",
                String.format("%.1f%%", summary.completionPercentage())), 2, 1);

        for (int column = 0; column < 3; column++) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(33.333);
            constraints.setHgrow(Priority.ALWAYS);
            metrics.getColumnConstraints().add(constraints);
        }

        VBox page = pageContainer(
                "Dashboard",
                "A quick view of your courses, assignments, and study progress.",
                metrics);
        setPage(page);
    }

    private void showPlaceholder(String title, String description) {
        Label message = new Label(
                title + " workspace is ready for its v2.0 feature screen.");
        message.getStyleClass().add("placeholder-message");
        VBox card = new VBox(message);
        card.setPadding(new Insets(24));
        card.getStyleClass().add("content-card");
        setPage(pageContainer(title, description, card));
    }

    private VBox pageContainer(String titleText, String descriptionText,
            Node content) {
        Label title = new Label(titleText);
        title.getStyleClass().add("section-title");
        Label description = new Label(descriptionText);
        description.getStyleClass().add("section-description");
        VBox page = new VBox(12, title, description, content);
        page.setPadding(new Insets(30));
        VBox.setMargin(content, new Insets(14, 0, 0, 0));
        return page;
    }

    private void setPage(Node page) {
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.getStyleClass().add("content-scroll");
        root.setCenter(scrollPane);
    }

    private VBox createMetricCard(String labelText, Object valueText) {
        Label value = new Label(String.valueOf(valueText));
        value.getStyleClass().add("metric-value");
        Label label = new Label(labelText);
        label.getStyleClass().add("metric-label");
        VBox card = new VBox(6, value, label);
        card.setPadding(new Insets(20));
        card.setMinHeight(120);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("metric-card");
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
