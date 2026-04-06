package com.commonwealthu.tutor_scheduler.entity;

import java.time.LocalTime;
import jakarta.persistence.*;


@Entity
@Table(name = "Tutor_Sessions")
public class Session {

    @EmbeddedId
    private SessionID sessionID;

    @Column(name="endTime")
    private LocalTime endTime;

    protected Session() {}

    public Session(SessionID sessionId, LocalTime end) {
        this.sessionID = sessionId;
        this.endTime = end;
    }

    public SessionID getSessionID() {return sessionID;}

    public void setSessionID(SessionID sessionID) {this.sessionID = sessionID;}

    public LocalTime getEndTime() {return endTime;}

    public void setEndTime(LocalTime endTime) {this.endTime = endTime;}
}
