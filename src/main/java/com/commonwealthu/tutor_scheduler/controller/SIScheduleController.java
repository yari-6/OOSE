package com.commonwealthu.tutor_scheduler.controller;

import com.commonwealthu.tutor_scheduler.dto.SIScheduleRequest;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.service.SessionService;
import com.commonwealthu.tutor_scheduler.service.TutorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("tutor", tutor);
        model.addAttribute("siLeader", tutor.getFirstName() + " " + tutor.getLastName());

        // Also pass the existing schedule so the JS can pre-fill the builder
        model.addAttribute("existingSchedule", sessionService.getSessionsByTutor(tutor));

        return "time-submission-SI";
    }

    @PostMapping("/preview")
    public String previewSchedule(@ModelAttribute SIScheduleRequest request,
                                  HttpSession session,
                                  Model model) {
        String currentUserID = (String) session.getAttribute("tutorID");
        if (currentUserID == null) return "redirect:/sign-in";

        Tutor currentUser = tutorService.findTutorByID(currentUserID);
        Set<Session> savedSessions = sessionService.getSessionsByTutor(currentUser);

        // 2. Generate the actual session times/locations via Service
        // Note: Ensure assignRoomsToSessions exists in your SessionService
        List<SessionService.SessionWithLocation> previewSessions = sessionService.assignRoomsToSessions(request);

        // 3. Store in Model so Thymeleaf shows the table
        model.addAttribute("tutor", currentUser);
        model.addAttribute("sessions", previewSessions);
        model.addAttribute("info", request);

        // 4. Store in Session so the /confirm method can access it later
        session.setAttribute("siPreview", previewSessions);
        session.setAttribute("siClassInfo", request);

        return "time-submission-SI";
    }

    @PostMapping("/confirm")
    public String confirmSchedule(HttpSession session) {
        String tutorID = (String) session.getAttribute("tutorID");
        List<SessionService.SessionWithLocation> preview = (List<SessionService.SessionWithLocation>) session.getAttribute("siPreview");
        SIScheduleRequest info = (SIScheduleRequest) session.getAttribute("siClassInfo");

        if (tutorID == null || preview == null) {
            return "redirect:/si-builder";
        }

        Tutor tutor = tutorService.findTutorByID(tutorID);
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

        // Clean up
        session.removeAttribute("siPreview");
        session.removeAttribute("siClassInfo");

        return "redirect:/tutors/" + tutorID;
    }
}