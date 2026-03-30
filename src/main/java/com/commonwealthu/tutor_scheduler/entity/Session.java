package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "Tutor_Sessions")
public class Session {

    @EmbeddedId
    private SessionID sessionID;

    @Column(name="endTime")
    private int endTime; //some way to differentiate between weeks; for use with booked tag

    protected Session() {};

    public SessionID getSessionID() {return sessionID;}

    public void setSessionID(SessionID sessionID) {this.sessionID = sessionID;}

    public int getEndTime() {return endTime;}

    public void setEndTime(int endTime) {this.endTime = endTime;}
}
