package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

@Embeddable
public class SessionID implements Serializable {

    @ManyToOne
    @JoinColumn(name= "\"TutorID\"")
    private Tutor tutor;

    @Column(name="\"day\"")
    private String day;

    @Column(name = "\"time\"")
    private LocalTime time;

    protected SessionID() {}

    public SessionID(Tutor tutor, String day, LocalTime time) {
        this.tutor = tutor;
        this.day = day;
        this.time = time;
    }

    public Tutor getTutor() { return tutor; }

    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    public String getDay() { return day != null ? day.trim() : null; }

    public void setDay(String day) { this.day = (day != null) ? day.trim() : null;}

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionID sessionID)) return false;
        return Objects.equals(tutor, sessionID.tutor) &&
               Objects.equals(getDay(), sessionID.getDay()) &&
               Objects.equals(time, sessionID.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tutor, getDay(), time);
    }
}
