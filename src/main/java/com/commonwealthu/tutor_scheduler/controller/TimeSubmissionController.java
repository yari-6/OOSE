package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.ScheduleInfo;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.Duration;
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
    public String buildSchedule(HttpSession browserSession, Model model) {
        String tutorID = (String) browserSession.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";

        Tutor loggedIn = tutorService.findTutorByID(tutorID);

        //admin redirect
        if (loggedIn.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        // SI redirect
        if ("SI".equals(loggedIn.getType())) {
            return "redirect:/si-builder";
        }

        //show previously entered sessions
        Set<Session> savedSessions = sessionService.getSessionsByTutor(loggedIn);
        Set<Session> addedSessions = sessionService.getAddedTimes(browserSession);

        //preview with entries
        Set<Session> combinedSessions = new HashSet<>(savedSessions);
        combinedSessions.addAll(addedSessions);

        List<LocalTime> times = sessionService.generateTimes();
        HashMap<String, ScheduleInfo> schedule = sessionService.fillInSessions(combinedSessions, times);

        model.addAttribute("tutor", loggedIn);
        model.addAttribute("times", times);
        model.addAttribute("schedule", schedule);
        model.addAttribute("hasUnsavedChanges", !addedSessions.isEmpty());
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        return "time-submission-edited";
    }

    @PostMapping("/add-times")
    public String addTimes(@RequestParam("day") char day,
                           @RequestParam("start") LocalTime start,
                           @RequestParam("end") LocalTime end,
                           HttpSession browserSession,
                           RedirectAttributes ra) {

        String tutorID = (String) browserSession.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";

        Tutor loggedIn = tutorService.findTutorByID(tutorID);

        if (loggedIn.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        //no entry if window closed
        if (!sessionService.isSubmissionWindowOpen()) {
            ra.addFlashAttribute("error", "The submission window is currently locked.");
            return "redirect:/schedule-builder";
        }
        // limit to 5 hours
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes > 300) {
            ra.addFlashAttribute("error", "Error: A single session cannot exceed 5 hours.");
            return "redirect:/schedule-builder";
        }

        com.commonwealthu.tutor_scheduler.entity.Session stagedSession =
                new com.commonwealthu.tutor_scheduler.entity.Session(new SessionID(loggedIn, day, start), end);

        try {
            sessionService.validateDropInConstraints(stagedSession);

            //overwrite overlaps
            Set<com.commonwealthu.tutor_scheduler.entity.Session> currentStaged = sessionService.getAddedTimes(browserSession);

            currentStaged.removeIf(existing ->
                    existing.getSessionID().getDay() == day &&
                            start.isBefore(existing.getEndTime()) &&
                            end.isAfter(existing.getSessionID().getTime())
            );

            currentStaged.add(stagedSession);
            ra.addFlashAttribute("success", "Time added successfully.");

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/schedule-builder";
    }

    @PostMapping("/confirm-times")
    public String confirmTimes(HttpSession browserSession) {
        String tutorID = (String) browserSession.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";
        Tutor tutor = tutorService.findTutorByID(tutorID);
        if (tutor.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        if (!sessionService.isSubmissionWindowOpen() && !tutor.isAdmin()) {
            return "redirect:/schedule-builder";
        }
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
