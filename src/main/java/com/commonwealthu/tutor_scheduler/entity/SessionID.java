package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SessionID implements Serializable {

    @ManyToOne
    @JoinColumn(name="TutorID")
    private Tutor tutor;

    @Column(name="day")
    private char day;

    @Column(name = "time")
    private double time;

    protected SessionID() {};

    public Tutor getTutor() { return tutor; }

    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public char getDay() {
        return day;
    }

    public void setDay(char day) {
        this.day = day;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionID sessionID)) return false;
        return Objects.equals(tutor, sessionID.tutor) && day == sessionID.day && time==sessionID.time;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tutor, day, time);
    }
}
