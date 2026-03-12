package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.entity.TutorLogin;
import com.commonwealthu.tutor_scheduler.service.RatingService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
        return "front-page";
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

    // Send the empty tutor object for it to be bind with the form data, then check in the service
    @GetMapping("/sign-in")
    public String signIn(Model model) {
        model.addAttribute("loginTutor", new TutorLogin());
        return "sign-in";
    }

    // ModelAttribute users the empty TutorLogin and binds the form data to it
    @PostMapping("/sign-in")
    public String handleSignIn(@Valid @ModelAttribute("loginTutor") TutorLogin loginTutor,
                               BindingResult bindingResult, Model model) {
        // Checks the form data
        if (bindingResult.hasErrors() ||
                !tutorService.checkLogin(loginTutor.getTutorID(), loginTutor.getPass())) {
            return "sign-in";
        }

        // Does not redirect to a new page so that the tutorID is kept across pages
        if (tutorService.checkFirstLogin(loginTutor.getTutorID())) {
            return "set-new-password"; // No get mapping needed because it is returned within the sign-in
        }

        return "redirect:/";
    }

    @PostMapping("/set-new-password")
    public String handleNewPassword(@ModelAttribute("loginTutor") TutorLogin loginTutor) {
        if (!loginTutor.getNewPass().equals(loginTutor.getConfirmPass())) {
            return "set-new-password";
        }

        tutorService.updatePassword(loginTutor.getTutorID(), loginTutor.getNewPass());

        return "redirect:/sign-in";
    }
}
