package com.commonwealthu.tutor_scheduler.dto;

import java.util.List;

/**
 * Class for storing the information needed in each cell of the schedule display grid
 * (name(s), tutorId(s), and color)
 */
public class ScheduleInfo {

    private String names;
    private String tutorId;
    private String color;

    // Fixed the constructor parameters and logic
    public ScheduleInfo(String names, String tutorId, String color) {
        this.names = names;
        this.tutorId = tutorId; // Case-sensitive: must match the field name
        this.color = color;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}