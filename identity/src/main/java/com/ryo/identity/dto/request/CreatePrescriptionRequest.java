package com.ryo.identity.dto.request;

import java.time.LocalDate;
import java.util.List;

public record CreatePrescriptionRequest(
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        List<IntakeRequest> intakes
) {}