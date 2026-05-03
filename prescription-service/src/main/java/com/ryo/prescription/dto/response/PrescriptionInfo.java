package com.ryo.prescription.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PrescriptionInfo {
    private List<Map<String,Map<String, Object>>> info;
    private List<Map<String, String>> drugInteractionResponseList;
}
