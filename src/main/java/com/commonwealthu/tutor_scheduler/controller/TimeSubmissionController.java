package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TimeSubmissionController {

    @PostMapping("/addTimes")
    public String addTimes(@RequestParam("day") char day, @RequestParam("start") double start,
                           @RequestParam("end") double end) {
        //Session submitted = new Session(new SessionID(/*tutor id*/, day, start), end);
        return "time-submission-edited"; //subject to change
    }

    @PostMapping("/review")
    public String reviewTimes() {
        return "time-sumbit-confirm";
    }


    //final confirm button after all sessions shown
}
