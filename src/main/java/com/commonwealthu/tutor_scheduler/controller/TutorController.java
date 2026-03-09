package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.service.RatingService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TutorController {

    private final TutorService tutorService;
    private final RatingService ratingService;

    public TutorController(TutorService tutorService, RatingService ratingService) {
        this.tutorService = tutorService;
        this.ratingService = ratingService;
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

    // Uses getAllRatings because rating information is never displayed in individual categories, all categories
    // will be displayed at once every time
    @GetMapping("/tutors/{id}")
    public String tutorProfile(Model model, @PathVariable String id) {
        model.addAttribute("tutor", tutorService.findTutorByID(id));
        model.addAttribute("ratings", ratingService.getAllRatings(id));
        return "tutor-profile";
    }

    @PostMapping("/tutors/{id}/rate")
    public String submitRating(@ModelAttribute("rating") Rating rating, @PathVariable String id) {
        ratingService.saveRating(rating, id);
        return "redirect:/tutors/" + id;
    }

}
