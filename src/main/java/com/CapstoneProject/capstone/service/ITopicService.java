package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.topic.CreateNewTopicRequest;
import com.CapstoneProject.capstone.dto.request.topic.UpdateTopicRequest;
import com.CapstoneProject.capstone.dto.response.topic.CreateNewTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.GetTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.UpdateTopicResponse;

import java.util.List;
import java.util.UUID;

public interface ITopicService {
    CreateNewTopicResponse createNewTopic(CreateNewTopicRequest request);
    List<GetTopicResponse> getTopics(UUID projectId);
    GetTopicResponse getTopic(UUID topicId, UUID projectId);
    boolean deleteTopic(UUID topicId, UUID projectId);
    UpdateTopicResponse updateTopic(UUID topicId, UUID projectId, UpdateTopicRequest request);
}
