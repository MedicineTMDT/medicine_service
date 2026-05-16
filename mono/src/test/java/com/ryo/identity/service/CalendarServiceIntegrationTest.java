package com.ryo.identity.service;

import com.ryo.identity.service.impl.CalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarServiceIntegrationTest extends ServiceIntegrationTestBase {

    @Autowired
    private CalendarService calendarService;

    @Test
    void generateGoogleCalendarLink_shouldEncodeTitleDescriptionAndDates() {
        String result = calendarService.generateGoogleCalendarLink(
                "Morning dose",
                "Drug Name: Paracetamol",
                LocalDateTime.of(2026, 5, 16, 6, 30)
        );

        assertTrue(result.startsWith("https://calendar.google.com/calendar/render?action=TEMPLATE"));
        assertTrue(result.contains("text=Morning+dose"));
        assertTrue(result.contains("details=Drug+Name%3A+Paracetamol"));
        assertTrue(result.contains("dates="));
    }
}
