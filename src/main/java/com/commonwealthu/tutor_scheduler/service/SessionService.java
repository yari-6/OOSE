package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.ScheduleInfo;
import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.SessionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class SessionService {

    private final SessionRepository sessionRepo;

    public SessionService(SessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    public List<Session> getAllSessions() { return sessionRepo.findAll(); }

    public Set<Session> getSessionsByTutor(Tutor tutor) {return sessionRepo.findBySessionID_Tutor(tutor);}

    public Set<Session> getSessionsByType(String type) { return sessionRepo.findByTutoringType(type); }

    public void saveSession(Session session) {
        sessionRepo.save(session);
    }

    // Take in a tutor's submitted sessions (saved or for confirmation) and a list of the times on the display grid
    // and returns a map storing the day + time and the tutors working + display color
    public HashMap<String, ScheduleInfo> fillInSessions(Set<Session> sessions, List<LocalTime> times) {
        HashMap<String, ScheduleInfo> timeMap = new HashMap<>();
        for (Session s: sessions) {
            char day = s.getSessionID().getDay();
            LocalTime start = s.getSessionID().getTime();
            LocalTime end = s.getEndTime();
            String name = s.getSessionID().getTutor().getFirstName();
            String type = s.getSessionID().getTutor().getType();

            // Check if the grid time falls within session time range
            for (LocalTime t: times) {
                if (!t.isBefore(start) && t.isBefore(end)) {
                    String key = day + " " + t;

                    // Add the second name if one tutor is already on schedule
                    if (timeMap.containsKey(key)) {
                        ScheduleInfo existingTutor = timeMap.get(key);
                        existingTutor.setNames(existingTutor.getNames() + "/" + name);
                    }
                    else {
                        ScheduleInfo display = new ScheduleInfo(name, setDisplayColor(type));
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

    // addedTimes represent times added during the time submission, not actual times saved to the db
    // Error for unchecked cast seems to be unavoidable because of HttpSession always returning an object
    public Set<Session> getAddedTimes(HttpSession browserSession) {
        Set<Session> addedTimes = (Set<Session>) browserSession.getAttribute("addedTimes");
        if (addedTimes == null) {
            addedTimes = new HashSet<>();
            browserSession.setAttribute("addedTimes", addedTimes);
        }
        return addedTimes;
    }

    // Updated to use repo saveAll
    public void saveAllTimes(Set<Session> addedTimes) {
        sessionRepo.saveAll(addedTimes);
    }

    // Set the display color by type, return no color by default
    public String setDisplayColor(String type) {
        return switch (type) {
            case "Drop-in" -> "crimson";
            case "Math Lab" -> "cornflowerblue";
            case "SSC" -> "forestgreen";
            default -> "";
        };
    }

}
