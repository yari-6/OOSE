package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TutorRepository extends JpaRepository<Tutor, String> {
    // Default Hibernate implementation returns Optional<Tutor>
    List<Tutor> findByTypeNotOrderByFirstNameAsc(String type);

    // Check the courses a tutor offers and return the tutors that offer the specific course
    @Query("SELECT DISTINCT t FROM Tutor t JOIN t.coursesOffered c " +
            "WHERE ((c.courseID.courseSubject = :subject AND c.courseID.courseNumber = :number) " +
            "OR UPPER(c.courseTitle) LIKE UPPER(CONCAT('%', :title, '%'))) " +
            "AND t.type != 'Admin' " +
            "ORDER BY t.firstName ASC")
    List<Tutor> findTutorsByCourse(@Param("subject") String subject, @Param("number") int number, @Param("title") String title);

}