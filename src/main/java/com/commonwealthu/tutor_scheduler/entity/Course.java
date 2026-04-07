package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table (name = "courses")
public class Course {

    @EmbeddedId
    private CourseID courseID;

    @Column(name = "CourseTitle", length = 200)
    private String courseTitle;

    // Required and only used by Hibernate
    protected Course() {}

    public CourseID getCourseID() {
        return courseID;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    // Possibly add equals and hashcode, unsure if needed

}
