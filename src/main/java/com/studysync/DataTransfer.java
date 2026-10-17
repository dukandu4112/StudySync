package com.studysync;

import java.nio.file.*;
import java.sql.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Local SQLite snapshots and transactional restore; imports never execute supplied SQL. */
public final class DataTransfer {
    private static final List<String> TABLES = List.of("courses", "assignments", "study_sessions", "app_settings", "academic_grades");
    private final DatabaseManager database;
    public DataTransfer(DatabaseManager database) { this.database = database; }

    public void backup(Path target) throws SQLException, IOException {
        Path path = target.toAbsolutePath().normalize();
        if (Files.exists(path)) throw new IOException("Choose a new filename; existing files are never overwritten.");
        try (Connection c = database.getConnection(); PreparedStatement s = c.prepareStatement("VACUUM INTO ?")) {
            s.setString(1, path.toString()); s.execute();
        }
    }

    public void restore(Path source) throws SQLException, IOException {
        Path path = source.toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) throw new IOException("Select an existing StudySync database backup.");
        try (Connection active = database.getConnection(); Statement statement = active.createStatement();
             ResultSet databases = statement.executeQuery("PRAGMA database_list")) {
            while (databases.next()) {
                String file = databases.getString("file");
                if ("main".equals(databases.getString("name")) && file != null && !file.isBlank()
                        && Files.isSameFile(path, Path.of(file)))
                    throw new IOException("This file is the active database. Select a separate backup to restore.");
            }
        }
        boolean hasSettings = false;
        boolean hasGrades = false;
        // Validate a read-only connection before touching the live database.
        try (Connection input = DriverManager.getConnection("jdbc:sqlite:" + path.toUri() + "?mode=ro"); Statement s = input.createStatement()) {
            try (ResultSet result = s.executeQuery("PRAGMA quick_check")) {
                if (!result.next() || !"ok".equals(result.getString(1))) throw new SQLException("Backup integrity check failed.");
            }
            int version;
            try (ResultSet r = s.executeQuery("PRAGMA user_version")) { r.next(); version = r.getInt(1); }
            if (version < 0 || version > DatabaseManager.SCHEMA_VERSION)
                throw new IOException("This backup requires a newer StudySync version.");
            for (String table : TABLES) {
                boolean exists;
                try (PreparedStatement lookup = input.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
                    lookup.setString(1, table);
                    try (ResultSet r = lookup.executeQuery()) { exists = r.next(); }
                }
                if (table.equals("app_settings")) hasSettings = exists;
                if (table.equals("academic_grades")) hasGrades = exists;
                if (!exists && version == 0 && (table.equals("app_settings") || table.equals("academic_grades"))) continue;
                try (ResultSet rows = s.executeQuery("SELECT * FROM " + table + " LIMIT 0")) {
                    if (rows.getMetaData().getColumnCount() == 0) throw new SQLException("Invalid backup table: " + table);
                }
            }
            try (ResultSet r = s.executeQuery("SELECT name FROM sqlite_master WHERE type='view' AND name IN ('courses','assignments','study_sessions','app_settings','academic_grades')")) {
                if (r.next()) throw new SQLException("Backup must contain ordinary StudySync tables.");
            }
        }
        try (Connection c = database.getConnection(); PreparedStatement attach = c.prepareStatement("ATTACH DATABASE ? AS imported")) {
            attach.setString(1, path.toUri().toString() + "?mode=ro"); attach.execute();
            c.setAutoCommit(false);
            try (Statement s = c.createStatement()) {
                s.executeUpdate("DELETE FROM academic_grades"); s.executeUpdate("DELETE FROM assignments");
                s.executeUpdate("DELETE FROM study_sessions"); s.executeUpdate("DELETE FROM courses"); s.executeUpdate("DELETE FROM app_settings");
                s.executeUpdate("INSERT INTO courses(id,name,code) SELECT id,name,code FROM imported.courses");
                s.executeUpdate("INSERT INTO assignments(id,course_id,title,description,due_date,priority,completed) SELECT id,course_id,title,description,due_date,priority,completed FROM imported.assignments");
                s.executeUpdate("INSERT INTO study_sessions(id,course_id,start_time,duration_minutes,notes) SELECT id,course_id,start_time,duration_minutes,notes FROM imported.study_sessions");
                if (hasSettings) s.executeUpdate("INSERT INTO app_settings(setting_key,setting_value) SELECT setting_key,setting_value FROM imported.app_settings");
                if (hasGrades) s.executeUpdate("INSERT INTO academic_grades(id,course_id,title,earned,possible,weight) SELECT id,course_id,title,earned,possible,weight FROM imported.academic_grades");
                // Exercise domain validation before committing imported data.
                try (ResultSet r = s.executeQuery("SELECT * FROM assignments")) {
                    while (r.next()) new Assignment(r.getInt("id"), r.getInt("course_id"), r.getString("title"), r.getString("description"), java.time.LocalDateTime.parse(r.getString("due_date")), Assignment.Priority.valueOf(r.getString("priority")), r.getInt("completed") == 1);
                }
                try (ResultSet r = s.executeQuery("SELECT * FROM academic_grades")) {
                    while (r.next()) new AcademicGrade(r.getInt("id"), r.getInt("course_id"), r.getString("title"), r.getDouble("earned"), r.getDouble("possible"), r.getDouble("weight"));
                }
                try (ResultSet r = s.executeQuery("SELECT * FROM courses")) {
                    while (r.next()) new Course(r.getInt("id"), r.getString("name"), r.getString("code"));
                }
                try (ResultSet r = s.executeQuery("SELECT * FROM study_sessions")) {
                    while (r.next()) new StudySession(r.getInt("id"), r.getInt("course_id"), java.time.LocalDateTime.parse(r.getString("start_time")), r.getInt("duration_minutes"), r.getString("notes"));
                }
                c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    public void exportCsv(Path target) throws IOException, SQLException {
        StringBuilder csv = new StringBuilder("type,course_code,title,details,value,date\r\n");
        var codes = new java.util.HashMap<Integer, String>();
        for (Course course : database.getAllCourses()) {
            codes.put(course.getId(), course.getCode());
            row(csv, "course", course.getCode(), course.getName(), "", "", "");
        }
        for (Assignment a : database.getAllAssignments()) row(csv, "assignment", codes.get(a.getCourseId()), a.getTitle(), a.getDescription(), a.isCompleted() ? "Completed" : a.getPriority().name(), a.getDueDate().toString());
        for (StudySession s : database.getAllStudySessions()) row(csv, "study_session", codes.get(s.getCourseId()), "Study session", s.getNotes(), Integer.toString(s.getDurationMinutes()), s.getStartTime().toString());
        for (AcademicGrade g : database.getAcademicGrades()) row(csv, "grade", codes.get(g.courseId()), g.title(), "weight=" + g.weight(), g.earned() + "/" + g.possible(), "");
        Files.writeString(target, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }
    private static void row(StringBuilder csv, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) csv.append(',');
            String value = fields[i] == null ? "" : fields[i];
            if (!value.isEmpty() && "=+-@\t\r\n".indexOf(value.charAt(0)) >= 0) value = "'" + value;
            csv.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }
}
