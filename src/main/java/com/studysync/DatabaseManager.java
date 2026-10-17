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

    static final int SCHEMA_VERSION = 1;
    private static final String DEFAULT_DATABASE_URL = "jdbc:sqlite:studysync.db";
    private final String databaseUrl;

    public DatabaseManager() { this(System.getProperty("studysync.database", DEFAULT_DATABASE_URL)); }

    public DatabaseManager(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) throw new IllegalArgumentException("Database URL cannot be empty.");
        this.databaseUrl = databaseUrl;
        initializeDatabase();
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(databaseUrl);
        try (Statement statement = connection.createStatement()) { statement.execute("PRAGMA foreign_keys = ON"); }
        return connection;
    }

    private void initializeDatabase() {
        String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, code TEXT NOT NULL UNIQUE COLLATE NOCASE)";
        String createAssignmentsTable = "CREATE TABLE IF NOT EXISTS assignments (id INTEGER PRIMARY KEY AUTOINCREMENT, course_id INTEGER NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL DEFAULT '', due_date TEXT NOT NULL, priority TEXT NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')), completed INTEGER NOT NULL DEFAULT 0 CHECK (completed IN (0, 1)), FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE)";
        String createStudySessionsTable = "CREATE TABLE IF NOT EXISTS study_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT, course_id INTEGER NOT NULL, start_time TEXT NOT NULL, duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0), notes TEXT NOT NULL DEFAULT '', FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE)";
        String createSettingsTable = "CREATE TABLE IF NOT EXISTS app_settings (setting_key TEXT PRIMARY KEY, setting_value TEXT NOT NULL)";
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            int version;
            try (ResultSet result = statement.executeQuery("PRAGMA user_version")) { result.next(); version = result.getInt(1); }
            if (version < 0 || version > SCHEMA_VERSION)
                throw new IllegalStateException("This database requires a newer StudySync version. Its data was not changed.");
            connection.setAutoCommit(false);
            try {
            statement.execute(createCoursesTable); statement.execute(createAssignmentsTable); statement.execute(createStudySessionsTable); statement.execute(createSettingsTable);
            statement.execute("CREATE TABLE IF NOT EXISTS academic_grades (id INTEGER PRIMARY KEY AUTOINCREMENT, course_id INTEGER NOT NULL REFERENCES courses(id) ON DELETE CASCADE, title TEXT NOT NULL, earned REAL NOT NULL CHECK(earned >= 0), possible REAL NOT NULL CHECK(possible > 0 AND earned <= possible), weight REAL NOT NULL CHECK(weight > 0))");
            statement.execute("CREATE INDEX IF NOT EXISTS assignments_course_due ON assignments(course_id, due_date)");
            statement.execute("CREATE INDEX IF NOT EXISTS sessions_course_start ON study_sessions(course_id, start_time)");
            statement.execute("PRAGMA user_version = " + SCHEMA_VERSION);
            connection.commit();
            } catch (SQLException | RuntimeException failure) { connection.rollback(); throw failure; }
        } catch (SQLException exception) { throw new IllegalStateException("Unable to initialize the StudySync database.", exception); }
    }

    public String getSetting(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Setting key cannot be empty.");
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            try (ResultSet results = statement.executeQuery()) { return results.next() ? results.getString("setting_value") : null; }
        } catch (SQLException exception) { throw new IllegalStateException("Unable to retrieve application setting.", exception); }
    }

    public void setSetting(String key, String value) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Setting key cannot be empty.");
        if (value == null) throw new IllegalArgumentException("Setting value cannot be null.");
        String sql = "INSERT INTO app_settings (setting_key, setting_value) VALUES (?, ?) ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key); statement.setString(2, value); statement.executeUpdate();
        } catch (SQLException exception) { throw new IllegalStateException("Unable to save application setting.", exception); }
    }

    public Course addCourse(String name, String code) {
        Course course = new Course(name, code); String sql = "INSERT INTO courses (name, code) VALUES (?, ?)";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, course.getName()); statement.setString(2, course.getCode()); statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { if (keys.next()) return new Course(keys.getInt(1), course.getName(), course.getCode()); }
            throw new IllegalStateException("Course was created, but no ID was returned.");
        } catch (SQLException exception) { throw new IllegalStateException("Unable to add course.", exception); }
    }
    public List<Course> getAllCourses() { List<Course> out=new ArrayList<>(); try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,name,code FROM courses ORDER BY code");ResultSet r=s.executeQuery()){while(r.next())out.add(new Course(r.getInt("id"),r.getString("name"),r.getString("code")));return out;}catch(SQLException e){throw new IllegalStateException("Unable to retrieve courses.",e);} }
    public Course findCourseById(int id){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,name,code FROM courses WHERE id=?")){s.setInt(1,id);try(ResultSet r=s.executeQuery()){return r.next()?new Course(r.getInt("id"),r.getString("name"),r.getString("code")):null;}}catch(SQLException e){throw new IllegalStateException("Unable to find course.",e);} }
    public boolean updateCourse(int id,String name,String code){Course x=new Course(id,name,code);try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("UPDATE courses SET name=?,code=? WHERE id=?")){s.setString(1,x.getName());s.setString(2,x.getCode());s.setInt(3,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to update course.",e);} }
    public boolean deleteCourse(int id){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("DELETE FROM courses WHERE id=?")){s.setInt(1,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to delete course.",e);} }

    public Assignment addAssignment(int courseId,String title,String description,LocalDateTime dueDate,Assignment.Priority priority){Assignment a=new Assignment(courseId,title,description,dueDate,priority);String sql="INSERT INTO assignments(course_id,title,description,due_date,priority,completed) VALUES(?,?,?,?,?,?)";try(Connection c=getConnection();PreparedStatement s=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){s.setInt(1,a.getCourseId());s.setString(2,a.getTitle());s.setString(3,a.getDescription());s.setString(4,a.getDueDate().toString());s.setString(5,a.getPriority().name());s.setInt(6,a.isCompleted()?1:0);s.executeUpdate();try(ResultSet k=s.getGeneratedKeys()){if(k.next())return new Assignment(k.getInt(1),a.getCourseId(),a.getTitle(),a.getDescription(),a.getDueDate(),a.getPriority(),a.isCompleted());}throw new IllegalStateException("Assignment was created, but no ID was returned.");}catch(SQLException e){throw new IllegalStateException("Unable to add assignment.",e);} }
    public List<Assignment> getAllAssignments(){List<Assignment> out=new ArrayList<>();try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,course_id,title,description,due_date,priority,completed FROM assignments ORDER BY due_date");ResultSet r=s.executeQuery()){while(r.next())out.add(mapAssignment(r));return out;}catch(SQLException e){throw new IllegalStateException("Unable to retrieve assignments.",e);} }
    public List<Assignment> getAssignmentsByCourse(int courseId){List<Assignment> out=new ArrayList<>();try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,course_id,title,description,due_date,priority,completed FROM assignments WHERE course_id=? ORDER BY due_date")){s.setInt(1,courseId);try(ResultSet r=s.executeQuery()){while(r.next())out.add(mapAssignment(r));}return out;}catch(SQLException e){throw new IllegalStateException("Unable to retrieve course assignments.",e);} }
    public Assignment findAssignmentById(int id){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,course_id,title,description,due_date,priority,completed FROM assignments WHERE id=?")){s.setInt(1,id);try(ResultSet r=s.executeQuery()){return r.next()?mapAssignment(r):null;}}catch(SQLException e){throw new IllegalStateException("Unable to find assignment.",e);} }
    public boolean updateAssignment(int id,int courseId,String title,String description,LocalDateTime dueDate,Assignment.Priority priority){Assignment a=new Assignment(courseId,title,description,dueDate,priority);try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("UPDATE assignments SET course_id=?,title=?,description=?,due_date=?,priority=? WHERE id=?")){s.setInt(1,a.getCourseId());s.setString(2,a.getTitle());s.setString(3,a.getDescription());s.setString(4,a.getDueDate().toString());s.setString(5,a.getPriority().name());s.setInt(6,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to update assignment.",e);} }
    public boolean setAssignmentCompleted(int id,boolean completed){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("UPDATE assignments SET completed=? WHERE id=?")){s.setInt(1,completed?1:0);s.setInt(2,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to update assignment completion status.",e);} }
    public boolean deleteAssignment(int id){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("DELETE FROM assignments WHERE id=?")){s.setInt(1,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to delete assignment.",e);} }
    private Assignment mapAssignment(ResultSet r)throws SQLException{return new Assignment(r.getInt("id"),r.getInt("course_id"),r.getString("title"),r.getString("description"),LocalDateTime.parse(r.getString("due_date")),Assignment.Priority.valueOf(r.getString("priority")),r.getInt("completed")==1);}

    public StudySession addStudySession(int courseId,LocalDateTime startTime,int durationMinutes,String notes){StudySession x=new StudySession(courseId,startTime,durationMinutes,notes);try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO study_sessions(course_id,start_time,duration_minutes,notes) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){s.setInt(1,x.getCourseId());s.setString(2,x.getStartTime().toString());s.setInt(3,x.getDurationMinutes());s.setString(4,x.getNotes());s.executeUpdate();try(ResultSet k=s.getGeneratedKeys()){if(k.next())return new StudySession(k.getInt(1),x.getCourseId(),x.getStartTime(),x.getDurationMinutes(),x.getNotes());}throw new IllegalStateException("Study session was created, but no ID was returned.");}catch(SQLException e){throw new IllegalStateException("Unable to add study session.",e);} }
    public List<StudySession> getAllStudySessions(){List<StudySession> out=new ArrayList<>();try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,course_id,start_time,duration_minutes,notes FROM study_sessions ORDER BY start_time DESC");ResultSet r=s.executeQuery()){while(r.next())out.add(mapStudySession(r));return out;}catch(SQLException e){throw new IllegalStateException("Unable to retrieve study sessions.",e);} }
    public List<StudySession> getStudySessionsByCourse(int courseId){List<StudySession> out=new ArrayList<>();try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,course_id,start_time,duration_minutes,notes FROM study_sessions WHERE course_id=? ORDER BY start_time DESC")){s.setInt(1,courseId);try(ResultSet r=s.executeQuery()){while(r.next())out.add(mapStudySession(r));}return out;}catch(SQLException e){throw new IllegalStateException("Unable to retrieve course study sessions.",e);} }
    public StudySession findStudySessionById(int id){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("SELECT id,course_id,start_time,duration_minutes,notes FROM study_sessions WHERE id=?")){s.setInt(1,id);try(ResultSet r=s.executeQuery()){return r.next()?mapStudySession(r):null;}}catch(SQLException e){throw new IllegalStateException("Unable to find study session.",e);} }
    public boolean updateStudySession(int id,int courseId,LocalDateTime startTime,int durationMinutes,String notes){StudySession x=new StudySession(courseId,startTime,durationMinutes,notes);try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("UPDATE study_sessions SET course_id=?,start_time=?,duration_minutes=?,notes=? WHERE id=?")){s.setInt(1,x.getCourseId());s.setString(2,x.getStartTime().toString());s.setInt(3,x.getDurationMinutes());s.setString(4,x.getNotes());s.setInt(5,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to update study session.",e);} }
    public boolean deleteStudySession(int id){try(Connection c=getConnection();PreparedStatement s=c.prepareStatement("DELETE FROM study_sessions WHERE id=?")){s.setInt(1,id);return s.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException("Unable to delete study session.",e);} }
    private StudySession mapStudySession(ResultSet r)throws SQLException{return new StudySession(r.getInt("id"),r.getInt("course_id"),LocalDateTime.parse(r.getString("start_time")),r.getInt("duration_minutes"),r.getString("notes"));}

    public List<AcademicGrade> getAcademicGrades() {
        List<AcademicGrade> grades = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT * FROM academic_grades ORDER BY id DESC")) {
            while (r.next()) grades.add(new AcademicGrade(r.getInt("id"), r.getInt("course_id"), r.getString("title"), r.getDouble("earned"), r.getDouble("possible"), r.getDouble("weight")));
            return grades;
        } catch (SQLException e) { throw new IllegalStateException("Unable to retrieve grades.", e); }
    }

    public void saveAcademicGrade(AcademicGrade grade) {
        String sql = grade.id() == 0
            ? "INSERT INTO academic_grades(course_id,title,earned,possible,weight) VALUES(?,?,?,?,?)"
            : "UPDATE academic_grades SET course_id=?,title=?,earned=?,possible=?,weight=? WHERE id=?";
        try (Connection c = getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, grade.courseId()); s.setString(2, grade.title()); s.setDouble(3, grade.earned());
            s.setDouble(4, grade.possible()); s.setDouble(5, grade.weight());
            if (grade.id() != 0) s.setInt(6, grade.id());
            if (s.executeUpdate() != 1) throw new IllegalArgumentException("Grade no longer exists.");
        } catch (SQLException e) { throw new IllegalStateException("Unable to save grade.", e); }
    }

    public void deleteAcademicGrade(int id) {
        try (Connection c = getConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM academic_grades WHERE id=?")) {
            s.setInt(1, id);
            if (s.executeUpdate() != 1) throw new IllegalArgumentException("Grade no longer exists.");
        } catch (SQLException e) { throw new IllegalStateException("Unable to delete grade.", e); }
    }
}
