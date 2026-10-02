package com.studysync;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/** JavaFX desktop entry point for StudySync 2.3. */
public class StudySyncApplication extends Application {
    private StudySyncService service;
    private BorderPane root;
    private final VBox navigation = new VBox(8);
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    @Override
    public void start(Stage stage) {
        service = new StudySyncService(new DatabaseManager());
        root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(createHeader());
        root.setLeft(createNavigation());
        showDashboard();
        Scene scene = new Scene(root, 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/com/studysync/studysync.css").toExternalForm());
        stage.setTitle("StudySync");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private HBox createHeader() {
        VBox branding = new VBox(3, styledLabel("StudySync", "app-title"), styledLabel("Plan. Study. Progress.", "app-subtitle"));
        HBox header = new HBox(branding);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 28, 18, 28));
        header.getStyleClass().add("app-header");
        return header;
    }

    private VBox createNavigation() {
        Button dashboard = navigationButton("Dashboard", this::showDashboard);
        Button courses = navigationButton("Courses", this::showCourses);
        Button assignments = navigationButton("Assignments", this::showAssignments);
        Button sessions = navigationButton("Study Sessions", this::showStudySessions);
        Button workload = navigationButton("Workload", this::showWorkload);
        navigation.getChildren().setAll(dashboard, courses, assignments, sessions, workload);
        setActiveNavigation(dashboard);
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
        return button;
    }

    private void setActiveNavigation(Button active) {
        navigation.getChildren().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .forEach(button -> button.getStyleClass().remove("navigation-button-active"));
        active.getStyleClass().add("navigation-button-active");
    }

    private void showDashboard() {
        setPage(pageContainer(
                "Dashboard",
                "A quick view of your courses, assignments, study progress, and planning priorities.",
                DashboardView.create(service, this::showDashboard)));
    }

    private void showCourses() {
        TextField code = new TextField();
        TextField name = new TextField();
        code.setPromptText("e.g. CSCI 2302");
        name.setPromptText("e.g. Data Structures & Algorithms");
        Button add = primary("Add Course");
        GridPane form = formGrid();
        form.add(new Label("Course Code"), 0, 0);
        form.add(code, 1, 0);
        form.add(new Label("Course Name"), 0, 1);
        form.add(name, 1, 1);
        form.add(add, 1, 2);
        TextField search = new TextField();
        search.setPromptText("Search course code or name");
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary-button");
        HBox tools = new HBox(10, search, clear);
        HBox.setHgrow(search, Priority.ALWAYS);
        VBox list = new VBox(10);
        Runnable refresh = () -> refreshCourseList(list, search.getText());
        refresh.run();
        search.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        clear.setOnAction(event -> search.clear());
        add.setOnAction(event -> {
            if (code.getText().isBlank() || name.getText().isBlank()) {
                showError("Missing course information", "Enter both a course code and course name.");
                return;
            }
            try {
                service.createCourse(name.getText().trim(), code.getText().trim());
                name.clear();
                code.clear();
                refresh.run();
            } catch (RuntimeException exception) {
                showError("Could not add course", safeMessage(exception));
            }
        });
        setPage(pageContainer("Courses", "Add, edit, search, and organize your courses.",
                new VBox(18, card("Add a Course", form), card("Find Courses", tools), card("Your Courses", list))));
    }

