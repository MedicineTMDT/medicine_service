package com.ryo.identity.projection;

import java.util.List;

public interface DrugProjection {
    Integer getId();
    String getName();
    String getSlug();
    List<String> getImage();
}
