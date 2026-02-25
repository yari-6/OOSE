package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CourseID implements Serializable {

    @Column(name = "CourseSubject", nullable = false, length = 5)
    private String courseSubject;

    @Column(name = "CourseNumber", nullable = false)
    private int courseNumber;

    // Required and only used by Hibernate
    protected CourseID() {}

    public String getCourseSubject() {
        return courseSubject;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    // equals and hashCode need to be overridden for Hibernate to compare objects properly
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseID courseID)) return false;
        return Objects.equals(courseSubject, courseID.courseSubject) && courseNumber == courseID.courseNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseSubject, courseNumber);
    }
}
