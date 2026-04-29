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
        // Add the full name to the model so the input can find it
        model.addAttribute("siName", tutor.getFirstName() + " " + tutor.getLastName());

        return "time-submission-SI";
    }

    @PostMapping("/preview")
    public String previewSchedule(@ModelAttribute SIScheduleRequest request,
                                  HttpSession session,
                                  Model model) {
        String tutorID = (String) session.getAttribute("tutorID");
        if (tutorID == null) return "redirect:/sign-in";

        Tutor tutor = tutorService.findTutorByID(tutorID);
        model.addAttribute("siName", tutor.getFirstName() + " " + tutor.getLastName());

        // 1. Logic check: Prevent internal overlaps
        Set<String> checkSet = new HashSet<>();
        for (var s : request.getSessions()) {
            String key = s.getDay() + "-" + s.getStartMinutes();
            if (!checkSet.add(key)) {
                model.addAttribute("error", "You cannot select the same day and time for multiple sessions.");
                model.addAttribute("info", request); // Keep their form data filled
                return "time-submission-SI";
            }
        }

        // 2. Generate the actual session times/locations via Service
        // Note: Ensure assignRoomsToSessions exists in your SessionService
        List<SessionService.SessionWithLocation> previewSessions = sessionService.assignRoomsToSessions(request);

        // 3. Store in Model so Thymeleaf shows the table
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