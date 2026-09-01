package com.studysync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
}
