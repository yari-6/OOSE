package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import org.springframework.stereotype.Service;
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

    // Updated to use the default Optional<Tutor> return of the TutorRepository
    // The use of RuntimeException is temporary, error handling will be done in another sprint
    public Tutor findTutorByID(String id) {
        return tutorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor not found"));
    }
}
