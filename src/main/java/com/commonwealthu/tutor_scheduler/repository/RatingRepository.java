package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface RatingRepository extends JpaRepository<Rating, Long> {
    // Updated to bring old methods back, I thought they were all incorrect because of an error saying multiple
    // numbers returned, but it was really just the overall because there was not an avg function on the outside

    @Query("SELECT AVG(r.communication) FROM Rating r where r.tutor.tutorID = :tutorID")
    Double findAvgComm(@Param("tutorID") String tutorID);

    @Query("SELECT AVG(r.approach) FROM Rating r where r.tutor.tutorID = :tutorID")
    Double findAvgApproach(@Param("tutorID") String tutorID);

    @Query("SELECT AVG(r.effLearn) FROM Rating r where r.tutor.tutorID = :tutorID")
    Double findAvgEffLearn(@Param("tutorID") String tutorID);

    @Query("SELECT AVG(r.helpfulness) FROM Rating r where r.tutor.tutorID = :tutorID")
    Double findAvgHelp(@Param("tutorID") String tutorID);

    @Query("SELECT AVG((r.communication + r.approach + r.effLearn + r.helpfulness) / 4.0) " +
            "FROM Rating r WHERE r.tutor.tutorID = :tutorID")
    Double findAvgOverall(@Param("tutorID") String tutorID);

}
