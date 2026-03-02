package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Session;
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

    //ways to get sessions:
    //by tutor
    //by tutoring type
    //by day of week?
    //by course offered?
}
