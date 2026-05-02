package com.ryo.identity.constant;
import lombok.Getter;

@Getter
public enum Note {
    // ===== LIÊN QUAN BỮA ĂN =====
    BEFORE_MEAL("Trước ăn", "Uống trước bữa ăn 30 phút đến 1 giờ (ac - Ante Cibum)."),
    AFTER_MEAL("Sau ăn", "Uống ngay sau bữa ăn hoặc trong vòng 1 giờ (pc - Post Cibum)."),
    WITH_MEAL("Trong bữa ăn", "Uống cùng với thức ăn."),
    EMPTY_STOMACH("Lúc đói", "Dùng khi dạ dày rỗng (1 giờ trước ăn hoặc 2 giờ sau ăn)."),
    PRN(
            "Khi cần",
            "Chỉ dùng khi có triệu chứng như đau, sốt (prn - Pro Re Nata)."
    );

    private final String name;
    private final String description;

    Note(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
