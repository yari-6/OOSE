package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.entity.TutorLogin;
import com.commonwealthu.tutor_scheduler.service.RatingService;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
public class TutorController {

    private final TutorService tutorService;
    private final RatingService ratingService;
    private final SessionService sessionService;

    public TutorController(TutorService tutorService,
                           RatingService ratingService, SessionService sessionService) {
        this.tutorService = tutorService;
        this.ratingService = ratingService;
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String home() {
        return "front-page";
    }

    // All tutors will display if no search has been made
    @GetMapping("/tutors")
    public String tutorList(Model model, @RequestParam(required = false) String courseSearch) {
        // Basically if params are null, do service get all, if params are not call service get tutor courses
        if (courseSearch == null) {
            model.addAttribute("tutors", tutorService.getAllTutors());
        }
        else {
            model.addAttribute("tutors", tutorService.getTutorsForCourse(courseSearch));
        }

        return "tutors-list";
    }

    // Uses getAllRatings because rating information is never displayed in individual categories, all categories
    // will be displayed at once every time
    // Possibly update to be a query parameter rather than embedded in the url
    // Updated to pass times and schedule for the schedule view portion of the profile
    // Could be changed later, because the same set of times has been passed for everything using the schedule
    @GetMapping("/tutors/{id}")
    public String tutorProfile(Model model, @PathVariable String id, HttpSession session) {
        Tutor tutor = tutorService.findTutorByID(id);
        List<LocalTime> times = sessionService.generateTimes();
        model.addAttribute("tutor", tutor);
        model.addAttribute("ratings", ratingService.getAllRatings(id));
        model.addAttribute("times", times);
        model.addAttribute("schedule",
                sessionService.fillInSessions(sessionService.getSessionsByTutor(tutor), times));
        return "tutor-profile";
    }

    //similar to above but only used for redirecting to the tutors profile
    @GetMapping("/profile")
    public String myProfile(HttpSession session, Model model) {

        String tutorID = (String) session.getAttribute("tutorID");

        if (tutorID == null) {
            return "redirect:/sign-in";
        }

        model.addAttribute("tutor", tutorService.findTutorByID(tutorID));
        model.addAttribute("ratings", ratingService.getAllRatings(tutorID));

        return "tutor-profile";
    }

    @PostMapping("/tutors/update-pfp")
    @ResponseBody
    public String updateProfilePicture(@RequestBody Map<String, String> body,
                                       HttpSession session) {

        String tutorId = (String) session.getAttribute("tutorID");
        if (tutorId == null) return "not_logged_in";

        String profilePicture = body.get("profilePicture");

        tutorService.updateProfilePicture(tutorId, profilePicture);

        return profilePicture;
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
                               BindingResult bindingResult,
                               HttpSession session) {

        // 1. Validate credentials
        if (bindingResult.hasErrors() ||
                !tutorService.checkLogin(loginTutor.getTutorID(), loginTutor.getPass())) {
            return "sign-in";
        }

        // 2. Fetch the tutor object NOW (This solves the "Cannot resolve symbol" error)
        Tutor tutor = tutorService.findTutorByID(loginTutor.getTutorID());

        // 3. Create session attributes using the 'tutor' variable we just created
        session.setAttribute("tutorID", loginTutor.getTutorID());
        session.setAttribute("isAdmin", tutor != null && tutor.isAdmin());

        // 4. Check for first login
        if (tutorService.checkFirstLogin(loginTutor.getTutorID())) {
            return "set-new-password";
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

    // lets tutor logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}