package com.ryo.identity.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.UserCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalendarService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    private static final DateTimeFormatter GOOGLE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public void addEventToGoogleCalendar(
            String accessToken,
            String refreshToken,
            String title,
            String description,
            LocalDateTime start
    ) throws GeneralSecurityException, IOException {

        GoogleCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .setAccessToken(new AccessToken(accessToken, null))
                .build();

        Calendar calendarService = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("YourAppName").build();

        ZonedDateTime zonedStart = start.atZone(ZoneId.systemDefault());
        ZonedDateTime zonedEnd = zonedStart.plusHours(1);

        Event event = new Event()
                .setSummary(title)
                .setDescription(description);

        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(Date.from(zonedStart.toInstant())))
                .setTimeZone(ZoneId.systemDefault().getId()));

        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(Date.from(zonedEnd.toInstant())))
                .setTimeZone(ZoneId.systemDefault().getId()));

        Event createdEvent = calendarService.events().insert("primary", event).execute();
        log.info("Calendar event created: id={}, link={}",
                createdEvent.getId(),
                createdEvent.getHtmlLink());
    }

    public String generateGoogleCalendarLink(
            String title,
            String description,
            LocalDateTime start
    ) {
        ZonedDateTime zonedStart = start.atZone(ZoneId.systemDefault());
        ZonedDateTime zonedEnd = zonedStart.plusHours(1);

        String startFormatted = zonedStart
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(GOOGLE_DATE_FORMAT);
        String endFormatted = zonedEnd
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(GOOGLE_DATE_FORMAT);

        return "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + encode(title)
                + "&details=" + encode(description)
                + "&dates=" + startFormatted + "/" + endFormatted;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}