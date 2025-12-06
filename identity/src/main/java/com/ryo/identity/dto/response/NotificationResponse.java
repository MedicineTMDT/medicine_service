package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    String message;
    LocalDateTime time;
}
