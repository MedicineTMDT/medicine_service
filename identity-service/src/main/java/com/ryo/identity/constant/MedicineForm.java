package com.ryo.identity.constant;

import lombok.Getter;

@Getter
public enum MedicineForm {
    TABLET("Viên/V","Thuốc viên nén, viên nang"),    // viên
    POWDER("Gói","Thuốc bột, cốm"),    // bột, gói
    VIAL("Ống","Thuốc tiêm, thuốc uống dạng lỏng đóng ống"),      // ống
    SYRUP("Lọ","Thuốc siro, dung dịch, thuốc nhỏ mắt/mũi, thuốc tiêm lọ lớn"),    // lọ
    TUBE("Tuýp","Thuốc mỡ, kem bôi"),      // tuýp
    BOTTLE("Chai","Thuốc dạng lỏng dung tích lớn.");
    private final String name;
    private final String description;
    MedicineForm(String name, String description){
        this.description = description;
        this.name = name;
    }
}

