package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.dto.NewPassword;
import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.dto.TutorLogin;
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
    public String home(HttpSession session,
                       @RequestParam(value = "mode", required = false) String mode,
                       Model model) {

        String tutorID = (String) session.getAttribute("tutorID");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (tutorID == null) {
            model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
            return "front-page";
        }

        if (isAdmin != null && isAdmin) {
            if ("standard".equals(mode)) {
                model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
                return "front-page";
            }
            return "front-page";
        }

        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        return "front-page";
    }

    // All tutors will display if no search has been made
    @GetMapping("/tutors")
    public String tutorList(Model model, @RequestParam(required = false) String courseSearch) {
        if (courseSearch == null || courseSearch.trim().isEmpty()) {
            List<Tutor> allTutors = tutorService.getAllTutors();
            model.addAttribute("tutors", allTutors);
        } else {
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

        if (tutor == null) return "redirect:/tutors";

        if (tutor.isAdmin()) {
            return "redirect:/tutors";
        }

        List<LocalTime> times = sessionService.generateTimes();
        model.addAttribute("tutor", tutor);
        model.addAttribute("ratings", ratingService.getAllRatings(id));
        model.addAttribute("times", times);
        model.addAttribute("schedule", sessionService.fillInSessions(sessionService.getSessionsByTutor(tutor), times));
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());

        Boolean userIsAdmin = (Boolean) session.getAttribute("isAdmin");
        model.addAttribute("userIsAdmin", userIsAdmin != null && userIsAdmin);

        return "tutor-profile";
    }

    //similar to above but only used for redirecting to the tutors profile
    @GetMapping("/profile")
    public String myProfile(HttpSession session) {
        String tutorID = (String) session.getAttribute("tutorID");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        if (tutorID == null) {
            return "redirect:/sign-in";
        }

        if (isAdmin != null && isAdmin) {
            return "redirect:/admin/dashboard";
        }

        return "redirect:/tutors/" + tutorID;
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

        if (bindingResult.hasErrors() ||
                !tutorService.checkLogin(loginTutor.getTutorID(), loginTutor.getPass())) {
            return "sign-in";
        }

        Tutor tutor = tutorService.findTutorByID(loginTutor.getTutorID());

        session.setAttribute("tutorID", loginTutor.getTutorID());
        session.setAttribute("isAdmin", tutor != null && tutor.isAdmin());

        if (tutorService.checkFirstLogin(loginTutor.getTutorID())) {
            return "redirect:/set-new-password";
        }
        return "redirect:/";
    }

    @GetMapping("/set-new-password")
    public String newPassword(Model model) {
        NewPassword newPassword = new NewPassword();
        model.addAttribute("newPassword", newPassword);
        return "set-new-password";
    }

    @PostMapping("/set-new-password")
    public String handleNewPassword(@Valid @ModelAttribute("newPassword") NewPassword newPassword,
                                    BindingResult bindingResult, HttpSession browserSession) {
        String tutorID = (String) browserSession.getAttribute("tutorID");

        if (tutorID == null) {
            return "redirect:/sign-in";
        }
        if (!newPassword.getNewPass().equals(newPassword.getConfirmPass())) {
            bindingResult.rejectValue("confirmPass", "error.confirmPass", "Passwords do not match!");
        }

        if (bindingResult.hasErrors()) {
            return "set-new-password";
        }

        tutorService.updatePassword(tutorID, newPassword.getNewPass());
        return "redirect:/sign-in";
    }

    // lets tutor logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}