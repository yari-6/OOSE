package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "Tutor_Sessions")
public class Session {

    @EmbeddedId
    private SessionID sessionID;

    @Column(name="endTime")
    private double endTime;

    protected Session() {};

    public Session(SessionID sessionId, double end) {
        this.sessionID = sessionId;
        this.endTime = end;
    }

    public SessionID getSessionID() {return sessionID;}

    public void setSessionID(SessionID sessionID) {this.sessionID = sessionID;}

    public double getEndTime() {return endTime;}

    public void setEndTime(double endTime) {this.endTime = endTime;}
}
