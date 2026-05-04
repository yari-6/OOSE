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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
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
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/";
        try {
            sessionService.updateSessionLocation(tutorId, day, time, room);
            redirectAttributes.addFlashAttribute("success", "Room assigned successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/tutors")
    public String manageTutors(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        List<Tutor> tutors = tutorService.getAllTutors();

        Map<String, List<String>> tutorCourseMap = new HashMap<>();
        for (Tutor t : tutors) {
            List<String> keys = t.getCoursesOffered().stream()
                    .map(c -> c.getCourseID().getCourseSubject() + "-" + c.getCourseID().getCourseNumber())
                    .collect(Collectors.toList());
            tutorCourseMap.put(t.getTutorID(), keys);
        }

        model.addAttribute("tutors", tutors);
        model.addAttribute("tutorCourseMap", tutorCourseMap);
        model.addAttribute("allCourses", courseService.getAllCourses());
        return "admin-tutors";
    }

    @PostMapping("/tutors/update-full")
    public String updateTutorFull(@RequestParam String tutorID,
                                  @RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String type,
                                  @RequestParam(required = false) List<String> courseKeys,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/";

        try {
            Tutor tutor = tutorService.findTutorByID(tutorID);
            if (tutor != null) {
                tutor.setFirstName(firstName.trim());
                tutor.setLastName(lastName.trim());
                tutor.setType(type.trim());

                tutorService.updateTutorCourses(tutorID, courseKeys);

                tutorService.createTutor(tutor);
                redirectAttributes.addFlashAttribute("success", "Tutor " + tutorID + " updated successfully.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Update failed: " + e.getMessage());
        }

        return "redirect:/admin/tutors";
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
    public String masterEditor(@RequestParam(required = false) String type,
                               @RequestParam(required = false) String tutorId,
                               HttpSession session,
                               Model model) {
        if (!isAdmin(session)) return "redirect:/";

        List<com.commonwealthu.tutor_scheduler.entity.Session> allSessions = sessionService.getAllSessions();
        List<LocalTime> times = sessionService.generateTimes();

        String activeType = (type != null) ? type : "Drop-in";
        String searchId = (tutorId != null && !tutorId.isEmpty()) ? tutorId : null;

        final String finalType = activeType;
        final String finalTutorId = searchId;

        List<com.commonwealthu.tutor_scheduler.entity.Session> filteredSessions = allSessions.stream()
                .filter(s -> {
                    if (finalTutorId != null) {
                        return s.getSessionID().getTutor().getTutorID().equals(finalTutorId);
                    }
                    return s.getSessionID().getTutor().getType().equalsIgnoreCase(finalType);
                })
                .collect(Collectors.toList());

        String displayFilter = activeType;
        if (finalTutorId != null) {
            Tutor selectedTutor = tutorService.findTutorByID(finalTutorId);
            if (selectedTutor != null) {
                displayFilter = selectedTutor.getFirstName() + " " + selectedTutor.getLastName();
            }
        }

        Map<String, ScheduleInfo> scheduleMap = sessionService.fillInSessions(filteredSessions, times);

        model.addAttribute("times", times);
        model.addAttribute("schedule", scheduleMap);
        model.addAttribute("currentFilter", displayFilter);
        model.addAttribute("tutors", tutorService.getAllTutors());

        model.addAttribute("siTableData", allSessions.stream()
                .filter(s -> s.getSessionID().getTutor().getType().equalsIgnoreCase("SI"))
                .collect(Collectors.toList()));

        return "admin-schedule-editor";
    }

    @PostMapping("/save-master-session")
    public String saveMasterSession(@RequestParam String tutorId,
                                    @RequestParam String day,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime,
                                    @RequestParam String location,
                                    @RequestParam(required = false) String className,
                                    @RequestParam(required = false) String professor,
                                    @RequestParam(required = false) String meetingTimes,
                                    @RequestParam(required = false) String oldTutorId,
                                    @RequestParam(required = false) String oldStartTime,
                                    @RequestParam(defaultValue = "Drop-in") String currentFilter,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/";

        try {
            boolean tutorChanged = (oldTutorId != null && !oldTutorId.isEmpty() && !oldTutorId.equals(tutorId));
            boolean timeChanged = (oldStartTime != null && !oldStartTime.isEmpty() && !oldStartTime.equals(startTime));

            if (tutorChanged || timeChanged) {
                String targetTutor = (oldTutorId != null) ? oldTutorId : tutorId;
                String targetTime = (oldStartTime != null) ? oldStartTime : startTime;
                sessionService.deleteSession(targetTutor, day, targetTime);
            }

            sessionService.adminSaveSession(tutorId, day, startTime, endTime, location, className, professor, meetingTimes);
            redirectAttributes.addFlashAttribute("success", "Session saved!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/schedule?type=" + URLEncoder.encode(currentFilter, StandardCharsets.UTF_8);
    }

    @PostMapping("/delete-session")
    public String deleteSession(@RequestParam String tutorId,
                                @RequestParam String day,
                                @RequestParam String startTime, // Match the HTML name
                                @RequestParam(defaultValue = "Drop-in") String currentFilter) {
        sessionService.deleteSession(tutorId, day, startTime);
        return "redirect:/admin/schedule?type=" + URLEncoder.encode(currentFilter, StandardCharsets.UTF_8);
    }

    private boolean isAdmin(HttpSession session) {
        String tutorID = (String) session.getAttribute("tutorID");
        if (tutorID == null) return false;
        Tutor t = tutorService.findTutorByID(tutorID);
        return t != null && t.isAdmin();
    }


}
