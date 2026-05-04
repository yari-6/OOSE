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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.*;

@Controller
public class TimeSubmissionController {

    private final TutorService tutorService;

    private final SessionService sessionService;

    public TimeSubmissionController(TutorService tutorService, SessionService sessionService) {
        this.tutorService = tutorService;
        this.sessionService = sessionService;
    }

    @GetMapping("/schedule-builder")
    public String buildSchedule(@RequestParam(required = false) String targetTutorID,
                                HttpSession browserSession,
                                Model model) {
        String currentUserID = (String) browserSession.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);

        // Determine who we are actually editing
        String effectiveTutorID = (currentUser.isAdmin() && targetTutorID != null)
                ? targetTutorID
                : currentUserID;

        Tutor tutor = tutorService.findTutorByID(effectiveTutorID);

        // Admin safety: if they hit the builder without a target, send them back
        if (currentUser.isAdmin() && targetTutorID == null) {
            return "redirect:/admin/dashboard";
        }

        // Redirect SI tutors to their specific builder
        if ("SI".equals(tutor.getType())) {
            return "redirect:/si-builder?targetTutorID=" + effectiveTutorID;
        }

        Set<Session> savedSessions = sessionService.getSessionsByTutor(tutor);
        Set<Session> addedSessions = sessionService.getAddedTimes(browserSession);

        Set<Session> combinedSessions = new HashSet<>(savedSessions);
        combinedSessions.addAll(addedSessions);

        List<LocalTime> times = sessionService.generateTimes();
        HashMap<String, ScheduleInfo> schedule = sessionService.fillInSessions(combinedSessions, times);

        model.addAttribute("tutor", tutor);
        model.addAttribute("tutorID", effectiveTutorID);
        model.addAttribute("times", times);
        model.addAttribute("schedule", schedule);
        model.addAttribute("hasUnsavedChanges", !addedSessions.isEmpty());
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        model.addAttribute("isAdmin", currentUser.isAdmin());

        return "time-submission-edited";
    }

    @PostMapping("/add-times")
    public String addTimes(@RequestParam("day") String day,
                           @RequestParam("start") LocalTime start,
                           @RequestParam("end") LocalTime end,
                           @RequestParam("effectiveTutorID") String effectiveTutorID,
                           HttpSession browserSession,
                           RedirectAttributes ra) {

        String currentUserID = (String) browserSession.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);
        Tutor targetTutor = tutorService.findTutorByID(effectiveTutorID);

        // Security: Admin or self only
        if (!currentUser.isAdmin() && !currentUserID.equals(effectiveTutorID)) {
            return "redirect:/";
        }

        // Admin bypasses window lock
        if (!sessionService.isSubmissionWindowOpen() && !currentUser.isAdmin()) {
            ra.addFlashAttribute("error", "The submission window is currently locked.");
            return "redirect:/schedule-builder";
        }

        if (Duration.between(start, end).toMinutes() > 300) {
            ra.addFlashAttribute("error", "Error: A single session cannot exceed 5 hours.");
            return "redirect:/schedule-builder?targetTutorID=" + effectiveTutorID;
        }

        String normalizedDay = day == null ? "" : day.trim();
        Session stagedSession = new Session(new SessionID(targetTutor, normalizedDay, start), end);

        try {
            sessionService.validateDropInConstraints(stagedSession);
            Set<Session> currentStaged = sessionService.getAddedTimes(browserSession);

            currentStaged.removeIf(existing ->
                    existing.getSessionID().getDay().equals(normalizedDay) &&
                            start.isBefore(existing.getEndTime()) &&
                            end.isAfter(existing.getSessionID().getTime())
            );

            currentStaged.add(stagedSession);
            ra.addFlashAttribute("success", "Time added successfully.");

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/schedule-builder?targetTutorID=" + effectiveTutorID;
    }

    @PostMapping("/confirm-times")
    public String confirmTimes(@RequestParam("effectiveTutorID") String effectiveTutorID,
                               HttpSession browserSession) {
        String currentUserID = (String) browserSession.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);

        if (!sessionService.isSubmissionWindowOpen() && !currentUser.isAdmin()) {
            return "redirect:/schedule-builder";
        }

        Set<Session> addedTimes = sessionService.getAddedTimes(browserSession);
        sessionService.saveAllTimes(addedTimes);

        browserSession.removeAttribute("addedTimes");

        return currentUser.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/tutors/" + effectiveTutorID;
    }

    @PostMapping("/reject-times")
    public String rejectTimes(HttpSession browserSession) {
        browserSession.removeAttribute("addedTimes");
        return "redirect:/schedule-builder";
    }
}

