package com.commonwealthu.tutor_scheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TimeSubmissionController {
    //keep a list up here and clear it when the confirm button is hit? will that mess up if 2 people schedule at once?
    //can a cookie keep a list/set?

    @PostMapping("/addTimes")
    public String addTimes(@RequestParam("day") char day, @RequestParam("start") double start,
                           @RequestParam("end") double end) {

        return "time-submission"; //subject to change
    }

    //final confirm button after all sessions shown
}
