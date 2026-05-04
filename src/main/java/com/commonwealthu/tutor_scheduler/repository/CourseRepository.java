package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Course;
import com.commonwealthu.tutor_scheduler.entity.CourseID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, CourseID> {
    List<Course> findAllByOrderByCourseID_CourseSubjectAscCourseID_CourseNumberAsc();

    @Modifying
    @Query(value = """
            DELETE FROM "Tutor_Courses"
            WHERE "CourseSubject" = :subject AND "CourseNumber" = :number
            """, nativeQuery = true)
    void deleteTutorCourseLinks(@Param("subject") String subject, @Param("number") int number);
}
