package com.studysync;

/** A scored assessment with a relative weight within its course. */
public record AcademicGrade(int id, int courseId, String title, double earned, double possible, double weight) {
    public AcademicGrade {
        if (id < 0) throw new IllegalArgumentException("Grade ID cannot be negative.");
        if (courseId <= 0) throw new IllegalArgumentException("Select a course.");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Enter an assessment title.");
        title = title.trim();
        if (!Double.isFinite(earned) || !Double.isFinite(possible) || !Double.isFinite(weight)
                || possible <= 0 || earned < 0 || earned > possible || weight <= 0)
            throw new IllegalArgumentException("Earned points must be between zero and possible points; possible points and weight must be positive finite numbers.");
    }
    public double percentage() { return (earned / possible) * 100.0; }
}
