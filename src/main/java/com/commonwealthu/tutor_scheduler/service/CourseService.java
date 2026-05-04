package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.entity.Course;
import com.commonwealthu.tutor_scheduler.entity.CourseID;
import com.commonwealthu.tutor_scheduler.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepo;

    public CourseService(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    public List<Course> getAllCourses() {
        return courseRepo.findAllByOrderByCourseID_CourseSubjectAscCourseID_CourseNumberAsc();
    }

    public Course findCourseByCompositeID(String subject, String number) {
        try {
            int numValue = Integer.parseInt(number.trim());
            CourseID id = new CourseID(subject.toUpperCase().trim(), numValue);
            return courseRepo.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void createNewCourse(String subject, String number, String title) {
        int cleanNumber = Integer.parseInt(number.trim());
        CourseID id = new CourseID(subject.toUpperCase().trim(), cleanNumber);

        Course newCourse = new Course(id, title.trim());
        courseRepo.save(newCourse);
    }

    @Transactional
    public void deleteCourse(String subject, String number) {
        int cleanNumber = Integer.parseInt(number.trim());
        String cleanSubject = subject.toUpperCase().trim();
        CourseID id = new CourseID(cleanSubject, cleanNumber);

        courseRepo.deleteTutorCourseLinks(cleanSubject, cleanNumber);
        courseRepo.deleteById(id);
    }

    public Course findCourse(String subject, String number) {
        return findCourseByCompositeID(subject, number);
    }
}
