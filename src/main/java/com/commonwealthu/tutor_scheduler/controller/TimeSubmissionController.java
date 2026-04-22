package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

    @GetMapping("/review")
    public String reviewTimes(HttpSession browserSession, Model model) {
        Tutor tutor = tutorService.findTutorByID((String) browserSession.getAttribute("tutorID"));
        Set<Session> sessions = sessionService.getSessionsByTutor(tutor);
        List<LocalTime> times = sessionService.generateTimes();
        HashMap<String, Boolean> schedule = sessionService.fillInSessions(sessions, times);

        model.addAttribute("tutor", tutor);
        model.addAttribute("times", times);
        model.addAttribute("schedule", schedule);

        return "time-submit-confirm";
    }

    @GetMapping("/no")
    public String refuse(){
        return "time-submission-edited";
    }

}
