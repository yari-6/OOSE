package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.dto.ScheduleInfo;
import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.servlet.http.HttpSession;
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

    private final TutorService tutorService;

    private final SessionService sessionService;

    public TimeSubmissionController(TutorService tutorService, SessionService sessionService) {
        this.tutorService = tutorService;
        this.sessionService = sessionService;
    }

    @GetMapping("/schedule-builder")
    public String buildSchedule(HttpSession browserSession) {
        Tutor loggedIn = tutorService.findTutorByID((String) browserSession.getAttribute("tutorID"));
        if(loggedIn.getType().equals("SI")) {
            return "time-submission-SI";
        } else {
            return "time-submission-edited";
        }
    }

    @PostMapping("/add-times")
    public String addTimes(@RequestParam("day") char day, @RequestParam("start") LocalTime start,
                           @RequestParam("end") LocalTime end, HttpSession browserSession) {
        Tutor loggedIn = tutorService.findTutorByID((String) browserSession.getAttribute("tutorID"));
        Session addedTime = new Session(new SessionID(loggedIn, day, start), end);

        // Create the Session from the submitted times, and store them inside the browser session for confirmation
        // in the review page, rather than saving directly after creation
        Set<Session> addedTimes = sessionService.getAddedTimes(browserSession);
        addedTimes.add(addedTime);

        return "time-submission-edited";
    }

    // Review page only shows sessions added during one browser session, existing sessions can be added if wanted
    @GetMapping("/review-times")
    public String reviewTimes(HttpSession browserSession, Model model) {
        Tutor tutor = tutorService.findTutorByID((String) browserSession.getAttribute("tutorID"));
        // Get the Sessions added from addTimes page instead of getting all Sessions
        Set<Session> unsavedSessions = sessionService.getAddedTimes(browserSession);
        List<LocalTime> times = sessionService.generateTimes();
        HashMap<String, ScheduleInfo> schedule = sessionService.fillInSessions(unsavedSessions, times);

        model.addAttribute("tutor", tutor);
        model.addAttribute("times", times);
        model.addAttribute("schedule", schedule);

        return "time-submit-confirm";
    }

    @PostMapping("/confirm-times")
    public String confirmTimes(HttpSession browserSession) {
        String tutorID = (String) browserSession.getAttribute("tutorID");
        Set<Session> addedTimes = sessionService.getAddedTimes(browserSession);
        sessionService.saveAllTimes(addedTimes);
        return "redirect:/tutors/" + tutorID;
    }

    @PostMapping("/reject-times")
    public String rejectTimes(HttpSession browserSession) {
        browserSession.removeAttribute("addedTimes");
        return "redirect:/schedule-builder";
    }

}
