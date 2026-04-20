package com.commonwealthu.tutor_scheduler.controller;


import com.commonwealthu.tutor_scheduler.service.SessionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;


import javax.annotation.processing.Generated;

@Controller
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {this.sessionService = sessionService;}

    @GetMapping("/schedules/SI")
    public String siSchedule(Model model) {
        model.addAttribute("SIschedule", sessionService.getSessionsByType("SI"));
        return "SI Schedule";
    }

    @GetMapping("schedules/drop-in")
    public String dropInSchedule(Model model) {
        model.addAttribute("dropinSchedule", sessionService.getSessionsByType("Drop-in"));
        return "drop-in-schedule.html";
    }

    @GetMapping("schedules/math-lab")
    public String bfMathLabSchedule(Model model) {
        model.addAttribute("BFmathLabSchedule", sessionService.getSessionsByType("Math Lab"));
        return "ben-frank-math-lab.html";
    }

    @GetMapping("schedules/SSC")
    public String SSCSchedule(Model model) {
        model.addAttribute("SSCSchedule", sessionService.getSessionsByType("SSC"));
        return "ssc-math-lab.html";
    }
}
