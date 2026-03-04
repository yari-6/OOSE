package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import com.commonwealthu.tutor_scheduler.repository.RatingRepository;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final RatingRepository ratingRepo;

    public RatingService (RatingRepository ratingRepo) {
        this.ratingRepo = ratingRepo;
    }


    public void saveRating(Rating rating) {
        ratingRepo.save(rating);
    }

    // Could possibly combine all methods into one big getRatings, access through some collection?
    public Double getAvgComm(String tutorID) {
        return ratingRepo.findAvgComm(tutorID);
    }

    public Double getAvgApproach(String tutorID) {
        return ratingRepo.findAvgApproach(tutorID);
    }

    public Double getAvgEffLearn(String tutorID) {
        return ratingRepo.findAvgEffLearn(tutorID);
    }

    public Double getAvgHelp(String tutorID) {
        return ratingRepo.findAvgHelp(tutorID);
    }

    public Double getAvgOverall(String tutorID) {
        return ratingRepo.findAvgOverall(tutorID);
    }



}
