package com.commonwealthu.tutor_scheduler.entity;

/**
 * Class for storing the information needed in each cell of the schedule display grid (name(s) and color)
 */
public class ScheduleInfo {

    private String names;

    private String color;

    public ScheduleInfo(String names, String color) {
        this.names = names;
        this.color = color;
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
