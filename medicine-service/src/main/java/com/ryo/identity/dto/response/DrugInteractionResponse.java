package com.ryo.identity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrugInteractionResponse {
    private Integer id;
    private String mucDoNghiemTrong;
    private String hauQua;
    private String coChe;
    private String xuTri;
}

