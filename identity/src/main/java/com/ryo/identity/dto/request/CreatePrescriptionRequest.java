package com.ryo.identity.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record CreatePrescriptionRequest(
    String name,
    String description,

    String userId, // thông tin của người kê thuốc
    String patientEmailAddress,
    LocalDate startDate,

    String message,
    String diagnosisNote,

    Map<String, Object> info,
    List<IntakeRequest> intakes
) {}