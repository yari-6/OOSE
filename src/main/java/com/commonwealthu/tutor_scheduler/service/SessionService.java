package com.commonwealthu.tutor_scheduler.service;

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

    //god i hope this is it
    //https://stackoverflow.com/questions/49435969/how-to-use-part-of-composite-key-in-jpa-repository-methods
    public Set<Session> getSessionsByTutor(Tutor tutor) {return sessionRepo.findBySessionID_Tutor(tutor);}

    //get sessions by tutoring type would require a type class to be able to pass to the method
    //it may work to have special tutor object for each type of tutoring
    //or it comes from a specific table, but that table is not made
    public Set<Session> getSessionsByType(String type) { return sessionRepo.findByTutoringType(type); }

    public void saveSession(Session session) {
        sessionRepo.save(session);
    }

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
    public Set<Session> getAddedTimes(HttpSession browserSession) {
        Set<Session> addedTimes = (Set<Session>) browserSession.getAttribute("addedTimes");
        if (addedTimes == null) {
            addedTimes = new HashSet<>();
            browserSession.setAttribute("addedTimes", addedTimes);
        }
        return addedTimes;
    }

    public void saveAllTimes(Set<Session> addedTimes) {
        for (Session s: addedTimes) {
            saveSession(s);
        }
    }

}
