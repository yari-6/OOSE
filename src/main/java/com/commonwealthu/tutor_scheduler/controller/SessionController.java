package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import java.time.format.DateTimeFormatter;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SessionController {
    private final SessionService sessionService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    public SessionController(SessionService sessionService, TutorService tutorService) {
        this.sessionService = sessionService;
        // TutorService can be removed if not used for specific logic here
    }

    @GetMapping("/schedules/SI")
    public String siSchedule(Model model) {
        var sessions = sessionService.getSessionsByType("SI");

        List<Map<String, Object>> schedule = sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            Tutor tutor = s.getSessionID().getTutor();

            map.put("tutorId", tutor.getTutorID());
            map.put("day", s.getSessionID().getDay());
            map.put("start", s.getSessionID().getTime().format(TIME_FORMATTER));
            map.put("end", s.getEndTime().format(TIME_FORMATTER));
            map.put("location", s.getLocation());
            map.put("siLeader", tutor.getFirstName() + " " + tutor.getLastName());
            map.put("className", s.getClassName());
            map.put("professor", s.getProfessor());
            map.put("classMeetingTimes", s.getClassMeetingTimes());
            return map;
        }).toList();

        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        model.addAttribute("schedule", schedule);
        return "si-schedule";
    }

    @GetMapping("/schedules/drop-in")
    public String dropInSchedule(Model model) {
        return populateGridModel(model, "Drop-in", "drop-in-schedule");
    }

    @GetMapping("/schedules/math-lab")
    public String mathLabSchedule(Model model) {
        return populateGridModel(model, "Math Lab", "ben-frank-math-lab");
    }

    @GetMapping("/schedules/SSC")
    public String sscSchedule(Model model) {
        return populateGridModel(model, "SSC", "ssc-math-lab");
    }

    //instead of repeating same thing for drop-in, math lab and ssc for schedule view
    private String populateGridModel(Model model, String type, String viewName) {
        var sessions = sessionService.getSessionsByType(type);
        List<LocalTime> times = sessionService.generateTimes();

        Set<Tutor> activeTutors = sessions.stream()
                .map(s -> s.getSessionID().getTutor())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        model.addAttribute("times", times);
        model.addAttribute("activeTutors", activeTutors);
        model.addAttribute("schedule", sessionService.fillInSessions(sessions, times));
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        return viewName;
    }
}
