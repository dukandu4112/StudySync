import com.studysync.*;
import java.nio.file.*;

/** Synthetic records for the packaged Windows file-dialog test. */
class NativeDataFixture {
    public static void main(String[] args) throws Exception {
        Path file = Path.of(args[1]); Files.createDirectories(file.getParent());
        var db = new DatabaseManager("jdbc:sqlite:" + file); var service = new StudySyncService(db);
        switch (args[0]) {
            case "seed" -> {
                var course = service.createCourse("Native dialog test", "NATIVE101");
                service.saveAcademicGrade(new AcademicGrade(0, course.getId(), "Exam", 85, 100, 1));
                service.setDailyStudyTargetMinutes(45);
            }
            case "mutate" -> service.setDailyStudyTargetMinutes(90);
            case "original" -> {
                if (service.getDailyStudyTargetMinutes() != 45 || service.getAcademicGrades().size() != 1)
                    throw new AssertionError("Restore did not recover original data");
            }
            case "mutated" -> {
                if (service.getDailyStudyTargetMinutes() != 90) throw new AssertionError("Cancelled restore changed data");
            }
            default -> throw new IllegalArgumentException("Unknown fixture mode");
        }
        System.out.println("PASS fixture: " + args[0]);
    }
}
