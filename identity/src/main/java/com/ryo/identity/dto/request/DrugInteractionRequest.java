package com.ryo.identity.dto.request;

import lombok.Data;

@Data
public class DrugInteractionRequest {
    private String mucDoNghiemTrong;
    private String hauQuaCuaTuongTac;
    private String coCheTuongTac;
    private String xuTriTuongTac;
    private Integer ingredient1Id;
    private Integer ingredient2Id;
}
