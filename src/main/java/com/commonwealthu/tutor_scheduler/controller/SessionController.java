package com.commonwealthu.tutor_scheduler.controller;


import com.commonwealthu.tutor_scheduler.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;


import javax.annotation.processing.Generated;

public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {this.sessionService = sessionService;}

    //running under the assumption that the tutoring types are stored as tutors; subject to change
    //I believe the error is that the arguments are not valid tutor IDs, but I can't properly test it yet
    @GetMapping("/schedules/SI")
    public String siSchedule(Model model) {
        model.addAttribute("SIschedule", sessionService.getSessionsByTutor(si));
        return "SI-schedule";
    }

    @GetMapping("schedules/drop-in")
    public String dropInSchedule(Model model) {
        model.addAttribute("dropinSchedule", sessionService.getSessionsByTutor(dropIn));
        return "drop-in-schedule";
    }

    @GetMapping("schedules/math-lab")
    public String bfMathLabSchedule(Model model) {
        model.addAttribute("BFmathLabSchedule", sessionService.getSessionsByTutor(bfMathLab));
        return "BF-math-lab";
    }

    @GetMapping("schedules/SSC")
    public String SSCSchedule(Model model) {
        model.addAttribute("SSCSchedule", sessionService.getSessionsByTutor(sscMathLab));
        return "SSC-schedule";
    }
}
