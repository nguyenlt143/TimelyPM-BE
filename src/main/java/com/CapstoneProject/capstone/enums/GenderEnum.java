package com.CapstoneProject.capstone.enums;

import com.CapstoneProject.capstone.exception.InvalidEnumException;

public enum GenderEnum {
    MALE,
    FEMALE;

    public static GenderEnum fromString(String gender) {
        for (GenderEnum g : GenderEnum.values()) {
            if (g.name().equalsIgnoreCase(gender)) {
                return g;
            }
        }
        throw new InvalidEnumException("Giới tính không hợp lệ! Chỉ chấp nhận MALE hoặc FEMALE.");
    }
}
