package com.ryo.prescription.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DrugList {
    private List<Integer> druglist;
}
