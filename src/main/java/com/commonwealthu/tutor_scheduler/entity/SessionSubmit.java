package com.commonwealthu.tutor_scheduler.entity;


public class SessionSubmit {
    private char day;
    private int start;
    private int end;

    public SessionSubmit() {}

    public char getDay() {return day;}

    public void setDay(char day) {this.day = day;}

    public int getStart() {return start;}

    public void setStart(int start) {this.start = start;}

    public int getEnd() {return end;}

    public void setEnd(int end) {this.end = end;}
}
