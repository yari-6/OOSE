package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
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
    private final TutorService tutorService;

    public SessionController(SessionService sessionService, TutorService tutorService) {
        this.sessionService = sessionService;
        this.tutorService = tutorService;
    }

    @GetMapping("/schedules/SI")
    public String siSchedule(Model model) {
        var sessions = sessionService.getSessionsByType("SI");

        List<Map<String, Object>> schedule = sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("tutorId", s.getSessionID().getTutor().getTutorID());
            map.put("day", String.valueOf(s.getSessionID().getDay()));
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            map.put("start", s.getSessionID().getTime().format(timeFormatter));
            map.put("end", s.getEndTime().format(timeFormatter));
            map.put("location", s.getLocation());
            map.put("siLeader", s.getSessionID().getTutor().getFirstName() + " " + s.getSessionID().getTutor().getLastName());
            map.put("className", s.getClassName());
            map.put("professor", s.getProfessor());
            map.put("classMeetingTimes", s.getClassMeetingTimes());
            return map;
        }).toList();

        model.addAttribute("schedule", schedule);
        return "si-schedule";
    }

    @GetMapping("/schedules/drop-in")
    public String dropInSchedule(Model model) {
        var sessions = sessionService.getSessionsByType("Drop-in");
        List<LocalTime> times = sessionService.generateTimes();

        Set<Tutor> activeTutors = sessions.stream()
                .map(s -> s.getSessionID().getTutor())
                .collect(Collectors.toSet());

        model.addAttribute("times", times);
        model.addAttribute("activeTutors", activeTutors);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByType("Drop-in"),  times));
        return "drop-in-schedule";
    }

    @GetMapping("/schedules/math-lab")
    public String bfMathLabSchedule(Model model) {
        var sessions = sessionService.getSessionsByType("Math Lab");
        List<LocalTime> times = sessionService.generateTimes();

        Set<Tutor> activeTutors = sessions.stream()
                .map(s -> s.getSessionID().getTutor())
                .collect(Collectors.toSet());

        model.addAttribute("times", times);
        model.addAttribute("activeTutors", activeTutors);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByType("Math Lab"),  times));
        return "ben-frank-math-lab";
    }

    @GetMapping("/schedules/SSC")
    public String SSCSchedule(Model model) {
        var sessions = sessionService.getSessionsByType("SSC");
        List<LocalTime> times = sessionService.generateTimes();

        Set<Tutor> activeTutors = sessions.stream()
                .map(s -> s.getSessionID().getTutor())
                .collect(Collectors.toSet());

        model.addAttribute("times", times);
        model.addAttribute("activeTutors", activeTutors);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByType("SSC"),  times));
        return "ssc-math-lab";
    }
}
