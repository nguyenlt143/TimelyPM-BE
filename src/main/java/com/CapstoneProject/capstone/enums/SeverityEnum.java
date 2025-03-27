package com.CapstoneProject.capstone.enums;

public enum SeverityEnum {
    MINOR,      // Nhỏ: Ảnh hưởng không đáng kể, hệ thống vẫn hoạt động bình thường.
    MODERATE,   // Trung bình: Có ảnh hưởng nhưng không làm gián đoạn chức năng chính.
    SIGNIFICANT,// Đáng kể: Gây gián đoạn một phần, nhưng hệ thống vẫn có thể phục hồi.
    SEVERE,     // Nghiêm trọng: Ảnh hưởng lớn, gây gián đoạn nghiêm trọng, cần can thiệp ngay.
    CATASTROPHIC // Thảm họa: Hệ thống sụp đổ hoàn toàn, không thể hoạt động.
}
