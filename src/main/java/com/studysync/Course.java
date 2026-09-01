package com.studysync;

import java.util.Objects;

/**
 * Represents an academic course managed by StudySync.
 */
public class Course {

    private int id;
    private String name;
    private String code;

    public Course(String name, String code) {
        setName(name);
        setCode(code);
    }

    public Course(int id, String name, String code) {
        this(name, code);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Course name cannot be empty.");
        }

        this.name = name.trim();
    }

    public void setCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }

        this.code = code.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Course course)) {
            return false;
        }

        return id == course.id
                && Objects.equals(name, course.name)
                && Objects.equals(code, course.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code);
    }
}
