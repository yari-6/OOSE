package com.commonwealthu.tutor_scheduler.controller;


import com.commonwealthu.tutor_scheduler.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.time.LocalTime;
import java.util.List;

@Controller
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {this.sessionService = sessionService;}

    @GetMapping("/schedules/SI")
    public String siSchedule(Model model) {
        model.addAttribute("SIschedule", sessionService.getSessionsByType("SI"));
        return "si-schedule";
    }

    @GetMapping("/schedules/drop-in")
    public String dropInSchedule(Model model) {
        List<LocalTime> times = sessionService.generateTimes();
        model.addAttribute("times", times);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByType("Drop-in"),  times));
        return "drop-in-schedule";
    }

    @GetMapping("/schedules/math-lab")
    public String bfMathLabSchedule(Model model) {
        List<LocalTime> times = sessionService.generateTimes();
        model.addAttribute("times", times);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByType("Math Lab"),  times));
        return "ben-frank-math-lab";
    }

    @GetMapping("/schedules/SSC")
    public String SSCSchedule(Model model) {
        List<LocalTime> times = sessionService.generateTimes();
        model.addAttribute("times", times);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByType("SSC"),  times));
        return "ssc-math-lab";
    }
}
