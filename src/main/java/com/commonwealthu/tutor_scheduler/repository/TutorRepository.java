package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TutorRepository extends JpaRepository<Tutor, String> {
    // Default Hibernate implementation returns Optional<Tutor>, need to directly return a tutor object or null
    Tutor findByTutorID(String id);
}
