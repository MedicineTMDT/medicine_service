package com.ryo.identity.constant;

import lombok.Getter;

import java.time.LocalTime;

@Getter
public enum Timing {

    // ===== THEO BUỔI =====
    MORNING("Sáng", "Dùng thuốc vào buổi sáng.",LocalTime.of(6, 30)),
    NOON("Trưa", "Dùng thuốc vào buổi trưa.",LocalTime.of(11, 0)),
    AFTERNOON("Chiều", "Dùng thuốc vào buổi chiều.",LocalTime.of(17, 0)),
    EVENING("Tối", "Dùng thuốc vào buổi tối.",LocalTime.of(20, 0));

    private final String name;
    private final String description;
    private final LocalTime time;

    Timing(String name, String description, LocalTime time) {
        this.name = name;
        this.description = description;
        this.time = time;
    }
}
