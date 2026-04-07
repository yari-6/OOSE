package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalTime;

@Controller
public class TimeSubmissionController {
    @Autowired
    private TutorService tutorService;
    @Autowired
    private SessionService sessionService;

    @GetMapping("/schedule-builder")
    public String buildSchedule(HttpSession browserSession) {
        Tutor loggedIn = tutorService.findTutorByID((String) browserSession.getAttribute("tutorID"));
        if(loggedIn.getType().equals("SI")) {
            return "time-submission-SI";
        } else {
            return "time-submission-edited";
        }
    }

    @PostMapping("/addTimes")
    public String addTimes(@RequestParam("day") char day, @RequestParam("start") LocalTime start,
                           @RequestParam("end") LocalTime end, HttpSession browserSession) {
        Tutor loggedIn = tutorService.findTutorByID((String) browserSession.getAttribute("tutorID"));
        Session submitted = new Session(new SessionID(loggedIn, day, start), end);
        sessionService.saveSession(submitted);
        return "time-submission-edited";
    }

    @PostMapping("/review")
    public String reviewTimes() {
        return "time-sumbit-confirm";
    }

    //sessions should already be created; redirect to the tutor's profile
    @GetMapping("/yes")
    public String confirm() {
        return "tutor-profile";
    }

    //will involve deleting sessions, which requires another repo+service method
    /*@GetMapping("/no")
    public String refuse(){

    }*/
}
