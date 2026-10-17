package com.studysync;

import java.time.LocalDate;
import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Month navigation and a date-specific agenda using existing assignment data. */
public final class CalendarView extends VBox {
    private YearMonth month;
    private LocalDate selected;
    private final Clock clock;
    private final StudySyncService service;

    public CalendarView(StudySyncService service) {
        this(service, Clock.systemDefaultZone());
    }

    CalendarView(StudySyncService service, Clock clock) {
        super(16);
        this.clock = java.util.Objects.requireNonNull(clock);
        this.service = java.util.Objects.requireNonNull(service);
        selected = LocalDate.now(clock);
        month = YearMonth.from(selected);
        setPadding(new Insets(12));
        render();
    }

    private void changeMonth(int direction) {
        month = month.plusMonths(direction);
        selected = month.atDay(Math.min(selected.getDayOfMonth(), month.lengthOfMonth()));
        render();
    }

    private void render() {
        getChildren().clear();
        Button previous = new Button("Previous month");
        Button next = new Button("Next month");
        Button today = new Button("Today");
        previous.setId("calendar-previous"); next.setId("calendar-next"); today.setId("calendar-today");
        previous.setOnAction(e -> changeMonth(-1));
        next.setOnAction(e -> changeMonth(1));
        today.setOnAction(e -> { selected = LocalDate.now(clock); month = YearMonth.from(selected); render(); });
        getChildren().add(new HBox(12, previous, new Label(month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))), next, today));
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        String[] names = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / 7);
            grid.getColumnConstraints().add(column);
            grid.add(new Label(names[i]), i, 0);
        }
        var assignments = service.getAssignments();
        int offset = month.atDay(1).getDayOfWeek().getValue() - 1;
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            long pending = assignments.stream().filter(a -> !a.isCompleted() && a.getDueDate().toLocalDate().equals(date)).count();
            Button button = new Button(day + (pending == 0 ? "" : "\n" + pending + " due"));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMinHeight(58);
            button.setId("calendar-day-" + date);
            button.setAccessibleText(date + ", " + pending + " pending assignments");
            if (date.equals(selected)) button.setStyle("-fx-border-color: #2563eb; -fx-border-width: 2;");
            button.setOnAction(e -> { selected = date; render(); });
            grid.add(button, (offset + day - 1) % 7, (offset + day - 1) / 7 + 1);
        }
        getChildren().addAll(grid, new Label("Agenda — " + selected));
        var agenda = assignments.stream().filter(a -> a.getDueDate().toLocalDate().equals(selected)).toList();
        if (agenda.isEmpty()) getChildren().add(new Label("No assignments due on this date."));
        for (Assignment assignment : agenda) {
            CheckBox done = new CheckBox(assignment.getDueDate().toLocalTime() + " · " + assignment.getTitle() + " · " + assignment.getPriority());
            done.setId("calendar-assignment-" + assignment.getId());
            done.setSelected(assignment.isCompleted());
            done.setOnAction(e -> {
                try {
                    if (done.isSelected()) service.completeAssignment(assignment.getId());
                    else service.reopenAssignment(assignment.getId());
                    render();
                } catch (RuntimeException ex) {
                    done.setId("calendar-assignment-" + assignment.getId());
            done.setSelected(assignment.isCompleted());
                    new Alert(Alert.AlertType.ERROR, "Could not update assignment. " + ex.getMessage()).showAndWait();
                }
            });
            getChildren().add(done);
        }
    }
}
