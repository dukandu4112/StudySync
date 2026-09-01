package com.studysync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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
}
