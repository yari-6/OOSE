package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.dto.SIScheduleRequest;
import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.ScheduleService;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/si-builder")
public class SIScheduleController {

    private final SessionService sessionService;
    private final TutorService tutorService;

    public SIScheduleController(SessionService sessionService, TutorService tutorService) {
        this.sessionService = sessionService;
        this.tutorService = tutorService;
    }

    @GetMapping
    public String showBuilder(@RequestParam(required = false) String targetTutorID,
                              HttpSession session,
                              Model model) {
        String currentUserID = (String) session.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);

        String effectiveTutorID = (currentUser.isAdmin() && targetTutorID != null)
                ? targetTutorID
                : currentUserID;

        Tutor tutor = tutorService.findTutorByID(effectiveTutorID);

        model.addAttribute("tutorCourses", tutor.getCoursesOffered());

        if (currentUser.isAdmin() && targetTutorID == null) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("tutorID", effectiveTutorID);
        model.addAttribute("siName", tutor.getFirstName() + " " + tutor.getLastName());
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        model.addAttribute("isAdmin", currentUser.isAdmin());

        return "time-submission-SI";
    }

    @PostMapping("/preview")
    public String previewSchedule(@ModelAttribute SIScheduleRequest request,
                                  @RequestParam String effectiveTutorID,
                                  HttpSession session,
                                  Model model) {
        String currentUserID = (String) session.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);
        Tutor targetTutor = tutorService.findTutorByID(effectiveTutorID);

        if (!currentUser.isAdmin() && !currentUserID.equals(effectiveTutorID)) {
            return "redirect:/";
        }

        model.addAttribute("siName", targetTutor.getFirstName() + " " + targetTutor.getLastName());
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        model.addAttribute("tutorID", effectiveTutorID);

        try {
            // no duplicate sessions
            Set<String> checkSet = new HashSet<>();
            for (var s : request.getSessions()) {
                String key = s.getDay() + "-" + s.getStartMinutes();
                if (!checkSet.add(key)) {
                    throw new IllegalStateException("You cannot select the same day and time for multiple sessions.");
                }
            }
            List<SessionService.SessionWithLocation> previewSessions = sessionService.assignRoomsToSessions(request);

            //preview sessions
            for (var ps : previewSessions) {
                Session temp = new Session(new SessionID(targetTutor, ps.getDay(), ps.getStart()), ps.getEnd());
                sessionService.validateDropInConstraints(temp);
            }

            model.addAttribute("sessions", previewSessions);
            model.addAttribute("info", request);

            session.setAttribute("siPreview", previewSessions);
            session.setAttribute("siClassInfo", request);
            session.setAttribute("targetTutorID", effectiveTutorID);

        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("info", request);
            return "time-submission-SI";
        }

        return "time-submission-SI";
    }

    @PostMapping("/confirm")
    public String confirmSchedule(HttpSession session) {
        String currentUserID = (String) session.getAttribute("tutorID");
        String targetTutorID = (String) session.getAttribute("targetTutorID");
        List<SessionService.SessionWithLocation> preview = (List<SessionService.SessionWithLocation>) session.getAttribute("siPreview");
        SIScheduleRequest info = (SIScheduleRequest) session.getAttribute("siClassInfo");

        if (currentUserID == null || preview == null || info == null || targetTutorID == null) {
            return "redirect:/si-builder";
        }

        Tutor currentUser = tutorService.findTutorByID(currentUserID);
        Tutor targetTutor = tutorService.findTutorByID(targetTutorID);

        if (!sessionService.isSubmissionWindowOpen() && !currentUser.isAdmin()) {
            return "redirect:/si-builder";
        }

        Set<Session> sessionsToSave = new HashSet<>();
        for (var s : preview) {
            SessionID id = new SessionID(targetTutor, s.getDay(), s.getStart());
            Session entity = new Session(id, s.getEnd());
            entity.setLocation(s.getLocation());
            entity.setClassName(info.getClassName());
            entity.setProfessor(info.getProfessor());
            entity.setClassMeetingTimes(info.getClassMeetingTimes());
            sessionsToSave.add(entity);
        }

        sessionService.replaceSchedule(targetTutor, sessionsToSave);

        session.removeAttribute("siPreview");
        session.removeAttribute("siClassInfo");
        session.removeAttribute("targetTutorID");

        return currentUser.isAdmin() ? "redirect:/admin/dashboard" : "redirect:/tutors/" + targetTutorID;
    }
}