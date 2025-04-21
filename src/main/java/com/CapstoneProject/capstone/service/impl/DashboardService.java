package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.response.dashboard.GetDashboardResponse;
import com.CapstoneProject.capstone.repository.ProjectRepository;
import com.CapstoneProject.capstone.repository.UserRepository;
import com.CapstoneProject.capstone.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    @Override
    public GetDashboardResponse getDashboard() {
        int totalProjects = (int) projectRepository.count();
        int totalProjectsCompleted = projectRepository.countByStatus("DONE");
        int totalUsers = (int) userRepository.count();

        GetDashboardResponse response = new GetDashboardResponse();
        response.setTotalProjects(totalProjects);
        response.setTotalProjectsCompleted(totalProjectsCompleted);
        response.setTotalUsers(totalUsers);

        return response;
    }
}
