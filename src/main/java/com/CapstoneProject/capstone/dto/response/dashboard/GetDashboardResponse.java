package com.CapstoneProject.capstone.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetDashboardResponse {
    private int totalProjects;
    private int totalProjectsCompleted;
    private int totalUsers;
}
