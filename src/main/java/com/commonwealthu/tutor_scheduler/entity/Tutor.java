package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "\"Tutors\"")
public class Tutor {

    @Id
    @Column(name = "\"TutorID\"", nullable = false, length = 50)
    private String tutorID;

    @Column(name = "\"FirstName\"", nullable = false, length = 50)
    private String firstName;

    @Column(name = "\"LastName\"", nullable = false, length = 50)
    private String lastName;

    @Column(name = "\"TutorType\"", nullable = false, length = 10)
    private String type;

    @ManyToMany
    @JoinTable(
            name = "\"Tutor_Courses\"",
            joinColumns = @JoinColumn(name = "\"TutorID\""),
            inverseJoinColumns = {
                    @JoinColumn(name = "\"CourseSubject\"", referencedColumnName = "\"CourseSubject\""),
                    @JoinColumn(name = "\"CourseNumber\"", referencedColumnName = "\"CourseNumber\"")
            }
    )
    private Set<Course> coursesOffered = new HashSet<>();

    @Column(name = "\"Pass\"", length = 65)
    private String pass;

    @Column(name = "\"ProfilePicture\"", length = 255)
    private String profilePicture;

    @OneToMany(mappedBy = "sessionID.tutor")
    @OrderBy("sessionID.day ASC, sessionID.time ASC")
    private Set<Session> schedule = new HashSet<>();

    public Tutor() {}

    public Tutor(String tutorID, String firstName, String lastName, String type) {
        this.tutorID = tutorID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
    }

    public String getTutorID() {
        return tutorID;
    }

    public void setTutorID(String tutorID) {
        this.tutorID = tutorID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getType() {
        return type != null ? type.trim() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Course> getCoursesOffered() {
        return coursesOffered;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<Session> getSchedule() {
        return schedule;
    }

    public boolean isAdmin() { return "Admin".equalsIgnoreCase(this.type); }
}