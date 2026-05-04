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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
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
    public String buildSchedule(HttpSession browserSession,
                                Model model) {
        String currentUserID = (String) browserSession.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);

        Tutor tutor = tutorService.findTutorByID(currentUserID);

        // Redirect SI tutors to their specific builder
        if ("SI".equals(tutor.getType())) {
            return "redirect:/si-builder?currentUserID=" + currentUserID;
        }

        Set<Session> savedSessions = sessionService.getSessionsByTutor(tutor);
        Set<Session> addedSessions = sessionService.getAddedTimes(browserSession);

        //Set<Session> combinedSessions = new HashSet<>(savedSessions);
        //combinedSessions.addAll(addedSessions);
        List<LocalTime> times = sessionService.generateTimes();
        HashMap<String, ScheduleInfo> schedule = sessionService.fillInSessions(savedSessions, times);
        HashMap<String, ScheduleInfo> addedTimeSchedule = sessionService.fillInSessions(addedSessions, times);
        schedule.forEach((k, v) -> {
            List<String> colors = v.getColors();
            Collections.fill(colors, "#9CA3AF");
        });
        schedule.putAll(addedTimeSchedule);

        model.addAttribute("tutor", tutor);
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
                           HttpSession browserSession,
                           RedirectAttributes ra) {
        String currentUserID = (String) browserSession.getAttribute("tutorID");

        Tutor currentUser = tutorService.findTutorByID(currentUserID);

        // Security: Admin or self only
        if (!currentUser.isAdmin() && !currentUserID.equals(currentUserID)) {
            return "redirect:/";
        }

        // Admin bypasses window lock
        if (!sessionService.isSubmissionWindowOpen() && !currentUser.isAdmin()) {
            ra.addFlashAttribute("error", "The submission window is currently locked.");
            return "redirect:/schedule-builder";
        }

        if (Duration.between(start, end).toMinutes() > 300) {
            ra.addFlashAttribute("error", "Error: A single session cannot exceed 5 hours.");
            return "redirect:/schedule-builder?currentUserID=" + currentUserID;
        }

        String normalizedDay = day == null ? "" : day.trim();
        Session stagedSession = new Session(new SessionID(currentUser, normalizedDay, start), end);

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

        return "redirect:/schedule-builder?currentUserID=" + currentUserID;
    }

    @PostMapping("/confirm-times")
    public String confirmTimes(HttpSession browserSession) {
        String currentUserID = (String) browserSession.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);

        if (!sessionService.isSubmissionWindowOpen() && !currentUser.isAdmin()) {
            return "redirect:/schedule-builder";
        }

        Set<Session> addedTimes = sessionService.getAddedTimes(browserSession);
        sessionService.saveAllTimes(addedTimes);

        browserSession.removeAttribute("addedTimes");

        return currentUser.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/tutors/" + currentUserID;
    }

    @PostMapping("/reject-times")
    public String rejectTimes(HttpSession browserSession) {
        browserSession.removeAttribute("addedTimes");
        return "redirect:/schedule-builder";
    }
}

