package com.ryo.prescription.projection;

import java.util.List;

public interface DrugProjection {
    Integer getId();
    String getName();
    String getSlug();
    List<String> getImage();
}
