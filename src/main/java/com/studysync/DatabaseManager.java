package com.studysync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
/**
 * Manages StudySync's SQLite database connection and schema.
 */
public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:studysync.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    private void initializeDatabase() {
        String createCoursesTable = """
                CREATE TABLE IF NOT EXISTS courses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    code TEXT NOT NULL UNIQUE COLLATE NOCASE
                )
                """;

        String createAssignmentsTable = """
                CREATE TABLE IF NOT EXISTS assignments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    due_date TEXT NOT NULL,
                    priority TEXT NOT NULL
                        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
                    completed INTEGER NOT NULL DEFAULT 0
                        CHECK (completed IN (0, 1)),
                    FOREIGN KEY (course_id)
                        REFERENCES courses(id)
                        ON DELETE CASCADE
                )
                """;

        String createStudySessionsTable = """
                CREATE TABLE IF NOT EXISTS study_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    course_id INTEGER NOT NULL,
                    start_time TEXT NOT NULL,
                    duration_minutes INTEGER NOT NULL
                        CHECK (duration_minutes > 0),
                    notes TEXT NOT NULL DEFAULT '',
                    FOREIGN KEY (course_id)
                        REFERENCES courses(id)
                        ON DELETE CASCADE
                )
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createCoursesTable);
            statement.execute(createAssignmentsTable);
            statement.execute(createStudySessionsTable);

        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Unable to initialize the StudySync database.",
                    exception);
        }
    }
    public Course addCourse(String name, String code) {
    Course course = new Course(name, code);

    String sql = """
            INSERT INTO courses (name, code)
            VALUES (?, ?)
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(
                         sql,
                         Statement.RETURN_GENERATED_KEYS)) {

        statement.setString(1, course.getName());
        statement.setString(2, course.getCode());
        statement.executeUpdate();

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return new Course(
                        keys.getInt(1),
                        course.getName(),
                        course.getCode());
            }
        }

        throw new IllegalStateException(
                "Course was created, but no ID was returned.");

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to add course.",
                exception);
    }
}

public List<Course> getAllCourses() {
    List<Course> courses = new ArrayList<>();

    String sql = """
            SELECT id, name, code
            FROM courses
            ORDER BY code
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql);
         ResultSet results = statement.executeQuery()) {

        while (results.next()) {
            courses.add(new Course(
                    results.getInt("id"),
                    results.getString("name"),
                    results.getString("code")));
        }

        return courses;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to retrieve courses.",
                exception);
    }
}

public Course findCourseById(int id) {
    String sql = """
            SELECT id, name, code
            FROM courses
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        try (ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                return new Course(
                        results.getInt("id"),
                        results.getString("name"),
                        results.getString("code"));
            }
        }

        return null;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to find course.",
                exception);
    }
}

public boolean updateCourse(
        int id,
        String name,
        String code) {

    Course course = new Course(id, name, code);

    String sql = """
            UPDATE courses
            SET name = ?, code = ?
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setString(1, course.getName());
        statement.setString(2, course.getCode());
        statement.setInt(3, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to update course.",
                exception);
    }
}

public boolean deleteCourse(int id) {
    String sql = """
            DELETE FROM courses
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to delete course.",
                exception);
    }
}
    public Assignment addAssignment(
        int courseId,
        String title,
        String description,
        LocalDateTime dueDate,
        Assignment.Priority priority) {

    Assignment assignment = new Assignment(
            courseId,
            title,
            description,
            dueDate,
            priority);

    String sql = """
            INSERT INTO assignments
                (course_id, title, description, due_date, priority, completed)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(
                         sql,
                         Statement.RETURN_GENERATED_KEYS)) {

        statement.setInt(1, assignment.getCourseId());
        statement.setString(2, assignment.getTitle());
        statement.setString(3, assignment.getDescription());
        statement.setString(4, assignment.getDueDate().toString());
        statement.setString(5, assignment.getPriority().name());
        statement.setInt(6, assignment.isCompleted() ? 1 : 0);

        statement.executeUpdate();

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return new Assignment(
                        keys.getInt(1),
                        assignment.getCourseId(),
                        assignment.getTitle(),
                        assignment.getDescription(),
                        assignment.getDueDate(),
                        assignment.getPriority(),
                        assignment.isCompleted());
            }
        }

        throw new IllegalStateException(
                "Assignment was created, but no ID was returned.");

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to add assignment.",
                exception);
    }
}

public List<Assignment> getAllAssignments() {
    List<Assignment> assignments = new ArrayList<>();

    String sql = """
            SELECT id, course_id, title, description,
                   due_date, priority, completed
            FROM assignments
            ORDER BY due_date
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet results = statement.executeQuery()) {

        while (results.next()) {
            assignments.add(mapAssignment(results));
        }

        return assignments;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to retrieve assignments.",
                exception);
    }
}

public List<Assignment> getAssignmentsByCourse(int courseId) {
    List<Assignment> assignments = new ArrayList<>();

    String sql = """
            SELECT id, course_id, title, description,
                   due_date, priority, completed
            FROM assignments
            WHERE course_id = ?
            ORDER BY due_date
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, courseId);

        try (ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                assignments.add(mapAssignment(results));
            }
        }

        return assignments;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to retrieve course assignments.",
                exception);
    }
}

public Assignment findAssignmentById(int id) {
    String sql = """
            SELECT id, course_id, title, description,
                   due_date, priority, completed
            FROM assignments
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        try (ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                return mapAssignment(results);
            }
        }

        return null;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to find assignment.",
                exception);
    }
}

