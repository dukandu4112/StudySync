package com.studysync;

/** Progress toward a weekly study target for one course. */
public record CourseStudyGoal(
        Course course,
        int targetMinutes,
        int studiedMinutes) {

    public CourseStudyGoal {
        if (course == null) throw new IllegalArgumentException("Course cannot be null.");
        if (targetMinutes <= 0) throw new IllegalArgumentException("Course study target must be greater than zero.");
        if (studiedMinutes < 0) throw new IllegalArgumentException("Studied minutes cannot be negative.");
    }

    public int remainingMinutes() {
        return Math.max(0, targetMinutes - studiedMinutes);
    }

    public double completionPercentage() {
        return Math.min(100.0, studiedMinutes * 100.0 / targetMinutes);
    }

    public boolean completed() {
        return studiedMinutes >= targetMinutes;
    }
}
