package com.studysync;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Application service that coordinates StudySync's core productivity features. */
public class StudySyncService {
    private final DatabaseManager databaseManager;

    public StudySyncService(DatabaseManager databaseManager) {
        if (databaseManager == null) throw new IllegalArgumentException("Database manager cannot be null.");
        this.databaseManager = databaseManager;
    }

    public Course createCourse(String name, String code) { return databaseManager.addCourse(name, code); }
    public List<Course> getCourses() { return databaseManager.getAllCourses(); }
    public boolean updateCourse(int courseId, String name, String code) { requireCourse(courseId); Course c=new Course(courseId,name,code); return databaseManager.updateCourse(courseId,c.getName(),c.getCode()); }
    public boolean deleteCourse(int courseId) { requireCourse(courseId); return databaseManager.deleteCourse(courseId); }

    public Assignment createAssignment(int courseId,String title,String description,LocalDateTime dueDate,Assignment.Priority priority) { requireCourse(courseId); return databaseManager.addAssignment(courseId,title,description,dueDate,priority); }
    public List<Assignment> getAssignments() { return databaseManager.getAllAssignments(); }
    public List<Assignment> getAssignmentsForCourse(int courseId) { requireCourse(courseId); return databaseManager.getAssignmentsByCourse(courseId); }
    public List<Assignment> getPendingAssignments() { return databaseManager.getAllAssignments().stream().filter(a->!a.isCompleted()).sorted(Comparator.comparing(Assignment::getDueDate)).toList(); }
    public List<Assignment> getCompletedAssignments() { return databaseManager.getAllAssignments().stream().filter(Assignment::isCompleted).sorted(Comparator.comparing(Assignment::getDueDate)).toList(); }
    public List<Assignment> getOverdueAssignments() { return databaseManager.getAllAssignments().stream().filter(Assignment::isOverdue).sorted(Comparator.comparing(Assignment::getDueDate)).toList(); }
    public List<Assignment> getUpcomingAssignments(int days) { if(days<=0)throw new IllegalArgumentException("Upcoming assignment window must be greater than zero days.");LocalDateTime now=LocalDateTime.now(),deadline=now.plusDays(days);return databaseManager.getAllAssignments().stream().filter(a->!a.isCompleted()).filter(a->!a.getDueDate().isBefore(now)).filter(a->!a.getDueDate().isAfter(deadline)).sorted(Comparator.comparing(Assignment::getDueDate)).toList(); }
    public List<Assignment> getAssignmentsByPriority(Assignment.Priority priority) { if(priority==null)throw new IllegalArgumentException("Assignment priority cannot be null.");return databaseManager.getAllAssignments().stream().filter(a->a.getPriority()==priority).sorted(Comparator.comparing(Assignment::getDueDate)).toList(); }
    public List<Assignment> searchAssignments(String query) { if(query==null||query.isBlank())throw new IllegalArgumentException("Search query cannot be empty.");String q=query.trim().toLowerCase(Locale.ROOT);return databaseManager.getAllAssignments().stream().filter(a->a.getTitle().toLowerCase(Locale.ROOT).contains(q)||a.getDescription().toLowerCase(Locale.ROOT).contains(q)).sorted(Comparator.comparing(Assignment::getDueDate)).toList(); }
    public boolean updateAssignment(int assignmentId,int courseId,String title,String description,LocalDateTime dueDate,Assignment.Priority priority) { requireAssignment(assignmentId);requireCourse(courseId);Assignment a=new Assignment(courseId,title,description,dueDate,priority);return databaseManager.updateAssignment(assignmentId,a.getCourseId(),a.getTitle(),a.getDescription(),a.getDueDate(),a.getPriority()); }
    public boolean deleteAssignment(int assignmentId) { requireAssignment(assignmentId);return databaseManager.deleteAssignment(assignmentId); }
    public boolean completeAssignment(int assignmentId) { requireAssignment(assignmentId);return databaseManager.setAssignmentCompleted(assignmentId,true); }
    public boolean reopenAssignment(int assignmentId) { requireAssignment(assignmentId);return databaseManager.setAssignmentCompleted(assignmentId,false); }

    public StudySession recordStudySession(int courseId,LocalDateTime startTime,int durationMinutes,String notes) { requireCourse(courseId);return databaseManager.addStudySession(courseId,startTime,durationMinutes,notes); }
    public List<StudySession> getStudySessions() { return databaseManager.getAllStudySessions(); }
    public List<StudySession> getStudySessionsForCourse(int courseId) { requireCourse(courseId);return databaseManager.getStudySessionsByCourse(courseId); }
    public boolean updateStudySession(int sessionId,int courseId,LocalDateTime startTime,int durationMinutes,String notes) { requireStudySession(sessionId);requireCourse(courseId);StudySession s=new StudySession(courseId,startTime,durationMinutes,notes);return databaseManager.updateStudySession(sessionId,s.getCourseId(),s.getStartTime(),s.getDurationMinutes(),s.getNotes()); }
    public boolean deleteStudySession(int sessionId) { requireStudySession(sessionId);return databaseManager.deleteStudySession(sessionId); }
    public int getTotalStudyMinutes() { return databaseManager.getAllStudySessions().stream().mapToInt(StudySession::getDurationMinutes).sum(); }
    public int getTotalStudyMinutesForCourse(int courseId) { requireCourse(courseId);return databaseManager.getStudySessionsByCourse(courseId).stream().mapToInt(StudySession::getDurationMinutes).sum(); }

    public double getAssignmentCompletionPercentage() { List<Assignment> a=databaseManager.getAllAssignments();if(a.isEmpty())return 0.0;long completed=a.stream().filter(Assignment::isCompleted).count();return completed*100.0/a.size(); }
    public DashboardSummary getDashboardSummary() { List<Course> courses=databaseManager.getAllCourses();List<Assignment> a=databaseManager.getAllAssignments();int completed=(int)a.stream().filter(Assignment::isCompleted).count();int pending=a.size()-completed;int overdue=(int)a.stream().filter(Assignment::isOverdue).count();return new DashboardSummary(courses.size(),a.size(),pending,completed,overdue,getTotalStudyMinutes(),getAssignmentCompletionPercentage()); }

    private Course requireCourse(int id) { Course c=databaseManager.findCourseById(id);if(c==null)throw new IllegalArgumentException("Course does not exist: "+id);return c; }
    private Assignment requireAssignment(int id) { Assignment a=databaseManager.findAssignmentById(id);if(a==null)throw new IllegalArgumentException("Assignment does not exist: "+id);return a; }
    private StudySession requireStudySession(int id) { StudySession s=databaseManager.findStudySessionById(id);if(s==null)throw new IllegalArgumentException("Study session does not exist: "+id);return s; }
}
