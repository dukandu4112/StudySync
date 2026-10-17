package com.studysync;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/** Real JavaFX handlers, dialogs, and SQLite persistence; enabled with -Pdesktop-qa. */
@EnabledIfSystemProperty(named = "desktop.tests", matches = "true")
class DesktopInteractionTest {
    @TempDir Path folder;
    private StudySyncService service;
    private DatabaseManager database;
    private Course course;
    private Stage stage;

    @BeforeAll static void toolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); ready.countDown(); });
        assertTrue(ready.await(15, TimeUnit.SECONDS));
    }
    @BeforeEach void setup() throws Exception {
        database = new DatabaseManager("jdbc:sqlite:" + folder.resolve("test.db"));
        service = new StudySyncService(database);
        course = service.createCourse("Mathematics", "MAT101");
        fx(() -> stage = new Stage());
    }
    @AfterEach void close() throws Exception { fx(() -> stage.close()); }
    @AfterAll static void shutdown() { Platform.exit(); }
    private static void fx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        try { task.get(20, TimeUnit.SECONDS); }
        catch (ExecutionException e) { throw new AssertionError("Desktop action failed", e.getCause()); }
    }
    private void show(Parent view) { stage.setScene(new Scene(view, 1000, 700)); stage.show(); view.applyCss(); view.layout(); }
    private static <T extends Node> T id(Parent view, String selector, Class<T> type) {
        return type.cast(java.util.Objects.requireNonNull(view.lookup("#" + selector), selector));
    }
    private static List<Node> nodes(Parent root) {
        java.util.ArrayList<Node> result = new java.util.ArrayList<>();
        for (Node node : root.getChildrenUnmodifiable()) {
            result.add(node);
            if (node instanceof Parent parent) result.addAll(nodes(parent));
        }
        return result;
    }
    private static Button button(Parent view, String text) {
        return nodes(view).stream().filter(Button.class::isInstance).map(Button.class::cast)
            .filter(b -> b.getText().equals(text)).findFirst().orElseThrow();
    }
    private static boolean hasText(Parent view, String text) {
        return nodes(view).stream().filter(Labeled.class::isInstance).map(Labeled.class::cast)
            .anyMatch(n -> n.getText() != null && n.getText().contains(text));
    }
    private static void answerDialog(ButtonType answer) {
        Platform.runLater(() -> {
            for (Window window : List.copyOf(Window.getWindows())) {
                if (window.getScene() != null && window.getScene().getRoot() instanceof DialogPane pane) {
                    ((Button)pane.lookupButton(answer)).fire(); return;
                }
            }
            fail("Expected a confirmation or validation dialog.");
        });
    }
    private void fillGrade(Parent view, String title, String earned) {
        @SuppressWarnings("unchecked") ComboBox<Course> selected = id(view, "grade-course", ComboBox.class);
        selected.setValue(course);
        id(view, "grade-title", TextField.class).setText(title);
        id(view, "grade-earned", TextField.class).setText(earned);
    }

    @Test void addAndEditGradeThroughButtons() throws Exception {
        fx(() -> {
            var view = new GradesView(service); show(view);
            fillGrade(view, "Midterm", "80"); id(view, "grade-save", Button.class).fire();
            assertEquals(80, service.getCourseGradePercentage(course.getId()).orElseThrow());
            assertTrue(hasText(view, "Midterm"));
            button(view, "Edit").fire(); id(view, "grade-earned", TextField.class).setText("95");
            id(view, "grade-save", Button.class).fire();
            assertEquals(1, service.getAcademicGrades().size());
            assertEquals(95, service.getCourseGradePercentage(course.getId()).orElseThrow());
        });
    }
    @Test void cancellingDeleteKeepsGradeAndConfirmingDeletesIt() throws Exception {
        service.saveAcademicGrade(new AcademicGrade(0, course.getId(), "Quiz", 9, 10, 1));
        fx(() -> {
            var view = new GradesView(service); show(view);
            answerDialog(ButtonType.CANCEL); button(view, "Delete").fire();
            assertEquals(1, service.getAcademicGrades().size());
            answerDialog(ButtonType.OK); button(view, "Delete").fire();
            assertTrue(service.getAcademicGrades().isEmpty());
            assertTrue(hasText(view, "No grades recorded"));
        });
    }
    @Test void invalidGradePreservesEnteredFormAndDatabase() throws Exception {
        fx(() -> {
            var view = new GradesView(service); show(view);
            fillGrade(view, "Keep my input", "101");
            answerDialog(ButtonType.OK); id(view, "grade-save", Button.class).fire();
            assertTrue(service.getAcademicGrades().isEmpty());
            assertEquals("Keep my input", id(view, "grade-title", TextField.class).getText());
            id(view, "grade-earned", TextField.class).setText("75"); id(view, "grade-save", Button.class).fire();
            assertEquals(1, service.getAcademicGrades().size());
        });
    }
    @Test void clearEditDoesNotChangeSavedGrade() throws Exception {
        service.saveAcademicGrade(new AcademicGrade(0, course.getId(), "Quiz", 9, 10, 1));
        fx(() -> {
            var view = new GradesView(service); show(view); button(view, "Edit").fire();
            id(view, "grade-title", TextField.class).setText("Unsaved"); button(view, "Clear / Cancel edit").fire();
            assertEquals("", id(view, "grade-title", TextField.class).getText());
            assertEquals("Quiz", service.getAcademicGrades().get(0).title());
        });
    }
    @Test void calendarCompletesAndReopensActualAssignment() throws Exception {
        LocalDate today = LocalDate.now();
        var assignment = service.createAssignment(course.getId(), "Today's task", "", today.atTime(12, 0), Assignment.Priority.HIGH);
        fx(() -> {
            var view = new CalendarView(service); show(view);
            id(view, "calendar-assignment-" + assignment.getId(), CheckBox.class).fire();
            assertTrue(service.getAssignments().get(0).isCompleted());
            id(view, "calendar-assignment-" + assignment.getId(), CheckBox.class).fire();
            assertFalse(service.getAssignments().get(0).isCompleted());
        });
    }
    @Test void changingMonthMovesAgendaIntoVisibleMonth() throws Exception {
        fx(() -> {
            var view = new CalendarView(service); show(view);
            id(view, "calendar-next", Button.class).fire();
            YearMonth next = YearMonth.now().plusMonths(1);
            assertTrue(hasText(view, "Agenda — " + next), "Agenda should belong to the displayed month");
            id(view, "calendar-today", Button.class).fire();
            assertTrue(hasText(view, "Agenda — " + LocalDate.now()));
        });
    }
    @Test void settingsButtonsPersistGoalAndReminderPreference() throws Exception {
        fx(() -> {
            var view = new DataSettingsView(database, service); show(view);
            nodes(view).stream().filter(TextField.class::isInstance).map(TextField.class::cast).findFirst().orElseThrow().setText("45");
            button(view, "Save daily goal (minutes)").fire();
            assertEquals(45, new StudySyncService(database).getDailyStudyTargetMinutes());
            nodes(view).stream().filter(CheckBox.class::isInstance).map(CheckBox.class::cast).findFirst().orElseThrow().fire();
            assertEquals("false", database.getSetting("reminders_enabled"));
        });
    }
    @Test void searchFiltersAndClearRestoresResults() throws Exception {
        service.createAssignment(course.getId(), "Geometry report", "proof", LocalDate.now().plusDays(5).atStartOfDay(), Assignment.Priority.MEDIUM);
        fx(() -> {
            var view = new FindView(service, database); show(view);
            TextField query = nodes(view).stream().filter(TextField.class::isInstance).map(TextField.class::cast).findFirst().orElseThrow();
            query.setText("geometry"); assertTrue(hasText(view, "Geometry report"));
            assertFalse(hasText(view, "Course · MAT101"));
            query.setText("no-such-work"); assertTrue(hasText(view, "No results"));
            button(view, "Clear").fire(); assertTrue(hasText(view, "Course · MAT101"));
        });
    }

    @Test void january31NavigationSelectsLeapDayAndReturnsToToday() throws Exception {
        fx(() -> {
            var clock = java.time.Clock.fixed(java.time.Instant.parse("2028-01-31T12:00:00Z"), java.time.ZoneOffset.UTC);
            var view = new CalendarView(service, clock); show(view);
            id(view, "calendar-next", Button.class).fire();
            assertTrue(hasText(view, "Agenda — 2028-02-29"));
            assertNotNull(view.lookup("#calendar-day-2028-02-29"));
            assertNull(view.lookup("#calendar-day-2028-02-30"));
            id(view, "calendar-today", Button.class).fire();
            assertTrue(hasText(view, "Agenda — 2028-01-31"));
        });
    }

    private Parent openApp(String page) {
        new StudySyncApplication(database).start(stage);
        Parent root = stage.getScene().getRoot();
        button(root, page).fire(); root.applyCss(); root.layout();
        return root;
    }
    private static <T extends Node> T field(Parent root, String label, Class<T> type) {
        return nodes(root).stream().filter(Label.class::isInstance).map(Label.class::cast)
            .filter(l -> l.getText().equals(label) && type.isInstance(l.getLabelFor()))
            .map(l -> type.cast(l.getLabelFor())).findFirst().orElseThrow(() -> new AssertionError("Missing field: " + label));
    }
    private static void dialog(java.util.function.Consumer<DialogPane> action, Runnable trigger) {
        var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        Platform.runLater(() -> {
            DialogPane pane = Window.getWindows().stream().filter(Window::isShowing)
                .map(Window::getScene).filter(java.util.Objects::nonNull).map(Scene::getRoot)
                .filter(DialogPane.class::isInstance).map(DialogPane.class::cast).reduce((a,b) -> b).orElse(null);
            try {
                assertNotNull(pane, "Expected a desktop dialog");
                action.accept(pane);
            } catch (Throwable t) {
                failure.set(t);
                if (pane != null) {
                    Node cancel = pane.lookupButton(ButtonType.CANCEL);
                    if (cancel == null) cancel = pane.lookupButton(ButtonType.OK);
                    if (cancel instanceof Button close) close.fire();
                }
            }
        });
        trigger.run();
        if (failure.get() != null) throw new AssertionError("Dialog interaction failed", failure.get());
    }
    private static void respond(DialogPane pane, ButtonType answer) { ((Button)pane.lookupButton(answer)).fire(); }

    @Test void courseCreateRenameAndCancelledEditPersistCorrectly() throws Exception {
        fx(() -> {
            Parent root = openApp("Courses");
            field(root, "Course Code", TextField.class).setText("CS101");
            field(root, "Course Name", TextField.class).setText("Computer Science");
            button(root, "Add Course").fire();
            assertEquals(2, service.getCourses().size());
            dialog(pane -> {
                field(pane, "Course Name", TextField.class).setText("Programming");
                respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertTrue(service.getCourses().stream().anyMatch(c -> c.getName().equals("Programming")));
            dialog(pane -> {
                field(pane, "Course Name", TextField.class).setText("Discard me"); respond(pane, ButtonType.CANCEL);
            }, () -> button(root, "Edit").fire());
            assertFalse(service.getCourses().stream().anyMatch(c -> c.getName().equals("Discard me")));
        });
    }
    @Test void duplicateCourseShowsErrorAndKeepsForm() throws Exception {
        fx(() -> {
            Parent root = openApp("Courses");
            field(root, "Course Code", TextField.class).setText("mat101");
            field(root, "Course Name", TextField.class).setText("Duplicate");
            dialog(pane -> respond(pane, ButtonType.OK), () -> button(root, "Add Course").fire());
            assertEquals(1, service.getCourses().size());
            assertEquals("Duplicate", field(root, "Course Name", TextField.class).getText());
        });
    }
    @Test void courseDeletionWarnsAboutGradesAndCascadesOnlyAfterConfirmation() throws Exception {
        service.createAssignment(course.getId(), "Task", "", LocalDate.now().plusDays(1).atStartOfDay(), Assignment.Priority.HIGH);
        service.recordStudySession(course.getId(), LocalDate.now().atTime(10,0), 20, "Notes");
        service.saveAcademicGrade(new AcademicGrade(0, course.getId(), "Exam", 90, 100, 1));
        fx(() -> {
            Parent root = openApp("Courses");
            dialog(pane -> respond(pane, ButtonType.CANCEL), () -> button(root, "Delete").fire());
            assertEquals(1, service.getCourses().size()); assertEquals(1, service.getAcademicGrades().size());
            dialog(pane -> {
                assertTrue(pane.getContentText().contains("grades"), "Deletion warning must mention assessment grades");
                respond(pane, ButtonType.OK);
            }, () -> button(root, "Delete").fire());
            assertTrue(service.getCourses().isEmpty()); assertTrue(service.getAssignments().isEmpty());
            assertTrue(service.getStudySessions().isEmpty()); assertTrue(service.getAcademicGrades().isEmpty());
        });
    }
    @Test void assignmentCreateEditCompleteReopenAndDeleteUseSameRecord() throws Exception {
        fx(() -> {
            Parent root = openApp("Assignments");
            field(root, "Title", TextField.class).setText("Project"); button(root, "Add Assignment").fire();
            int id = service.getAssignments().get(0).getId();
            dialog(pane -> {
                field(pane, "Title", TextField.class).setText("Revised project");
                field(pane, "Due Time", TextField.class).setText("14:30"); respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals(id, service.getAssignments().get(0).getId());
            assertEquals("Revised project", service.getAssignments().get(0).getTitle());
            assertEquals(java.time.LocalTime.of(14,30), service.getAssignments().get(0).getDueDate().toLocalTime());
            button(root, "Complete").fire(); assertTrue(service.getAssignments().get(0).isCompleted());
            button(root, "Reopen").fire(); assertFalse(service.getAssignments().get(0).isCompleted());
            dialog(pane -> respond(pane, ButtonType.CANCEL), () -> button(root, "Delete").fire());
            assertEquals(1, service.getAssignments().size());
            dialog(pane -> respond(pane, ButtonType.OK), () -> button(root, "Delete").fire());
            assertTrue(service.getAssignments().isEmpty());
        });
    }
    @Test void invalidAssignmentEditRemainsOpenAndCanBeCorrected() throws Exception {
        service.createAssignment(course.getId(), "Original", "", LocalDate.now().plusDays(1).atStartOfDay(), Assignment.Priority.MEDIUM);
        fx(() -> {
            Parent root = openApp("Assignments");
            dialog(pane -> {
                field(pane, "Title", TextField.class).setText("Corrected");
                field(pane, "Due Time", TextField.class).setText("25:70");
                dialog(error -> respond(error, ButtonType.OK), () -> respond(pane, ButtonType.OK));
                assertTrue(pane.getScene().getWindow().isShowing());
                assertEquals("Original", service.getAssignments().get(0).getTitle());
                assertEquals("Corrected", field(pane, "Title", TextField.class).getText());
                field(pane, "Due Time", TextField.class).setText("09:15"); respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals("Corrected", service.getAssignments().get(0).getTitle());
        });
    }
    @Test void typedAssignmentDateIsSavedInsteadOfOldPickerValue() throws Exception {
        fx(() -> {
            Parent root = openApp("Assignments");
            DatePicker date = field(root, "Due Date", DatePicker.class);
            LocalDate desired = LocalDate.now().plusDays(20);
            date.getEditor().setText(date.getConverter().toString(desired));
            field(root, "Title", TextField.class).setText("Typed date"); button(root, "Add Assignment").fire();
            assertEquals(desired, service.getAssignments().get(0).getDueDate().toLocalDate());
            dialog(pane -> {
                DatePicker editDate = field(pane, "Due Date", DatePicker.class);
                editDate.getEditor().setText(editDate.getConverter().toString(desired.plusDays(1)));
                respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals(desired.plusDays(1), service.getAssignments().get(0).getDueDate().toLocalDate());
        });
    }
    @Test void studySessionCreateEditCancelAndDeletePersistCorrectly() throws Exception {
        fx(() -> {
            Parent root = openApp("Study Sessions");
            field(root, "Minutes", TextField.class).setText("45");
            field(root, "Notes", TextField.class).setText("Chapter one"); button(root, "Record Session").fire();
            int id = service.getStudySessions().get(0).getId();
            dialog(pane -> {
                field(pane, "Minutes", TextField.class).setText("60");
                field(pane, "Notes", TextField.class).setText("Revised notes"); respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals(id, service.getStudySessions().get(0).getId()); assertEquals(60, service.getTotalStudyMinutes());
            dialog(pane -> {
                field(pane, "Minutes", TextField.class).setText("100"); respond(pane, ButtonType.CANCEL);
            }, () -> button(root, "Edit").fire());
            assertEquals(60, service.getTotalStudyMinutes());
            dialog(pane -> respond(pane, ButtonType.OK), () -> button(root, "Delete").fire());
            assertTrue(service.getStudySessions().isEmpty());
        });
    }
    @Test void invalidSessionDurationPreservesEditUntilCorrected() throws Exception {
        service.recordStudySession(course.getId(), LocalDate.now().atTime(10,0), 30, "Original");
        fx(() -> {
            Parent root = openApp("Study Sessions");
            dialog(pane -> {
                field(pane, "Minutes", TextField.class).setText("0");
                field(pane, "Notes", TextField.class).setText("Keep notes");
                dialog(error -> respond(error, ButtonType.OK), () -> respond(pane, ButtonType.OK));
                assertTrue(pane.getScene().getWindow().isShowing()); assertEquals(30, service.getTotalStudyMinutes());
                field(pane, "Minutes", TextField.class).setText("25"); respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals(25, service.getTotalStudyMinutes());
            assertEquals("Keep notes", service.getStudySessions().get(0).getNotes());
        });
    }

    @Test void invalidTypedDateDoesNotSilentlySavePreviousDate() throws Exception {
        fx(() -> {
            Parent root = openApp("Assignments");
            field(root, "Title", TextField.class).setText("Keep this title");
            field(root, "Due Date", DatePicker.class).getEditor().setText("not-a-date");
            dialog(error -> {
                assertTrue(error.getContentText().contains("valid date")); respond(error, ButtonType.OK);
            }, () -> button(root, "Add Assignment").fire());
            assertTrue(service.getAssignments().isEmpty());
            assertEquals("Keep this title", field(root, "Title", TextField.class).getText());
        });
    }
    @Test void typedStudyDatesAreCommittedOnCreateAndEdit() throws Exception {
        fx(() -> {
            Parent root = openApp("Study Sessions");
            LocalDate desired = LocalDate.now().minusDays(5);
            DatePicker date = field(root, "Date", DatePicker.class);
            date.getEditor().setText(date.getConverter().toString(desired));
            button(root, "Record Session").fire();
            assertEquals(desired, service.getStudySessions().get(0).getStartTime().toLocalDate());
            dialog(pane -> {
                DatePicker editDate = field(pane, "Date", DatePicker.class);
                editDate.getEditor().setText(editDate.getConverter().toString(desired.plusDays(1)));
                respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals(desired.plusDays(1), service.getStudySessions().get(0).getStartTime().toLocalDate());
        });
    }
    @Test void invalidCourseEditKeepsDialogOpenAndAcceptsCorrection() throws Exception {
        fx(() -> {
            Parent root = openApp("Courses");
            dialog(pane -> {
                field(pane, "Course Name", TextField.class).setText("");
                dialog(error -> respond(error, ButtonType.OK), () -> respond(pane, ButtonType.OK));
                assertTrue(pane.getScene().getWindow().isShowing());
                assertEquals("Mathematics", service.getCourses().get(0).getName());
                field(pane, "Course Name", TextField.class).setText("Algebra"); respond(pane, ButtonType.OK);
            }, () -> button(root, "Edit").fire());
            assertEquals("Algebra", service.getCourses().get(0).getName());
        });
    }
}
