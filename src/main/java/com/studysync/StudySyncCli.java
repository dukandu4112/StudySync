package com.studysync;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface for interacting with StudySync.
 */
public class StudySyncCli {

    private final StudySyncService service;
    private final Scanner scanner;

    public StudySyncCli(StudySyncService service, Scanner scanner) {
        if (service == null) {
            throw new IllegalArgumentException("StudySync service cannot be null.");
        }
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null.");
        }

        this.service = service;
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;

        printHeader();

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> showDashboard();
                    case "2" -> addCourse();
                    case "3" -> listCourses();
                    case "4" -> addAssignment();
                    case "5" -> listAssignments();
                    case "6" -> completeAssignment();
                    case "7" -> recordStudySession();
                    case "8" -> listStudySessions();
                    case "9" -> searchAssignments();
                    case "10" -> filterAssignmentsByPriority();
                    case "11" -> listPendingAssignments();
                    case "12" -> listCompletedAssignments();
                    case "13" -> listOverdueAssignments();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                System.out.println("Error: " + exception.getMessage());
            }

            System.out.println();
        }

        System.out.println("StudySync closed. Keep making progress!");
    }

    private void printHeader() {
        System.out.println("================================");
        System.out.println("          StudySync");
        System.out.println("================================");
        System.out.println("Student Productivity Manager");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("Main Menu");
        System.out.println("1. View dashboard");
        System.out.println("2. Add course");
        System.out.println("3. View courses");
        System.out.println("4. Add assignment");
        System.out.println("5. View assignments");
        System.out.println("6. Complete assignment");
        System.out.println("7. Record study session");
        System.out.println("8. View study sessions");
        System.out.println("9. Search assignments");
        System.out.println("10. Filter assignments by priority");
        System.out.println("11. View pending assignments");
        System.out.println("12. View completed assignments");
        System.out.println("13. View overdue assignments");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private void showDashboard() {
        DashboardSummary summary = service.getDashboardSummary();

        System.out.println("\n--- Dashboard ---");
        System.out.println("Courses: " + summary.totalCourses());
        System.out.println("Assignments: " + summary.totalAssignments());
        System.out.println("Pending: " + summary.pendingAssignments());
        System.out.println("Completed: " + summary.completedAssignments());
        System.out.println("Overdue: " + summary.overdueAssignments());
        System.out.println("Study time: " + summary.totalStudyMinutes() + " minutes");
        System.out.printf("Completion: %.1f%%%n", summary.completionPercentage());
    }

    private void addCourse() {
        System.out.print("Course name: ");
        String name = scanner.nextLine();
        System.out.print("Course code: ");
        String code = scanner.nextLine();

        Course course = service.createCourse(name, code);
        System.out.println("Added course: " + course);
    }

    private void listCourses() {
        List<Course> courses = service.getCourses();
        System.out.println("\n--- Courses ---");

        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }

        courses.forEach(course ->
                System.out.println(course.getId() + ". " + course));
    }

    private void addAssignment() {
        int courseId = readPositiveInt("Course ID: ");
        System.out.print("Assignment title: ");
        String title = scanner.nextLine();
        System.out.print("Description: ");
        String description = scanner.nextLine();
        LocalDateTime dueDate = readDateTime(
                "Due date/time (YYYY-MM-DDTHH:MM): ");
        Assignment.Priority priority = readPriority();

        Assignment assignment = service.createAssignment(
                courseId,
                title,
                description,
                dueDate,
                priority);

        System.out.println("Added assignment: " + assignment);
    }

    private void listAssignments() {
        printAssignments("Assignments", service.getAssignments());
    }

    private void searchAssignments() {
        System.out.print("Search assignments: ");
        String query = scanner.nextLine();
        printAssignments(
                "Search Results",
                service.searchAssignments(query));
    }

    private void filterAssignmentsByPriority() {
        Assignment.Priority priority = readPriority();
        printAssignments(
                priority + " Priority Assignments",
                service.getAssignmentsByPriority(priority));
    }

    private void listPendingAssignments() {
        printAssignments(
                "Pending Assignments",
                service.getPendingAssignments());
    }

    private void listCompletedAssignments() {
        printAssignments(
                "Completed Assignments",
                service.getCompletedAssignments());
    }

    private void listOverdueAssignments() {
        printAssignments(
                "Overdue Assignments",
                service.getOverdueAssignments());
    }

    private void printAssignments(
            String heading,
            List<Assignment> assignments) {

        System.out.println("\n--- " + heading + " ---");

        if (assignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        assignments.forEach(assignment ->
                System.out.println(assignment.getId() + ". " + assignment));
    }

    private void completeAssignment() {
        int assignmentId = readPositiveInt("Assignment ID: ");
        service.completeAssignment(assignmentId);
        System.out.println("Assignment marked completed.");
    }

    private void recordStudySession() {
        int courseId = readPositiveInt("Course ID: ");
        LocalDateTime startTime = readDateTime(
                "Start date/time (YYYY-MM-DDTHH:MM): ");
        int durationMinutes = readPositiveInt("Duration in minutes: ");
        System.out.print("Notes: ");
        String notes = scanner.nextLine();

        StudySession session = service.recordStudySession(
                courseId,
                startTime,
                durationMinutes,
                notes);

        System.out.println("Recorded: " + session);
    }

    private void listStudySessions() {
        List<StudySession> sessions = service.getStudySessions();
        System.out.println("\n--- Study Sessions ---");

        if (sessions.isEmpty()) {
            System.out.println("No study sessions found.");
            return;
        }

        sessions.forEach(session ->
                System.out.println(session.getId() + ". " + session));
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Display the common validation message below.
            }

            System.out.println("Please enter a positive whole number.");
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return LocalDateTime.parse(input);
            } catch (DateTimeParseException exception) {
                System.out.println(
                        "Invalid date/time. Example: 2026-09-23T14:30");
            }
        }
    }

    private Assignment.Priority readPriority() {
        while (true) {
            System.out.print("Priority (LOW, MEDIUM, HIGH): ");
            String input = scanner.nextLine().trim().toUpperCase();

            try {
                return Assignment.Priority.valueOf(input);
            } catch (IllegalArgumentException exception) {
                System.out.println("Please enter LOW, MEDIUM, or HIGH.");
            }
        }
    }
}
