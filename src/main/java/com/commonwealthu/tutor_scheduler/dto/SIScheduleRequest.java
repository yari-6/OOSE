package com.commonwealthu.tutor_scheduler.dto;

import lombok.Data;
import java.util.List;
import jakarta.validation.constraints.NotBlank;

@Data
public class SIScheduleRequest {

    @NotBlank(message = "SI Leader is required")
    private String siLeader;

    @NotBlank(message = "Class name is required")
    private String className;

    @NotBlank(message = "Professor name is required")
    private String professor;

    @NotBlank(message = "Class meeting times are required")
    private String classMeetingTimes;

    @NotBlank(message = "Session pattern is required")
    private String pattern;

    private List<SessionEntry> sessions;

    @Data
    public static class SessionEntry {
        private String day;
        private int startMinutes;
    }
}