package com.commonwealthu.tutor_scheduler.dto;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing the information needed in each cell of the schedule display grid
 * (name(s), tutorId(s), and color)
 */
public class ScheduleInfo {

    private List<String> names;
    private List<String> tutorIDs;
    private List<String> colors;
    private String location;
    private LocalTime endTime;

    // Fixed the constructor parameters and logic
    public ScheduleInfo() {
        this.names = new ArrayList<>();
        this.tutorIDs = new ArrayList<>(); // Case-sensitive: must match the field name
        this.colors = new ArrayList<>();
        this.location = "";
    }

    public LocalTime getEndTime() {return endTime; }

    public void setEndTime(LocalTime endTime) {this.endTime = endTime;}

    public List<String> getTutorIDs() {
        return tutorIDs;
    }

    public void setTutorIDs(List<String> tutorIDs) {
        this.tutorIDs = tutorIDs;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public String getLocation () { return location; }

    public void setLocation(String location) { this.location = location; }

    public void addTutor(String name, String tutorID, String color) {
        names.add(name);
        tutorIDs.add(tutorID);
        colors.add(color);
    }

    // Gets the cell styling based on how many tutors are in a grid cell
    public String getBackgroundStyle() {
        if (colors.size() == 1) {
            return "background-color: " + colors.getFirst();
        }
        return "background: linear-gradient(to right, " + colors.getFirst() + " 50%, " + colors.get(1) + " 50%)";
    }
}