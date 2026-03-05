package com.commonwealthu.tutor_scheduler.controller;


import com.commonwealthu.tutor_scheduler.service.SessionService;

public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {this.sessionService = sessionService;}

    //mapping to each schedule, after schedules are generated...
    //anything else?
}
