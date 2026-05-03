package com.ryo.prescription.dto;

import com.ryo.prescription.constant.Timing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MedicationSchedule {
    private Timing timing;
    private Integer quantity;
}
