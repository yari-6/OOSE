package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SessionService {
    private final SessionRepository sessionRepo;

    public SessionService(SessionRepository sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    public List<Session> getAllSessions() {return sessionRepo.findAll(); }

    //god i hope this is it
    //https://stackoverflow.com/questions/49435969/how-to-use-part-of-composite-key-in-jpa-repository-methods
    public List<Session> getSessionsByTutor(Tutor tutor) {return sessionRepo.findBySessionID_Tutor(tutor);}

    //get sessions by tutoring type would require a type class to be able to pass to the method
    //it may work to have special tutor object for each type of tutoring
    //or it comes from a specific table, but that table is not made
}
