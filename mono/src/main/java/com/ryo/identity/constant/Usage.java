package com.ryo.identity.constant;

import lombok.Getter;

@Getter
public enum Usage {

    // ===== ĐƯỜNG UỐNG =====
    ORAL("Uống", "Cách dùng phổ biến nhất."),
    SUBLINGUAL("Ngậm", "Ngậm dưới lưỡi hoặc ngậm tan trong miệng."),
    CHEW("Nhai", "Nhai kỹ trước khi nuốt."),

    // ===== NGOÀI DA =====
    TOPICAL("Bôi", "Thoa ngoài da."),

    // ===== NHỎ =====
    EYE_DROPS("Nhỏ mắt", "Nhỏ trực tiếp vào mắt."),
    EAR_DROPS("Nhỏ tai", "Nhỏ vào ống tai."),
    NASAL_DROPS("Nhỏ mũi", "Nhỏ vào mũi."),

    // ===== TIÊM =====
    IM("Tiêm bắp", "Tiêm vào cơ (Intramuscular)."),
    IV("Tiêm tĩnh mạch", "Tiêm trực tiếp vào tĩnh mạch (Intravenous)."),
    SC("Tiêm dưới da", "Tiêm vào lớp mô dưới da (Subcutaneous)."),

    // ===== ĐẶT =====
    RECTAL("Đặt hậu môn", "Đặt thuốc qua đường hậu môn."),
    VAGINAL("Đặt âm đạo", "Đặt thuốc qua đường âm đạo.");

    private final String name;
    private final String description;

    Usage(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
