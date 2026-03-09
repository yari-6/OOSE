package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table (name = "Courses")
public class Course {

    @EmbeddedId
    private CourseID courseID;

    @Column(name = "CourseTitle", length = 200)
    private String courseTitle;

    @ManyToMany (mappedBy = "coursesOffered")
    private Set<Tutor> tutors = new HashSet<Tutor>();

    // Required and only used by Hibernate
    protected Course() {}

    public CourseID getCourseID() {
        return courseID;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public Set<Tutor> getTutors() {
        return tutors;
    }

    // Possibly add equals and hashcode, unsure if needed

}
