package com.commonwealthu.tutor_scheduler.repository;

import com.commonwealthu.tutor_scheduler.entity.Session;
import com.commonwealthu.tutor_scheduler.entity.SessionID;
import com.commonwealthu.tutor_scheduler.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface SessionRepository extends JpaRepository<Session, SessionID> {
    Set<Session> findBySessionID_Tutor(Tutor tutor);

    @Query("SELECT s FROM Session s WHERE s.sessionID.day = :day " +
            "AND s.sessionID.time < :endTime " +
            "AND s.endTime > :startTime")
    List<Session> findOverlappingSessions(@Param("day") char day,
                                          @Param("startTime") LocalTime startTime,
                                          @Param("endTime") LocalTime endTime);

    //prevent duplicate sessions
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.sessionID.tutor = :tutor " +
            "AND s.sessionID.day = :day " +
            "AND s.sessionID.time < :endTime " +
            "AND s.endTime > :startTime")
    boolean existsByTutorConflict(@Param("tutor") Tutor tutor,
                                  @Param("day") char day,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime);

    //prevent double assigned rooms
    @Query("SELECT COUNT(s) > 0 FROM Session s WHERE s.sessionID.day = :day " +
            "AND s.location = :room " +
            "AND s.sessionID.time < :endTime " +
            "AND s.endTime > :startTime")
    boolean existsByRoomConflict(@Param("day") char day,
                                 @Param("startTime") LocalTime startTime,
                                 @Param("endTime") LocalTime endTime,
                                 @Param("room") String room);

    @Query("SELECT s FROM Session s WHERE s.sessionID.tutor.type = :tutorType")
    Set<Session> findByTutoringType(@Param("tutorType") String tutorType);

    List<Session> findByLocationContaining(String keyword);
}