    private void refreshCourseList(VBox list, String query) {
        List<Course> courses = UiSupport.filterCourses(service.getCourses(), query);
        list.getChildren().clear();
        if (courses.isEmpty()) {
            addEmpty(list, query == null || query.isBlank() ? "No courses yet. Add your first course above." : "No courses match your search.");
            return;
        }
        for (Course course : courses) {
            VBox details = new VBox(3, styledLabel(course.getCode(), "course-code"), styledLabel(course.getName(), "course-name"));
            HBox.setHgrow(details, Priority.ALWAYS);
            Button edit = new Button("Edit");
            Button delete = new Button("Delete");
            edit.getStyleClass().add("secondary-button");
            delete.getStyleClass().add("danger-button");
            edit.setOnAction(event -> {
                if (editCourse(course)) refreshCourseList(list, query);
            });
            delete.setOnAction(event -> deleteCourse(course, list, query));
            HBox row = new HBox(10, details, edit, delete);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14));
            row.getStyleClass().add("course-row");
            list.getChildren().add(row);
        }
    }

    private boolean editCourse(Course course) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("StudySync");
        dialog.setHeaderText("Edit course");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField code = new TextField(course.getCode());
        TextField name = new TextField(course.getName());
        GridPane form = formGrid();
        form.add(new Label("Course Code"), 0, 0);
        form.add(code, 1, 0);
        form.add(new Label("Course Name"), 0, 1);
        form.add(name, 1, 1);
        dialog.getDialogPane().setContent(form);
        boolean[] saved = {false};
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (code.getText().isBlank() || name.getText().isBlank()) {
                showError("Missing course information", "Enter both a course code and course name.");
                event.consume();
                return;
            }
            try {
                service.updateCourse(course.getId(), name.getText().trim(), code.getText().trim());
                saved[0] = true;
            } catch (RuntimeException exception) {
                showError("Could not edit course", safeMessage(exception));
                event.consume();
            }
        });
        dialog.showAndWait();
        return saved[0];
    }

    private void deleteCourse(Course course, VBox list, String query) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Deleting \"" + course.getCode() + " - " + course.getName() + "\" also permanently removes its assignments and study sessions.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Delete course and related data?");
        confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(result -> {
            try {
                service.deleteCourse(course.getId());
                refreshCourseList(list, query);
            } catch (RuntimeException exception) {
                showError("Could not delete course", safeMessage(exception));
            }
        });
    }

    private void showAssignments() {
        List<Course> courses = service.getCourses();
        if (courses.isEmpty()) {
            setPage(pageContainer("Assignments", "Manage deadlines, priorities, and completion status.",
                    card("Assignments", styledLabel("Add a course before creating assignments.", "placeholder-message"))));
            return;
        }
        ComboBox<Course> course = new ComboBox<>();
        course.getItems().addAll(courses);
        course.getSelectionModel().selectFirst();
        course.setMaxWidth(Double.MAX_VALUE);
        TextField title = new TextField();
        TextField description = new TextField();
        TextField time = new TextField("23:59");
        title.setPromptText("Assignment title");
        description.setPromptText("Description (optional)");
        DatePicker date = new DatePicker(LocalDate.now().plusDays(1));
        ComboBox<Assignment.Priority> priority = new ComboBox<>();
        priority.getItems().addAll(Assignment.Priority.values());
        priority.setValue(Assignment.Priority.MEDIUM);
        Button add = primary("Add Assignment");
        GridPane form = formGrid();
        String[] labels = {"Course", "Title", "Description", "Due Date", "Due Time", "Priority"};
        Node[] inputs = {course, title, description, date, time, priority};
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(inputs[i], 1, i);
        }
        form.add(add, 1, 6);
        TextField search = new TextField();
        search.setPromptText("Search title or description");
        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().addAll("All", "Pending", "Completed", "Overdue", "High Priority", "Medium Priority", "Low Priority");
        filter.setValue("All");
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary-button");
        HBox tools = new HBox(10, search, filter, clear);
        HBox.setHgrow(search, Priority.ALWAYS);
        VBox list = new VBox(10);
        Runnable refresh = () -> refreshAssignmentList(list, search.getText(), filter.getValue());
        refresh.run();
        search.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        filter.setOnAction(event -> refresh.run());
        clear.setOnAction(event -> {
            search.clear();
            filter.setValue("All");
        });
        add.setOnAction(event -> {
            if (title.getText().isBlank()) {
                showError("Missing assignment title", "Enter a title before adding the assignment.");
                return;
            }
            try {
                LocalDateTime due = UiSupport.parseDateTime(date.getValue(), time.getText());
                service.createAssignment(course.getValue().getId(), title.getText().trim(), description.getText().trim(), due, priority.getValue());
                title.clear();
                description.clear();
                refresh.run();
            } catch (RuntimeException exception) {
                showError("Could not add assignment", safeMessage(exception));
            }
        });
        setPage(pageContainer("Assignments", "Manage deadlines, priorities, and completion status.",
                new VBox(18, card("Add an Assignment", form), card("Find Assignments", tools), card("Your Assignments", list))));
    }

    private void refreshAssignmentList(VBox list, String query, String filter) {
        String status = "All";
        String priority = "All";
        if ("Pending".equals(filter) || "Completed".equals(filter) || "Overdue".equals(filter)) status = filter;
        else if ("High Priority".equals(filter)) priority = "HIGH";
        else if ("Medium Priority".equals(filter)) priority = "MEDIUM";
        else if ("Low Priority".equals(filter)) priority = "LOW";
        List<Assignment> items = UiSupport.filterAssignments(service.getAssignments(), query, status, priority);
        list.getChildren().clear();
        if (items.isEmpty()) {
            addEmpty(list, "No assignments match the current view.");
            return;
        }
        for (Assignment assignment : items) list.getChildren().add(assignmentRow(assignment, list, query, filter));
    }

    private HBox assignmentRow(Assignment assignment, VBox target, String query, String filter) {
        Course course = findCourse(assignment.getCourseId());
        String courseCode = course == null ? "Course #" + assignment.getCourseId() : course.getCode();
        Label title = styledLabel(assignment.getTitle(), "assignment-title");
        Label meta = styledLabel(courseCode + "  •  Due " + assignment.getDueDate().format(DATE_TIME_FORMAT) + "  •  "
                + assignment.getPriority() + "  •  " + (assignment.isCompleted() ? "Completed" : assignment.isOverdue() ? "Overdue" : "Pending"), "assignment-meta");
        Label description = styledLabel(assignment.getDescription().isBlank() ? "No description" : assignment.getDescription(), "course-name");
        description.setWrapText(true);
        VBox details = new VBox(4, title, meta, description);
        HBox.setHgrow(details, Priority.ALWAYS);
        Button edit = new Button("Edit");
        Button status = new Button(assignment.isCompleted() ? "Reopen" : "Complete");
        Button delete = new Button("Delete");
        edit.getStyleClass().add("secondary-button");
        status.getStyleClass().add("secondary-button");
        delete.getStyleClass().add("danger-button");
        edit.setOnAction(event -> {
            if (editAssignment(assignment)) refreshAssignmentList(target, query, filter);
        });
        status.setOnAction(event -> {
            try {
                if (assignment.isCompleted()) service.reopenAssignment(assignment.getId());
                else service.completeAssignment(assignment.getId());
                refreshAssignmentList(target, query, filter);
            } catch (RuntimeException exception) {
                showError("Could not update assignment", safeMessage(exception));
            }
        });
        delete.setOnAction(event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + assignment.getTitle() + "\"?", ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("Delete assignment");
            confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(result -> {
                try {
                    service.deleteAssignment(assignment.getId());
                    refreshAssignmentList(target, query, filter);
                } catch (RuntimeException exception) {
                    showError("Could not delete assignment", safeMessage(exception));
                }
            });
        });
        HBox row = new HBox(10, details, edit, status, delete);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14));
        row.getStyleClass().add("course-row");
        return row;
    }

    private boolean editAssignment(Assignment assignment) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("StudySync");
        dialog.setHeaderText("Edit assignment");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ComboBox<Course> course = new ComboBox<>();
        course.getItems().addAll(service.getCourses());
        course.getItems().stream().filter(item -> item.getId() == assignment.getCourseId()).findFirst().ifPresent(course::setValue);
        course.setMaxWidth(Double.MAX_VALUE);
        TextField title = new TextField(assignment.getTitle());
        TextField description = new TextField(assignment.getDescription());
        TextField time = new TextField(assignment.getDueDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        DatePicker date = new DatePicker(assignment.getDueDate().toLocalDate());
        ComboBox<Assignment.Priority> priority = new ComboBox<>();
        priority.getItems().addAll(Assignment.Priority.values());
        priority.setValue(assignment.getPriority());
        GridPane form = formGrid();
        String[] labels = {"Course", "Title", "Description", "Due Date", "Due Time", "Priority"};
        Node[] inputs = {course, title, description, date, time, priority};
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(inputs[i], 1, i);
        }
        dialog.getDialogPane().setContent(form);
        boolean[] saved = {false};
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (title.getText().isBlank() || course.getValue() == null) {
                showError("Missing assignment information", "Select a course and enter an assignment title.");
                event.consume();
                return;
            }
            try {
                LocalDateTime due = UiSupport.parseDateTime(date.getValue(), time.getText());
                service.updateAssignment(assignment.getId(), course.getValue().getId(), title.getText().trim(), description.getText().trim(), due, priority.getValue());
                saved[0] = true;
            } catch (RuntimeException exception) {
                showError("Could not edit assignment", safeMessage(exception));
                event.consume();
            }
        });
        dialog.showAndWait();
        return saved[0];
    }

    private void showStudySessions() {
        List<Course> courses = service.getCourses();
        if (courses.isEmpty()) {
            setPage(pageContainer("Study Sessions", "Track focused study time by course.",
                    card("Study Sessions", styledLabel("Add a course before recording study sessions.", "placeholder-message"))));
            return;
        }
        ComboBox<Course> course = new ComboBox<>();
        course.getItems().addAll(courses);
        course.getSelectionModel().selectFirst();
        course.setMaxWidth(Double.MAX_VALUE);
        DatePicker date = new DatePicker(LocalDate.now());
        TextField time = new TextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        TextField minutes = new TextField("30");
        TextField notes = new TextField();
        notes.setPromptText("What did you work on?");
        Button add = primary("Record Session");
        GridPane form = formGrid();
        String[] labels = {"Course", "Date", "Start Time", "Minutes", "Notes"};
        Node[] inputs = {course, date, time, minutes, notes};
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(inputs[i], 1, i);
        }
        form.add(add, 1, 5);
        TextField search = new TextField();
        search.setPromptText("Search notes or course");
        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().add("All Courses");
        courses.stream().map(Course::getCode).forEach(filter.getItems()::add);
        filter.setValue("All Courses");
        Button clear = new Button("Clear");
        clear.getStyleClass().add("secondary-button");
        HBox tools = new HBox(10, search, filter, clear);
        HBox.setHgrow(search, Priority.ALWAYS);
        VBox list = new VBox(10);
        Runnable refresh = () -> refreshStudySessionList(list, search.getText(), filter.getValue());
        refresh.run();
        search.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        filter.setOnAction(event -> refresh.run());
        clear.setOnAction(event -> {
            search.clear();
            filter.setValue("All Courses");
        });
        add.setOnAction(event -> {
            try {
                int duration = Integer.parseInt(minutes.getText().trim());
                LocalDateTime start = UiSupport.parseDateTime(date.getValue(), time.getText());
                service.recordStudySession(course.getValue().getId(), start, duration, notes.getText().trim());
                notes.clear();
                refresh.run();
            } catch (RuntimeException exception) {
                showError("Could not record study session", safeMessage(exception));
            }
        });
        setPage(pageContainer("Study Sessions", "Track focused study time by course.",
                new VBox(18, card("Record Study Time", form), card("Find Sessions", tools), card("Study History", list))));
    }

    private void refreshStudySessionList(VBox list, String query, String courseFilter) {
        List<StudySession> sessions = UiSupport.filterStudySessions(service.getStudySessions(), service.getCourses(), query, courseFilter);
        list.getChildren().clear();
        if (sessions.isEmpty()) {
            addEmpty(list, "No study sessions match the current view.");
            return;
        }
        for (StudySession session : sessions) {
            Course course = findCourse(session.getCourseId());
            String courseCode = course == null ? "Course #" + session.getCourseId() : course.getCode();
            Label title = styledLabel(courseCode + "  •  " + session.getDurationMinutes() + " minutes", "assignment-title");
            Label meta = styledLabel(session.getStartTime().format(DATE_TIME_FORMAT), "assignment-meta");
            Label notes = styledLabel(session.getNotes().isBlank() ? "No notes" : session.getNotes(), "course-name");
            notes.setWrapText(true);
            VBox details = new VBox(4, title, meta, notes);
            HBox.setHgrow(details, Priority.ALWAYS);
            Button edit = new Button("Edit");
            Button delete = new Button("Delete");
            edit.getStyleClass().add("secondary-button");
            delete.getStyleClass().add("danger-button");
            edit.setOnAction(event -> {
                if (editStudySession(session)) refreshStudySessionList(list, query, courseFilter);
            });
            delete.setOnAction(event -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this " + session.getDurationMinutes() + " minute study session?", ButtonType.OK, ButtonType.CANCEL);
                confirm.setHeaderText("Delete study session");
                confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(result -> {
                    try {
                        service.deleteStudySession(session.getId());
                        refreshStudySessionList(list, query, courseFilter);
                    } catch (RuntimeException exception) {
                        showError("Could not delete study session", safeMessage(exception));
                    }
                });
            });
            HBox row = new HBox(10, details, edit, delete);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(14));
            row.getStyleClass().add("course-row");
            list.getChildren().add(row);
        }
    }

    private boolean editStudySession(StudySession session) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("StudySync");
        dialog.setHeaderText("Edit study session");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        ComboBox<Course> course = new ComboBox<>();
        course.getItems().addAll(service.getCourses());
        course.getItems().stream().filter(item -> item.getId() == session.getCourseId()).findFirst().ifPresent(course::setValue);
        DatePicker date = new DatePicker(session.getStartTime().toLocalDate());
        TextField time = new TextField(session.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        TextField minutes = new TextField(String.valueOf(session.getDurationMinutes()));
        TextField notes = new TextField(session.getNotes());
        GridPane form = formGrid();
        String[] labels = {"Course", "Date", "Start Time", "Minutes", "Notes"};
        Node[] inputs = {course, date, time, minutes, notes};
        for (int i = 0; i < labels.length; i++) {
            form.add(new Label(labels[i]), 0, i);
            form.add(inputs[i], 1, i);
        }
        dialog.getDialogPane().setContent(form);
        boolean[] saved = {false};
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                int duration = Integer.parseInt(minutes.getText().trim());
                LocalDateTime start = UiSupport.parseDateTime(date.getValue(), time.getText());
                service.updateStudySession(session.getId(), course.getValue().getId(), start, duration, notes.getText().trim());
                saved[0] = true;
            } catch (RuntimeException exception) {
                showError("Could not edit study session", safeMessage(exception));
                event.consume();
            }
        });
        dialog.showAndWait();
        return saved[0];
    }

    private void showWorkload() {
        List<AssignmentPlanItem> plan = service.getAssignmentPlan();
        VBox content = new VBox(12);
        if (plan.isEmpty()) {
            addEmpty(content, "No pending assignments. Your workload is clear.");
        } else {
            for (AssignmentPlanItem item : plan) {
                Assignment assignment = item.assignment();
                Course course = findCourse(assignment.getCourseId());
                String courseCode = course == null ? "Course #" + assignment.getCourseId() : course.getCode();
                String timing = item.minutesUntilDue() < 0 ? Math.abs(item.minutesUntilDue()) + " min overdue" : item.minutesUntilDue() + " min remaining";
                VBox details = new VBox(4,
                        styledLabel(assignment.getTitle(), "assignment-title"),
                        styledLabel(courseCode + "  •  " + item.urgency() + "  •  " + assignment.getPriority(), "assignment-meta"),
                        styledLabel("Due " + assignment.getDueDate().format(DATE_TIME_FORMAT) + "  •  " + timing, "course-name"));
                details.setPadding(new Insets(14));
                details.getStyleClass().add("course-row");
                content.getChildren().add(details);
            }
        }
        setPage(pageContainer("Workload", "See pending work ordered by urgency, priority, and deadline.", card("Priority Queue", content)));
    }

    private Course findCourse(int courseId) {
        return service.getCourses().stream().filter(course -> course.getId() == courseId).findFirst().orElse(null);
    }

    private VBox pageContainer(String title, String subtitle, Node content) {
        VBox body = new VBox(18, styledLabel(title, "page-title"), styledLabel(subtitle, "page-subtitle"), content);
        body.setPadding(new Insets(28));
        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("page-scroll");
        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return wrapper;
    }

    private VBox card(String title, Node content) {
        VBox card = new VBox(12, styledLabel(title, "section-title"), content);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("card");
        return card;
    }

    private GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(110);
        ColumnConstraints inputColumn = new ColumnConstraints();
        inputColumn.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelColumn, inputColumn);
        return grid;
    }

    private Button primary(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private Label styledLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private void addEmpty(VBox target, String message) {
        target.getChildren().add(styledLabel(message, "placeholder-message"));
    }

    private void setPage(Node node) {
        root.setCenter(node);
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "An unexpected error occurred." : message;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
