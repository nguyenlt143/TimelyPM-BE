package com.CapstoneProject.capstone.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChartDataResponse {
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> issueByStatus;
    private Map<String, Long> priorityDistribution;
    private Long totalTasks;
    private Long pendingTasks;
    private Long toDoTasks;
    private Long inProgressTasks;
    private Long waitingTestTasks;
    private Long doneTasks;
    private Long totalIssues;
    private Long openIssues;
    private Long notBugIssues;
    private Long fixedIssues;
    private Long pendingRetestIssues;
    private Long retestIssues;
    private Long reOpenedIssues;
    private Long verifiedIssues;
    private Long closedIssues;
}
