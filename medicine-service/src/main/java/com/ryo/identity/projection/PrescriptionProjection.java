package com.ryo.identity.projection;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionProjection {
    String getId();
    String getName();
    String getDescription();
    LocalDate getStartDate();
    LocalDate getEndDate();
}
