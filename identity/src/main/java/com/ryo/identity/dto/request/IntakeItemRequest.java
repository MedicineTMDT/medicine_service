package com.ryo.identity.dto.request;

public record IntakeItemRequest(
        Integer drugId,
        Integer quantity
) {}