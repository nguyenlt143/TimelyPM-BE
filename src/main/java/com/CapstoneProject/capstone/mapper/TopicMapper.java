package com.CapstoneProject.capstone.mapper;

import com.CapstoneProject.capstone.dto.request.topic.CreateNewTopicRequest;
import com.CapstoneProject.capstone.dto.response.topic.CreateNewTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.GetTopicResponse;
import com.CapstoneProject.capstone.model.Topic;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TopicMapper {
    ModelMapper modelMapper;
    public Topic toTopic(CreateNewTopicRequest request) {
        return modelMapper.map(request, Topic.class);
    }

    public CreateNewTopicResponse toResponse(Topic topic) {
        return modelMapper.map(topic, CreateNewTopicResponse.class);
    }

    public GetTopicResponse topicResponse(Topic topic) {
        return modelMapper.map(topic, GetTopicResponse.class);
    }
}
