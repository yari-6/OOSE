package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TutorService {

    private final TutorRepository tutorRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public TutorService(TutorRepository tutorRepo) {
        this.tutorRepo = tutorRepo;
    }

    public List<Tutor> getAllTutors() {
        return tutorRepo.findAll();
    }

    // Updated to use the default Optional<Tutor> return of the TutorRepository
    // Changed from returning error to just returning null if no tutor is found, exceptions to be added
    public Tutor findTutorByID(String id) {
        return tutorRepo.findById(id).orElse(null);
    }

    public boolean checkLogin(String formTutorID, String formPass) {
        // Find a tutor based on the tutorID entered, if non-existent return false
        Tutor tutor = findTutorByID(formTutorID);
        if (tutor == null) {
            return false;
        }
        return checkPasswords(formPass, tutor.getPass());
    }

    public boolean checkPasswords(String formPass, String pass) {
        // All hashed passwords will begin with $
        if (pass.startsWith("$")) {
            return encoder.matches(formPass, pass);
        }
        return formPass.equals(pass);
    }

    // Returns true if their password is equal to their tutorID (means first login)
    public boolean checkFirstLogin(String tutorID) {
        Tutor tutor = findTutorByID(tutorID);
        if (tutor == null) {
            return false;
        }

        return tutor.getPass().equals(tutorID);
    }

    public void updatePassword(String tutorID, String pass) {
        Tutor tutor = findTutorByID(tutorID);
        if (tutor != null) {
            tutor.setPass(encoder.encode(pass));
            tutorRepo.save(tutor);
        }
    }

}
