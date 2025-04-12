package com.CapstoneProject.capstone.dto.response.projectActivityLog;

import com.CapstoneProject.capstone.enums.ActivityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetProjectLogResponse {
    private ActivityTypeEnum activityType;
    private LocalDateTime updateTime;
}
