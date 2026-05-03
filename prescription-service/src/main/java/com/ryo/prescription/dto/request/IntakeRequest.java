package com.ryo.prescription.dto.request;

import com.ryo.prescription.constant.DosageUnit;
import com.ryo.prescription.constant.MedicineForm;
import com.ryo.prescription.constant.Note;
import com.ryo.prescription.constant.Usage;
import com.ryo.prescription.dto.MedicationSchedule;
import com.ryo.identity.constant.*;

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
