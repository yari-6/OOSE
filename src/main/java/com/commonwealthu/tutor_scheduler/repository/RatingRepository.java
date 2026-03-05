package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Returns a list of every rating for a tutor, accounts for multiple ratings (past methods didn't do that,
    // not sure how I didn't realize they would need to be lists)
    @Query("SELECT r FROM Rating r WHERE r.tutor.tutorID = :tutorID")
    List<Rating> findAllRatings(@Param("tutorID") String tutorID);

}
