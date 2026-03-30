package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.SessionSubmit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TimeSubmissionController {
    @PostMapping("/addTimes")
    public String addTimes(@ModelAttribute SessionSubmit formData) {
        //figure out how to store each time until the end
        return "time-submission"; //subject to change
    }
    //final confirm button after all sessions shown
}
