package com.ryo.request.dto;

import com.ryo.request.constant.Timing;
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
