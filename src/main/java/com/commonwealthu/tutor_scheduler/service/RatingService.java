package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.RatingRepository;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepo;
    private final TutorRepository tutorRepo;

    public RatingService (RatingRepository ratingRepo, TutorRepository tutorRepo) {
        this.ratingRepo = ratingRepo;
        this.tutorRepo = tutorRepo;
    }


    public void saveRating(Rating rating, String tutorID) {
        Tutor tutor = tutorRepo.findById(tutorID).orElseThrow();
        rating.setTutor(tutor);
        ratingRepo.save(rating);
    }

    public HashMap<String, Double> getAllRatings(String tutorID) {
        HashMap<String, Double> averages = new HashMap<>();
        List<Rating> ratings = ratingRepo.findAllRatings(tutorID);
        double comm = 0, approach = 0, effLearn = 0, help = 0;
        for (Rating r : ratings) {
            comm += r.getCommunication();
            approach += r.getApproach();
            effLearn += r.getEffLearn();
            help += r.getHelpfulness();
        }

        double avgComm = comm / ratings.size();
        double avgApproach = approach / ratings.size();
        double avgEffLearn = effLearn / ratings.size();
        double avgHelp = help / ratings.size();

        double avgOverall = (avgComm + avgApproach + avgEffLearn + avgHelp) / 4;

        averages.put("comm", avgComm);
        averages.put("approach", avgApproach);
        averages.put("effLearn", avgEffLearn);
        averages.put("help", avgHelp);
        averages.put("overall", avgOverall);

        return averages;
    }


}
