package com.ryo.identity.constant;

import lombok.Getter;

@Getter
public enum DosageUnit {
    MG("milligram",""),
    MCG("microgram","1mg = 1000mcg"),
    G("gram","1g = 1000mg"),
    IU("unit","Đơn vị quốc tế (International Unit) - thường dùng cho vitamin, vaccine, hormone"),
    ML("milliliter","thường dùng cho thuốc dạng lỏng (siro, dung dịch)"),
    PERCENT("%","Nồng độ phần trăm - thường dùng cho thuốc bôi, nhỏ mắt/mũi");

    private final String name;
    private final String description;

    DosageUnit(String name, String description){
        this.name = name;
        this.description = description;
    }
}

