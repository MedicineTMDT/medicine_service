package com.ryo.prescription.dto.response;

import com.ryo.prescription.dto.request.IntakeRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PrescriptionResponse {
    String name;
    String description;

    String userId; // thông tin của người kê thuốc
    String patientEmailAddress;
    LocalDate startDate;

    String message;
    String diagnosisNote;
    // thong tin lien quan cua don thuoc ( chong chi dinh. chong tuong tac ... )
    Map<String, Object> info;
    List<IntakeRequest> intakes;

    String image;
}
