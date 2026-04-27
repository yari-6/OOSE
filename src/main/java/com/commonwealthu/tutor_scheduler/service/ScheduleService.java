package com.commonwealthu.tutor_scheduler.service;

import com.commonwealthu.tutor_scheduler.dto.SIScheduleRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
public class ScheduleService {

    public List<String> validateAndCalculate(SIScheduleRequest request) {
        List<String> errors = new ArrayList<>();
        int duration = Integer.parseInt(request.getPattern().split("x")[1]);

        for (int i = 0; i < request.getSessions().size(); i++) {
            var s1 = request.getSessions().get(i);
            int end1 = s1.getStartMinutes() + duration;

            for (int j = i + 1; j < request.getSessions().size(); j++) {
                var s2 = request.getSessions().get(j);
                int end2 = s2.getStartMinutes() + duration;

                if (s1.getDay().equals(s2.getDay())) {
                    if (s1.getStartMinutes() < end2 && end1 > s2.getStartMinutes()) {
                        errors.add("Conflict: Session " + (i+1) + " overlaps with " + (j+1));
                    }
                }
            }
        }
        return errors;
    }
}