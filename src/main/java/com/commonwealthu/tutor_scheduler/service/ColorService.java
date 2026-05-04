package com.commonwealthu.tutor_scheduler.service;

import java.util.List;

/**
 * Utility class for assigning each tutor a color for the schedule display
 * Named ColorService because I did not want to make a Utility class folder for just the one class
 */
public final class ColorService {

    private static final List<String> COLORS = List.of(
            "#FFD1DC", "#F4978E", "#FF6B6B", "#F08080",
            "#FF8FA3", "#6EC1E4", "#4FA3D1", "#7DB7C9", "#6FCF97",
            "#4CD3A8", "#F4C430", "#E6B566", "#F6A96D", "#FBC3C1",
            "#FFFACD", "#B388EB", "#9D7CC8", "#A78BFA", "#7C83FD", "#F28C38");

    private ColorService() {}

    public static String getColor(String tutorID) {
        int index = Math.floorMod(tutorID.hashCode(), COLORS.size());
        return COLORS.get(index);
    }

}