public boolean updateAssignment(
        int id,
        int courseId,
        String title,
        String description,
        LocalDateTime dueDate,
        Assignment.Priority priority) {

    Assignment assignment = new Assignment(
            courseId,
            title,
            description,
            dueDate,
            priority);

    String sql = """
            UPDATE assignments
            SET course_id = ?,
                title = ?,
                description = ?,
                due_date = ?,
                priority = ?
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, assignment.getCourseId());
        statement.setString(2, assignment.getTitle());
        statement.setString(3, assignment.getDescription());
        statement.setString(4, assignment.getDueDate().toString());
        statement.setString(5, assignment.getPriority().name());
        statement.setInt(6, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to update assignment.",
                exception);
    }
}

public boolean setAssignmentCompleted(int id, boolean completed) {
    String sql = """
            UPDATE assignments
            SET completed = ?
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, completed ? 1 : 0);
        statement.setInt(2, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to update assignment completion status.",
                exception);
    }
}

public boolean deleteAssignment(int id) {
    String sql = """
            DELETE FROM assignments
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to delete assignment.",
                exception);
    }
}

private Assignment mapAssignment(ResultSet results)
        throws SQLException {

    return new Assignment(
            results.getInt("id"),
            results.getInt("course_id"),
            results.getString("title"),
            results.getString("description"),
            LocalDateTime.parse(results.getString("due_date")),
            Assignment.Priority.valueOf(
                    results.getString("priority")),
            results.getInt("completed") == 1);
}
    public StudySession addStudySession(
        int courseId,
        LocalDateTime startTime,
        int durationMinutes,
        String notes) {

    StudySession session = new StudySession(
            courseId,
            startTime,
            durationMinutes,
            notes);

    String sql = """
            INSERT INTO study_sessions
                (course_id, start_time, duration_minutes, notes)
            VALUES (?, ?, ?, ?)
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(
                         sql,
                         Statement.RETURN_GENERATED_KEYS)) {

        statement.setInt(1, session.getCourseId());
        statement.setString(2, session.getStartTime().toString());
        statement.setInt(3, session.getDurationMinutes());
        statement.setString(4, session.getNotes());

        statement.executeUpdate();

        try (ResultSet keys = statement.getGeneratedKeys()) {
            if (keys.next()) {
                return new StudySession(
                        keys.getInt(1),
                        session.getCourseId(),
                        session.getStartTime(),
                        session.getDurationMinutes(),
                        session.getNotes());
            }
        }

        throw new IllegalStateException(
                "Study session was created, but no ID was returned.");

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to add study session.",
                exception);
    }
}

public List<StudySession> getAllStudySessions() {
    List<StudySession> sessions = new ArrayList<>();

    String sql = """
            SELECT id, course_id, start_time,
                   duration_minutes, notes
            FROM study_sessions
            ORDER BY start_time DESC
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql);
         ResultSet results = statement.executeQuery()) {

        while (results.next()) {
            sessions.add(mapStudySession(results));
        }

        return sessions;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to retrieve study sessions.",
                exception);
    }
}

public List<StudySession> getStudySessionsByCourse(
        int courseId) {

    List<StudySession> sessions = new ArrayList<>();

    String sql = """
            SELECT id, course_id, start_time,
                   duration_minutes, notes
            FROM study_sessions
            WHERE course_id = ?
            ORDER BY start_time DESC
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, courseId);

        try (ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                sessions.add(mapStudySession(results));
            }
        }

        return sessions;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to retrieve course study sessions.",
                exception);
    }
}

public StudySession findStudySessionById(int id) {
    String sql = """
            SELECT id, course_id, start_time,
                   duration_minutes, notes
            FROM study_sessions
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        try (ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                return mapStudySession(results);
            }
        }

        return null;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to find study session.",
                exception);
    }
}

public boolean updateStudySession(
        int id,
        int courseId,
        LocalDateTime startTime,
        int durationMinutes,
        String notes) {

    StudySession session = new StudySession(
            courseId,
            startTime,
            durationMinutes,
            notes);

    String sql = """
            UPDATE study_sessions
            SET course_id = ?,
                start_time = ?,
                duration_minutes = ?,
                notes = ?
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, session.getCourseId());
        statement.setString(2, session.getStartTime().toString());
        statement.setInt(3, session.getDurationMinutes());
        statement.setString(4, session.getNotes());
        statement.setInt(5, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to update study session.",
                exception);
    }
}

public boolean deleteStudySession(int id) {
    String sql = """
            DELETE FROM study_sessions
            WHERE id = ?
            """;

    try (Connection connection = getConnection();
         PreparedStatement statement =
                 connection.prepareStatement(sql)) {

        statement.setInt(1, id);

        return statement.executeUpdate() > 0;

    } catch (SQLException exception) {
        throw new IllegalStateException(
                "Unable to delete study session.",
                exception);
    }
}

private StudySession mapStudySession(ResultSet results)
        throws SQLException {

    return new StudySession(
            results.getInt("id"),
            results.getInt("course_id"),
            LocalDateTime.parse(
                    results.getString("start_time")),
            results.getInt("duration_minutes"),
            results.getString("notes")); 
        }
    
}
