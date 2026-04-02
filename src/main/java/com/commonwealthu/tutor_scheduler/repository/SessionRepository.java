package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, SessionID> {
    List<Session> findBySessionID_Tutor(Tutor tutor);

    @Query("SELECT s FROM Session s WHERE s.tutor = (SELECT t FROM Tutor t WHERE t.type = type)")
    List<Session> findByTutoringType(@Param("tutor_type") String type);
}
