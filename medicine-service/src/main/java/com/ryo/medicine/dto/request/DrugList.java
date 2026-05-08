package com.ryo.medicine.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DrugList {
    private List<Integer> druglist;
}
