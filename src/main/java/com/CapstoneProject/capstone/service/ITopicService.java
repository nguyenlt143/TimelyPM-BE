package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.topic.CreateNewTopicRequest;
import com.CapstoneProject.capstone.dto.response.topic.CreateNewTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.GetTopicResponse;

import java.util.List;
import java.util.UUID;

public interface ITopicService {
    CreateNewTopicResponse createNewTopic(CreateNewTopicRequest request);
    List<GetTopicResponse> getTopics(UUID projectId);
}
