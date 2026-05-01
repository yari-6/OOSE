package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.CourseService;
import com.commonwealthu.tutor_scheduler.dto.ScheduleInfo;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final TutorService tutorService;
    private final SessionService sessionService;
    private final CourseService courseService;

    public AdminController(TutorService tutorService, SessionService sessionService, CourseService courseService) {
        this.tutorService = tutorService;
        this.sessionService = sessionService;
        this.courseService = courseService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String tutorID = (String) session.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";
        Tutor user = tutorService.findTutorByID(tutorID);
        if (user == null || !user.isAdmin()) return "redirect:/";
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        model.addAttribute("pendingSessions", sessionService.getSessionsByLocationContaining("PENDING"));
        model.addAttribute("adminName", user.getFirstName());
        model.addAttribute("tutors", tutorService.getAllTutors());
        return "admin-dashboard";
    }

    @PostMapping("/toggle-window")
    public String toggleWindow(@RequestParam boolean open, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        sessionService.toggleSubmissionWindow(open);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/assign-room")
    public String assignRoom(@RequestParam String tutorId,
                             @RequestParam String day,
                             @RequestParam String time,
                             @RequestParam String room,
                             HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        sessionService.updateSessionLocation(tutorId, day, time, room);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/tutors")
    public String manageTutors(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("tutors", tutorService.getAllTutors());
        return "admin-tutors";
    }

    @PostMapping("/tutors/add")
    public String addTutor(@RequestParam String tutorID,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String type,
                           HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";

        Tutor tutor = new Tutor(tutorID.trim(), firstName.trim(), lastName.trim(), type.trim());
        tutorService.createTutor(tutor);
        return "redirect:/admin/tutors";
    }

    @PostMapping("/tutors/reset/{id}")
    public String resetTutorPassword(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        tutorService.resetTutorPassword(id);
        return "redirect:/admin/tutors";
    }

    @PostMapping("/tutors/delete/{id}")
    public String deleteTutor(@PathVariable String id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        tutorService.deleteTutor(id);
        return "redirect:/admin/tutors";
    }

    @GetMapping("/courses")
    public String manageCourses(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        model.addAttribute("courses", courseService.getAllCourses());
        return "admin-courses";
    }

    @PostMapping("/courses/add")
    public String addCourse(@RequestParam String courseSubject,
                            @RequestParam String courseNumber,
                            @RequestParam String courseTitle,
                            HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        courseService.createNewCourse(courseSubject, courseNumber, courseTitle);
        return "redirect:/admin/courses";
    }

    @PostMapping("/courses/delete")
    public String deleteCourse(@RequestParam String subject,
                               @RequestParam String number,
                               HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";
        courseService.deleteCourse(subject, number);
        return "redirect:/admin/courses";
    }

    @GetMapping("/schedule")
    public String masterEditor(@RequestParam(required = false, defaultValue = "Drop-in") String type,
                               HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        List<com.commonwealthu.tutor_scheduler.entity.Session> allSessions = sessionService.getAllSessions();
        List<LocalTime> times = sessionService.generateTimes();

        // Filter sessions by type manually for the grid
        List<com.commonwealthu.tutor_scheduler.entity.Session> filteredSessions = allSessions.stream()
                .filter(s -> s.getSessionID().getTutor().getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        // Use the same color assignment as the public schedule views.
        Map<String, ScheduleInfo> scheduleMap = sessionService.fillInSessions(filteredSessions, times);

        model.addAttribute("times", times);
        model.addAttribute("schedule", scheduleMap);
        model.addAttribute("currentFilter", type);
        model.addAttribute("tutors", tutorService.getAllTutors());

        // SI Table Data
        model.addAttribute("siTableData", allSessions.stream()
                .filter(s -> s.getSessionID().getTutor().getType().equalsIgnoreCase("SI"))
                .collect(Collectors.toList()));

        return "admin-schedule-editor";
    }

    @PostMapping("/save-master-session")
    public String saveMasterSession(@RequestParam String day, @RequestParam String time,
                                    @RequestParam String tutorId, @RequestParam String room,
                                    @RequestParam(defaultValue = "Drop-in") String currentFilter,
                                    HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            sessionService.adminSaveSession(tutorId, day, time, room);
        } catch (IllegalStateException e) {
            return masterEditor(currentFilter, session, model);
        }
        return "redirect:/admin/schedule?type=" + URLEncoder.encode(currentFilter, StandardCharsets.UTF_8);
    }

    @PostMapping("/delete-session")
    public String deleteSession(@RequestParam String tutorId, @RequestParam String day,
                                @RequestParam String time, @RequestParam(defaultValue = "Drop-in") String currentFilter) {
        sessionService.deleteSession(tutorId, day, time);
        return "redirect:/admin/schedule?type=" + URLEncoder.encode(currentFilter, StandardCharsets.UTF_8);
    }

    private boolean isAdmin(HttpSession session) {
        String tutorID = (String) session.getAttribute("tutorID");
        if (tutorID == null) return false;
        Tutor t = tutorService.findTutorByID(tutorID);
        return t != null && t.isAdmin();
    }
}
