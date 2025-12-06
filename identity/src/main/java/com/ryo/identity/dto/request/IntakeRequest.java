package com.ryo.identity.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record IntakeRequest(
        LocalDateTime time,
        String status,
        List<IntakeItemRequest> items
) {
}
