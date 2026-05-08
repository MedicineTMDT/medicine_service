package com.ryo.identity.dto.request;

import com.ryo.identity.constant.*;
import com.ryo.identity.dto.MedicationSchedule;

import java.time.LocalDateTime;
import java.util.List;

public record IntakeRequest(
        String drugName,            // Celebrex
        String drugId,              // 128
        Integer total,              // 7

        DosageUnit unit,            // mg
        Integer quantitative,       // 700
        MedicineForm medicineForm,  // Viên/V
        Usage usage,                // ORAL( Uống )

        List<MedicationSchedule> timingList,              // MORNING - 1

        List<Note> noteList         // [BEFORE_MEAL]
) {
}
