package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "Tutor_Sessions")
public class Session {

    @EmbeddedId
    private SessionID sessionID;

    @Column(name="date")
    private int date; //some way to differentiate between weeks; for use with booked tag

    @Column(name="booked")
    private boolean booked = false;  //true if booked, false otherwise

    protected Session() {};

    public SessionID getSessionID() {return sessionID;}

    public void setSessionID(SessionID sessionID) {this.sessionID = sessionID;}

    public int getDate() {return date;}

    public void setDate(int date) {this.date = date;}

    public void markBooked() {
        booked = true;
    }
}
