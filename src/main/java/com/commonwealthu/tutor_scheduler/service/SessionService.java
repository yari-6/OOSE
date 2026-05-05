package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.dto.ScheduleInfo;
import com.commonwealthu.tutor_scheduler.dto.SIScheduleRequest;
import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.SessionRepository;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.commonwealthu.tutor_scheduler.entity.SystemSetting;
import com.commonwealthu.tutor_scheduler.repository.SystemSettingRepository;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepo;
    private final TutorRepository tutorRepo;
    private final SystemSettingRepository settingRepo;

    public SessionService(SessionRepository sessionRepo,
                          TutorRepository tutorRepo,
                          SystemSettingRepository settingRepo) {
        this.sessionRepo = sessionRepo;
        this.tutorRepo = tutorRepo;
        this.settingRepo = settingRepo;
    }

    public boolean isSubmissionWindowOpen() {
        return settingRepo.findById("SUBMISSION_WINDOW_OPEN")
                .map(SystemSetting::isValue)
                .orElse(false);
    }

    @Transactional
    public void toggleSubmissionWindow(boolean open) {
        SystemSetting setting = settingRepo.findById("SUBMISSION_WINDOW_OPEN")
                .orElse(new SystemSetting("SUBMISSION_WINDOW_OPEN", false));
        setting.setValue(open);
        settingRepo.save(setting);
    }

    @Transactional
    public void replaceSchedule(Tutor tutor, Set<Session> newSessions) {
        if (!isSubmissionWindowOpen() && !tutor.isAdmin()) {
            throw new IllegalStateException("The submission window is currently closed. Changes cannot be saved.");
        }

        Set<Session> currentSchedule = sessionRepo.findBySessionID_Tutor(tutor);

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

    public List<Session> getSessionsByLocationContaining(String keyword) {
        return sessionRepo.findByLocationContaining(keyword);
    }

    public List<Session> getAllSessions() { return sessionRepo.findAll(); }

    public Set<Session> getSessionsByTutor(Tutor tutor) {return sessionRepo.findBySessionID_Tutor(tutor);}

    public Set<Session> getSessionsByType(String type) { return sessionRepo.findByTutoringType(type); }

    // Take in a tutor's submitted sessions (saved or for confirmation) and a list of the times on the display grid
    // and returns a map storing the day + time and the tutors working + display color


    public HashMap<String, ScheduleInfo> fillInSessions(Collection<Session> sessions,
                                                        List<LocalTime> times) {
        HashMap<String, ScheduleInfo> timeMap = new HashMap<>();
        for (Session s: sessions) {
            String rawDay = s.getSessionID().getDay();
            if (rawDay == null) continue;

            // Normalize day to M, T, W, R, F (COULD CHANGE THIS to just string)
            String dayCode = normalizeDay(rawDay);

            LocalTime start = s.getSessionID().getTime();
            LocalTime end = (s.getEndTime() != null) ? s.getEndTime() : start.plusMinutes(50);
            String name = s.getSessionID().getTutor().getFirstName() + " " + s.getSessionID().getTutor().getLastName();
            String tutorID = s.getSessionID().getTutor().getTutorID();
            String color = ColorService.getColor(tutorID);

            // Check if the grid time falls within session time range
            for (LocalTime t: times) {
                if (!t.isBefore(start) && t.isBefore(end)) {
                    String timeStr = t.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                    String key = dayCode + " " + timeStr;

                    if (timeMap.containsKey(key)) {
                        timeMap.get(key).addTutor(name, tutorID, color);
                        timeMap.get(key).setLocation(s.getLocation());
                        timeMap.get(key).setEndTime(s.getEndTime());
                    } else {
                        ScheduleInfo display = new ScheduleInfo();
                        display.addTutor(name, tutorID, color);
                        display.setLocation(s.getLocation());
                        display.setEndTime(s.getEndTime());
                        timeMap.put(key, display);
                    }
                }
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

    public String normalizeDay(String rawDay) {
        if (rawDay == null) {
            return "";
        }
        String dayCode = rawDay.trim().toUpperCase();
        if (dayCode.startsWith("MON")) return "M";
        else if (dayCode.startsWith("TUE")) return "T";
        else if (dayCode.startsWith("WED")) return "W";
        else if (dayCode.startsWith("THU")) return "R";
        else if (dayCode.startsWith("FRI")) return "F";
        return (!dayCode.isEmpty()) ? dayCode.substring(0, 1) : "";
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


    //Drop in rules for Learning Center
    public void validateDropInConstraints(Session newSess) {
        SessionID id = newSess.getSessionID();
        if (id == null || id.getTutor() == null) return;

        LocalTime start = id.getTime();
        LocalTime end = newSess.getEndTime();
        String day = id.getDay();
        Tutor tutor = id.getTutor();

        //End after Start
        if (end != null && !end.isAfter(start)) {
            throw new IllegalStateException("End time (" + end + ") must be after start time (" + start + ") on " + day);
        }

        //No double sessions
        boolean isTutorOverloaded = sessionRepo.findBySessionID_Tutor(tutor).stream()
                .anyMatch(existing ->
                        existing.getSessionID().getDay().equals(day) &&
                                start.isBefore(existing.getEndTime()) &&
                                end.isAfter(existing.getSessionID().getTime())
                );

        if (isTutorOverloaded) {
            throw new IllegalStateException("Double-Entry: You are already scheduled for a session during this timeframe.");
        }

        //No more than two tutors at a time
        if (!"Drop-in".equalsIgnoreCase(tutor.getType())) return;

        String currentTutorId = tutor.getTutorID();

        List<Session> overlaps = sessionRepo.findOverlappingSessions(day, start, end).stream()
                .filter(s -> "Drop-in".equalsIgnoreCase(s.getSessionID().getTutor().getType()))
                .filter(s -> !s.getSessionID().getTutor().getTutorID().equals(currentTutorId))
                .toList();

        // Capacity Check
        if (overlaps.size() >= 2) {
            throw new IllegalStateException("The Learning Center Drop-in is at capacity for " + start + " on " + day);
        }

        // One person at a time per major subject area
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

    public String autoAssignRoom(String day, LocalTime start, LocalTime end) {
        if (!sessionRepo.existsByRoomConflict(day, start, end, "Soltz 105")) return "Soltz 105";
        if (!sessionRepo.existsByRoomConflict(day, start, end, "SSC 021")) return "PENDING: Admin Assignment (SSC 021 Available)";
        return "PENDING: Conflict (No Standard Rooms Available)";
    }

    @Transactional
    public void updateSessionLocation(String tutorId, String day, String time, String newRoom) {
        LocalTime startTime = LocalTime.parse(time);

        // find session and end time for room check
        tutorRepo.findById(tutorId).ifPresent(tutor -> {
            SessionID id = new SessionID(tutor, day, startTime);
            sessionRepo.findById(id).ifPresent(session -> {

                // Is entered room booked
                if (sessionRepo.existsByRoomConflict(day, startTime, session.getEndTime(), newRoom)) {
                    throw new IllegalStateException("Room Conflict: " + newRoom + " is already occupied on " + day + " at " + time);
                }

                session.setLocation(newRoom);
                sessionRepo.save(session);
            });
        });
    }

    public List<SessionWithLocation> assignRoomsToSessions(SIScheduleRequest request) {
        List<SessionWithLocation> sessionsWithRooms = new ArrayList<>();

        Set<String> uniqueCheck = new HashSet<>();

        int duration = 50;
        if (request.getPattern() != null && request.getPattern().contains("x")) {
            try { duration = Integer.parseInt(request.getPattern().split("x")[1]); } catch (Exception ignored) {}
        }

        for (SIScheduleRequest.SessionEntry s : request.getSessions()) {
            String day = s.getDay().isEmpty() ? "?" : s.getDay();
            LocalTime start = LocalTime.MIDNIGHT.plusMinutes(s.getStartMinutes());

            String key = day + "@" + start.toString();
            if (!uniqueCheck.add(key)) {
                throw new IllegalStateException("Duplicate session detected for " + s.getDay() + " at " + start);
            }
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
        private String day;
        private LocalTime start;
        private LocalTime end;
        private String location;

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }
        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public void deleteSession(String tutorId, String day, String startTimeStr) {
        Tutor tutor = tutorRepo.findById(tutorId).orElse(null);

        if (tutor != null) {
            LocalTime startTime = LocalTime.parse(startTimeStr);
            SessionID id = new SessionID(tutor, day, startTime);
            sessionRepo.deleteById(id);
        }
    }

    @Transactional
    public void adminSaveSession(String tutorId, String day, String startTimeStr, String endTimeStr,
                                 String room, String className, String professor, String meetingTimes) {
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor not found"));

        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        if (!endTime.isAfter(startTime)) {
            throw new IllegalStateException("End time must be after start time.");
        }

        SessionID id = new SessionID(tutor, day, startTime);

        Session session = sessionRepo.findById(id).orElse(new Session(id, endTime));

        session.setLocation(room);
        session.setEndTime(endTime);
        session.setClassName(className != null ? className.trim() : null);
        session.setProfessor(professor != null ? professor.trim() : null);
        session.setClassMeetingTimes(meetingTimes != null ? meetingTimes.trim() : null);

        validateDropInConstraints(session);

        sessionRepo.save(session);
    }
}