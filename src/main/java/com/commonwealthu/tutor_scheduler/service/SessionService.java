package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.dto.SIScheduleRequest;
import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.SessionRepository;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepo;
    private final TutorRepository tutorRepo;

    public SessionService(SessionRepository sessionRepo, TutorRepository tutorRepo) {
        this.sessionRepo = sessionRepo;
        this.tutorRepo = tutorRepo;
    }

    public List<Session> getSessionsByLocationContaining(String keyword) {
        return sessionRepo.findByLocationContaining(keyword);
    }

    public List<Session> getAllSessions() { return sessionRepo.findAll(); }

    //god i hope this is it
    //https://stackoverflow.com/questions/49435969/how-to-use-part-of-composite-key-in-jpa-repository-methods
    public Set<Session> getSessionsByTutor(Tutor tutor) {return sessionRepo.findBySessionID_Tutor(tutor);}

    //get sessions by tutoring type would require a type class to be able to pass to the method
    //it may work to have special tutor object for each type of tutoring
    //or it comes from a specific table, but that table is not made
    public Set<Session> getSessionsByType(String type) { return sessionRepo.findByTutoringType(type); }

    // Take in a tutor's submitted sessions and the times available from the schedule grid, mark the times that
    // need to be filled in on the schedule grid display
    // This method could be updated in the future to not use String keys, but they are working for now
    public HashMap<String, Boolean> fillInSessions(Set<Session> sessions, List<LocalTime> times) {
        HashMap<String, Boolean> timeMap = new HashMap<>();
        for (Session s: sessions) {
            char day = s.getSessionID().getDay();
            LocalTime start = s.getSessionID().getTime();
            LocalTime end = s.getEndTime();
            // Check if the grid time falls within session time range
            for (LocalTime t: times) {
                if (!t.isBefore(start) && t.isBefore(end))
                    timeMap.put(day + " " + t, true);
            }
        }
        return timeMap;
    }

    public List<LocalTime> generateTimes() {
        List<LocalTime> times = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);
        LocalTime current = start;
        while (current.isBefore(end)) {
            times.add(current);
            current = current.plusMinutes(30);
        }
        return times;
    }

    // addedTimes represent times added during the time submission, not actual times saved to the db
    // Error for unchecked cast seems to be unavoidable because of HttpSession always returning an object
    @SuppressWarnings("unchecked")
    public Set<Session> getAddedTimes(HttpSession browserSession) {
        Set<Session> addedTimes = (Set<Session>) browserSession.getAttribute("addedTimes");
        if (addedTimes == null) {
            addedTimes = new HashSet<>();
            browserSession.setAttribute("addedTimes", addedTimes);
        }
        return addedTimes;
    }


    @Transactional
    public void saveSession(Session session) {
        validateDropInConstraints(session);
        sessionRepo.save(session);
    }

    @Transactional
    public void saveAllTimes(Set<Session> addedTimes) {
        for (Session s : addedTimes) {
            saveSession(s);
        }
    }

    /**
     * Enforces Drop-in Hub capacity and subject conflict rules.
     */
    public void validateDropInConstraints(Session newSess) {
        SessionID id = newSess.getSessionID();
        if (id == null || id.getTutor() == null) return;

        Tutor tutor = id.getTutor();
        if (!"Drop-in".equalsIgnoreCase(tutor.getType())) return;

        char day = id.getDay();
        LocalTime start = id.getTime();
        LocalTime end = newSess.getEndTime();
        String currentTutorId = tutor.getTutorID();

        List<Session> overlaps = sessionRepo.findOverlappingSessions(day, start, end).stream()
                .filter(s -> "Drop-in".equalsIgnoreCase(s.getSessionID().getTutor().getType()))
                .filter(s -> !s.getSessionID().getTutor().getTutorID().equals(currentTutorId))
                .toList();

        if (overlaps.size() >= 2) {
            throw new IllegalStateException("Drop-in Hub is at capacity (max 2) for " + start + " on " + day);
        }

        Set<String> newSubjects = tutor.getCoursesOffered().stream()
                .map(c -> c.getCourseID().getCourseSubject())
                .collect(Collectors.toSet());

        for (Session existing : overlaps) {
            boolean hasOverlap = existing.getSessionID().getTutor().getCoursesOffered().stream()
                    .anyMatch(c -> newSubjects.contains(c.getCourseID().getCourseSubject()));

            if (hasOverlap) {
                String name = existing.getSessionID().getTutor().getLastName();
                throw new IllegalStateException("Subject Conflict: " + name + " is already covering those subjects.");
            }
        }
    }

    @Transactional
    public void replaceSchedule(Tutor tutor, Set<Session> newSessions) {
        Set<Session> currentSchedule = sessionRepo.findBySessionID_Tutor(tutor);

        // Remove old sessions that are not part of the update
        currentSchedule.removeIf(old -> newSessions.stream().anyMatch(n -> n.getSessionID().equals(old.getSessionID())));
        sessionRepo.deleteAll(currentSchedule);

        for (Session newSess : newSessions) {
            validateDropInConstraints(newSess);
            sessionRepo.findById(newSess.getSessionID()).ifPresent(existing -> {
                if (existing.getLocation() != null && !existing.getLocation().contains("PENDING")) {
                    newSess.setLocation(existing.getLocation());
                }
            });
        }
        sessionRepo.saveAll(newSessions);
    }

    /* --- ROOM ASSIGNMENT LOGIC --- */

    public String autoAssignRoom(char day, LocalTime start, LocalTime end) {
        if (!sessionRepo.existsByRoomConflict(day, start, end, "Soltz 105")) return "Soltz 105";
        if (!sessionRepo.existsByRoomConflict(day, start, end, "SSC 021")) return "PENDING: Admin Assignment (SSC 021 Available)";
        return "PENDING: Conflict (No Standard Rooms Available)";
    }

    @Transactional
    public void updateSessionLocation(String tutorId, char day, String time, String newRoom) {
        LocalTime startTime = LocalTime.parse(time);
        tutorRepo.findById(tutorId).ifPresent(tutor -> {
            SessionID id = new SessionID(tutor, day, startTime);
            sessionRepo.findById(id).ifPresent(session -> {
                session.setLocation(newRoom);
                sessionRepo.save(session);
            });
        });
    }

    public List<SessionWithLocation> assignRoomsToSessions(SIScheduleRequest request) {
        List<SessionWithLocation> sessionsWithRooms = new ArrayList<>();
        int duration = 50;
        if (request.getPattern() != null && request.getPattern().contains("x")) {
            try { duration = Integer.parseInt(request.getPattern().split("x")[1]); } catch (Exception ignored) {}
        }

        for (SIScheduleRequest.SessionEntry s : request.getSessions()) {
            char day = s.getDay().isEmpty() ? '?' : s.getDay().charAt(0);
            LocalTime start = LocalTime.MIDNIGHT.plusMinutes(s.getStartMinutes());
            LocalTime end = start.plusMinutes(duration);

            SessionWithLocation swl = new SessionWithLocation();
            swl.setDay(day);
            swl.setStart(start);
            swl.setEnd(end);
            swl.setLocation(autoAssignRoom(day, start, end));
            sessionsWithRooms.add(swl);
        }
        return sessionsWithRooms;
    }

    public static class SessionWithLocation {
        private char day;
        private LocalTime start;
        private LocalTime end;
        private String location;

        public char getDay() { return day; }
        public void setDay(char day) { this.day = day; }
        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }
        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}