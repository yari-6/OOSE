package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table (name = "\"courses\"")
public class Course {

    @EmbeddedId
    private CourseID courseID;

    @Column(name = "\"CourseTitle\"", nullable = false, length = 200)
    private String courseTitle;

    // Required and only used by Hibernate
    protected Course() {}

    public Course(CourseID courseID, String courseTitle) {
        this.courseID = courseID;
        this.courseTitle = courseTitle;
    }
    public CourseID getCourseID() { return courseID; }
    public void setCourseID(CourseID courseID) { this.courseID = courseID; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(courseID, course.courseID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseID);
    }
}

