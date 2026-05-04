package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.CourseID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.CourseRepository;
import com.commonwealthu.tutor_scheduler.repository.RatingRepository;
import com.commonwealthu.tutor_scheduler.repository.SessionRepository;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TutorService {

    private final TutorRepository tutorRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final CourseRepository courseRepo;
    private final SessionRepository sessionRepo;
    private final RatingRepository ratingRepo;

    public TutorService(TutorRepository tutorRepo,
                        CourseRepository courseRepo,
                        SessionRepository sessionRepo,
                        RatingRepository ratingRepo) {
        this.tutorRepo = tutorRepo;
        this.courseRepo = courseRepo;
        this.sessionRepo = sessionRepo;
        this.ratingRepo = ratingRepo;
    }

    public List<Tutor> getAllTutors() {
        return tutorRepo.findByTypeNotOrderByFirstNameAsc("Admin");
    }
    // Updated to use the default Optional<Tutor> return of the TutorRepository
    // Changed from returning error to just returning null if no tutor is found, exceptions to be added
    public Tutor findTutorByID(String id) {
        Tutor t = tutorRepo.findById(id).orElse(null);

        if (t != null) {
            t.getCoursesOffered().size();
            t.getSchedule().size();
        }

        return t;
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

    public List<Tutor> getTutorsForCourse(String courseSearch) {
        if (courseSearch == null || courseSearch.trim().isEmpty()) {
            return List.of();
        }

        String searchTrimmed = courseSearch.trim();
        String[] searchParts = searchTrimmed.split(" ");

        String subject = "";
        int number = -1;

        if (searchParts.length >= 2) {
            try {
                subject = searchParts[0].toUpperCase();
                number = Integer.parseInt(searchParts[1]);
            } catch (NumberFormatException e) {
            }
        }

        return tutorRepo.findTutorsByCourse(subject, number, searchTrimmed);
    }


    public void updateProfilePicture(String tutorID, String profilePicture) {
        Tutor tutor = findTutorByID(tutorID);
        if (tutor != null) {
            tutor.setProfilePicture(profilePicture);
            tutorRepo.save(tutor);
        }
    }

    @Transactional
    public void updateTutorCourses(String tutorID, List<String> courseKeys) {
        Tutor tutor = findTutorByID(tutorID);
        if (tutor == null) return;

        tutor.getCoursesOffered().clear();

        if (courseKeys != null && !courseKeys.isEmpty()) {
            for (String key : courseKeys) {
                String[] parts = key.split("-");
                if (parts.length == 2) {
                    try {
                        String subject = parts[0];
                        int number = Integer.parseInt(parts[1]);
                        CourseID id = new CourseID(subject, number);
                        courseRepo.findById(id).ifPresent(tutor.getCoursesOffered()::add);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        tutorRepo.save(tutor);
    }


    public void createTutor(Tutor tutor) {
        if (tutor.getPass() == null || tutor.getPass().isEmpty()) {
            tutor.setPass(tutor.getTutorID());
        }
        tutorRepo.save(tutor);
    }

    @Transactional
    public void deleteTutor(String tutorID) {
        Tutor tutor = findTutorByID(tutorID);
        if (tutor == null) {
            return;
        }

        sessionRepo.deleteBySessionID_Tutor(tutor);
        ratingRepo.deleteByTutor_TutorID(tutorID);
        tutor.getCoursesOffered().clear();
        tutorRepo.save(tutor);
        tutorRepo.delete(tutor);
    }

    public void resetTutorPassword(String tutorID) {
        Tutor tutor = findTutorByID(tutorID);
        if (tutor != null) {
            tutor.setPass(tutor.getTutorID());
            tutorRepo.save(tutor);
        }
    }
}
