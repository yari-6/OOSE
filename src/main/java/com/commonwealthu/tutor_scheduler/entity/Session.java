package com.commonwealthu.tutor_scheduler.entity;

import java.time.LocalTime;
import jakarta.persistence.*;


@Entity
@Table(name = "\"Tutor_Sessions\"")
public class Session {

    @EmbeddedId
    private SessionID sessionID;

    @Column(name="\"endTime\"")
    private LocalTime endTime;

    @Column(name="\"location\"")
    private String location;

    @Column(name="\"professor\"")
    private String professor;

    @Column(name="\"class_meeting_times\"")
    private String classMeetingTimes;

    @Column(name="\"class_name\"")
    private String className;

    protected Session() {}

    public Session(SessionID sessionId, LocalTime end) {
        this.sessionID = sessionId;
        this.endTime = end;
    }

    public String getLocation() { return location;}

    public void setLocation(String location) { this.location = location;}

    public SessionID getSessionID() {return sessionID;}

    public void setSessionID(SessionID sessionID) {this.sessionID = sessionID;}

    public LocalTime getEndTime() {return endTime;}

    public void setEndTime(LocalTime endTime) {this.endTime = endTime;}

    public String getProfessor() { return professor; }

    public void setProfessor(String professor) { this.professor = professor; }

    public String getClassMeetingTimes() { return classMeetingTimes; }

    public void setClassMeetingTimes(String classMeetingTimes) { this.classMeetingTimes = classMeetingTimes; }

    public String getClassName() { return className; }

    public void setClassName(String className) { this.className = className; }


}
