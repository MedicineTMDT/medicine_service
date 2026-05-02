package com.ryo.medicine.projection;

import java.time.LocalDate;

public interface PrescriptionProjection {
    String getId();
    String getName();
    String getDescription();
    LocalDate getStartDate();
    LocalDate getEndDate();
}
