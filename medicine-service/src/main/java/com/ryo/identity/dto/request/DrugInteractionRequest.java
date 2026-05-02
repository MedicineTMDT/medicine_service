package com.ryo.identity.dto.request;

import lombok.Data;

@Data
public class DrugInteractionRequest {
    private String mucDoNghiemTrong;
    private String hauQuaCuaTuongTac;
    private String coCheTuongTac;
    private String xuTriTuongTac;
    private String hoatChat1Name;
    private String hoatChat2Name;
    private Integer ingredient1Id;
    private Integer ingredient2Id;
}
