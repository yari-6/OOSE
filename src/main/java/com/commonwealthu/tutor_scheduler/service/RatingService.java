package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import com.commonwealthu.tutor_scheduler.repository.RatingRepository;
import com.commonwealthu.tutor_scheduler.repository.TutorRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;

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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public HashMap<String, Double> getAllRatings(String tutorID) {
        HashMap<String, Double> averages = new HashMap<>();

        averages.put("comm", round(ratingRepo.findAvgComm(tutorID)));
        averages.put("approach", round(ratingRepo.findAvgApproach(tutorID)));
        averages.put("effLearn", round(ratingRepo.findAvgEffLearn(tutorID)));
        averages.put("help", round(ratingRepo.findAvgHelp(tutorID)));
        averages.put("overall", round(ratingRepo.findAvgOverall(tutorID)));

        return averages;
    }

}
