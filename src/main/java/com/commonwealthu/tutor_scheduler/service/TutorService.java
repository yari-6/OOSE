package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class TutorService {

    private final TutorRepository tutorRepo;

    public TutorService(TutorRepository tutorRepo) {
        this.tutorRepo = tutorRepo;
    }

    public List<Tutor> getAllTutors() {
        return tutorRepo.findAll();
    }

    // Should change to either unwrapping Optional, or handle the null, works for now
    public Tutor findTutorByID(String id) {
        return tutorRepo.findByTutorID(id);
    }
}
