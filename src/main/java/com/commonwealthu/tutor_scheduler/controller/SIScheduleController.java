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
    public String showBuilder(HttpSession session, Model model) {
        String tutorID = (String) session.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";

        Tutor tutor = tutorService.findTutorByID(tutorID);
        if (tutor.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("siName", tutor.getFirstName() + " " + tutor.getLastName());
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());
        return "time-submission-SI";
    }

    @PostMapping("/preview")
    public String previewSchedule(@ModelAttribute SIScheduleRequest request,
                                  HttpSession session,
                                  Model model) {
        String tutorID = (String) session.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";

        Tutor tutor = tutorService.findTutorByID(tutorID);

        // --- ADMIN BLOCK ---
        if (tutor.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("siName", tutor.getFirstName() + " " + tutor.getLastName());
        model.addAttribute("isWindowOpen", sessionService.isSubmissionWindowOpen());

        try {
            // no duplicate sessions
            Set<String> checkSet = new HashSet<>();
            for (var s : request.getSessions()) {
                String key = s.getDay() + "-" + s.getStartMinutes();
                if (!checkSet.add(key)) {
                    throw new IllegalStateException("You cannot select the same day and time for multiple sessions.");
                }
            }
            //preview sessions
            List<SessionService.SessionWithLocation> previewSessions = sessionService.assignRoomsToSessions(request);

            for (var ps : previewSessions) {
                Session temp = new Session(new SessionID(tutor, ps.getDay(), ps.getStart()), ps.getEnd());
                sessionService.validateDropInConstraints(temp);
            }

            model.addAttribute("sessions", previewSessions);
            model.addAttribute("info", request);
            session.setAttribute("siPreview", previewSessions);
            session.setAttribute("siClassInfo", request);

        } catch (IllegalStateException e) {
            //avoid duplicate database entries
            model.addAttribute("error", e.getMessage());
            model.addAttribute("info", request);
            return "time-submission-SI";
        }

        return "time-submission-SI";
    }

    @PostMapping("/confirm")
    public String confirmSchedule(HttpSession session) {
        String tutorID = (String) session.getAttribute("tutorID");
        List<SessionService.SessionWithLocation> preview = (List<SessionService.SessionWithLocation>) session.getAttribute("siPreview");
        SIScheduleRequest info = (SIScheduleRequest) session.getAttribute("siClassInfo");

        if (tutorID == null || preview == null || info == null) {
            return "redirect:/si-builder";
        }
        Tutor tutor = tutorService.findTutorByID(tutorID);

        if (!sessionService.isSubmissionWindowOpen() && (tutor == null || !tutor.isAdmin())) {
            return "redirect:/si-builder";
        }

        if (tutor != null && tutor.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        Set<Session> sessionsToSave = new HashSet<>();
        for (var s : preview) {
            SessionID id = new SessionID(tutor, s.getDay(), s.getStart());
            Session entity = new Session(id, s.getEnd());
            entity.setLocation(s.getLocation());
            entity.setClassName(info.getClassName());
            entity.setProfessor(info.getProfessor());
            entity.setClassMeetingTimes(info.getClassMeetingTimes());
            sessionsToSave.add(entity);
        }

        sessionService.replaceSchedule(tutor, sessionsToSave);

        session.removeAttribute("siPreview");
        session.removeAttribute("siClassInfo");

        return "redirect:/tutors/" + tutorID;
    }
}