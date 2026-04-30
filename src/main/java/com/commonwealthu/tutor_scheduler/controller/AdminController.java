package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TutorService tutorService;
    private final SessionService sessionService;

    public AdminController(TutorService tutorService, SessionService sessionService) {
        this.tutorService = tutorService;
        this.sessionService = sessionService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String tutorID = (String) session.getAttribute("tutorID");

        // Safety check for null tutorID
        if (tutorID == null) {
            return "redirect:/sign-in";
        }

        Tutor user = tutorService.findTutorByID(tutorID);

        // If user doesn't exist or isn't admin, send back to front page
        if (user == null || !user.isAdmin()) {
            return "redirect:/";
        }
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        model.addAttribute("pendingSessions", sessionService.getSessionsByLocationContaining("PENDING"));
        model.addAttribute("adminName", user.getFirstName());

        return "admin-dashboard";
    }

    @PostMapping("/assign-room")
    public String assignRoom(@RequestParam String day,
                             @RequestParam String time,
                             @RequestParam String tutorId,
                             @RequestParam String room) {
        // Logic to update the location for a specific session
        sessionService.updateSessionLocation(tutorId, day.charAt(0), time, room);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/toggle-window")
    public String toggleWindow(@RequestParam boolean open) {
        sessionService.toggleSubmissionWindow(open);
        return "redirect:/admin/dashboard";
    }
}