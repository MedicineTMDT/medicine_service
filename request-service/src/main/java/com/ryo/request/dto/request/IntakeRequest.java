package com.ryo.request.dto.request;

import com.ryo.request.constant.DosageUnit;
import com.ryo.request.constant.MedicineForm;
import com.ryo.request.constant.Note;
import com.ryo.request.constant.Usage;
import com.ryo.request.dto.MedicationSchedule;

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
