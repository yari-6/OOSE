package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.service.TutorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/tutors")
    public String tutorList(Model model) {
        model.addAttribute("tutors", tutorService.getAllTutors());
        return "tutors-list";
    }

    // Need to add the rating service usage, could be worthwhile to use a method to return all ratings?
    @GetMapping("/tutors/{id}")
    public String tutorProfile(Model model, @PathVariable String id) {
        model.addAttribute("tutor", tutorService.findTutorByID(id));
        return "tutor-profile";
    }

}
