package com.ryo.medicine.dto;

import com.ryo.medicine.constant.Timing;
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
