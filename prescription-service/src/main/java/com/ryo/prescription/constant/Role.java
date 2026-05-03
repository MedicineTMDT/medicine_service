package com.ryo.prescription.constant;

import lombok.Getter;

@Getter
public enum Role {
    ADMIN("ADMIN"),
    USER("USER"),
    MED("MED");

    private final String name;

    Role(String name){
        this.name = name;
    }
}
