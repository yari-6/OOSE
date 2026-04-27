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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/si")
public class SIScheduleController {

    private final ScheduleService scheduleService;
    private final SessionService sessionService;
    private final TutorService tutorService;

    public SIScheduleController(ScheduleService scheduleService,
                                SessionService sessionService,
                                TutorService tutorService) {
        this.scheduleService = scheduleService;
        this.sessionService = sessionService;
        this.tutorService = tutorService;
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewSchedule(@Valid @RequestBody SIScheduleRequest request, BindingResult bindingResult) {
        // ... (keep validation logic as is)

        List<SessionService.SessionWithLocation> sessionsWithLocations = sessionService.assignRoomsToSessions(request);

        // CRITICAL: Convert complex objects to a simple List of Maps
        List<Map<String, Object>> cleanSessions = sessionsWithLocations.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("day", s.getDay());
            map.put("start", s.getStart().toString()); // Convert LocalTime to String
            map.put("end", s.getEnd().toString());
            map.put("location", s.getLocation());
            return map;
        }).toList();

        Map<String, Object> response = new HashMap<>();
        response.put("siLeader", request.getSiLeader());
        response.put("className", request.getClassName());
        response.put("professor", request.getProfessor());
        response.put("classMeetingTimes", request.getClassMeetingTimes());
        response.put("pattern", request.getPattern());

        // Use the clean list here
        response.put("sessions", cleanSessions);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveSchedule(@RequestBody Map<String, Object> payload, HttpSession browserSession) {
        try {
            String tutorID = (String) browserSession.getAttribute("tutorID");
            if (tutorID == null) {
                return ResponseEntity.status(401).body("Session expired. Please log in again.");
            }

            Tutor tutor = tutorService.findTutorByID(tutorID);

            // 1. Extract the class-level info from the payload (passed from the JS 'data' object)
            String className = (String) payload.get("className");
            String professor = (String) payload.get("professor");
            String classMeetingTimes = (String) payload.get("classMeetingTimes");

            List<Map<String, Object>> sessionsData = (List<Map<String, Object>>) payload.get("sessions");
            Set<Session> sessionsToSave = new HashSet<>();

            for (Map<String, Object> s : sessionsData) {
                char day = s.get("day").toString().charAt(0);
                LocalTime start = LocalTime.parse((String) s.get("start"));
                LocalTime end = LocalTime.parse((String) s.get("end"));
                String room = (String) s.get("location");

                if (room == null || room.isEmpty()) {
                    room = "PENDING: Needs Assignment";
                }

                SessionID id = new SessionID(tutor, day, start);
                Session sessionEntity = new Session(id, end);
                sessionEntity.setLocation(room);

                // 2. Set the new fields on the entity before adding to the set
                sessionEntity.setClassName(className);
                sessionEntity.setProfessor(professor);
                sessionEntity.setClassMeetingTimes(classMeetingTimes);

                sessionsToSave.add(sessionEntity);
            }

            sessionService.replaceSchedule(tutor, sessionsToSave);
            return ResponseEntity.ok(Map.of("message", "Success"));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}