package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.topic.CreateNewTopicRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.topic.CreateNewTopicResponse;
import com.CapstoneProject.capstone.dto.response.topic.GetTopicResponse;
import com.CapstoneProject.capstone.service.ITopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.TOPIC.TOPIC)
@RequiredArgsConstructor
public class TopicController {
    private final ITopicService topicService;
    @PostMapping(UrlConstant.TOPIC.CREATE)
    public ResponseEntity<BaseResponse<CreateNewTopicResponse>> create(@Valid @RequestBody CreateNewTopicRequest request) {
        CreateNewTopicResponse response = topicService.createNewTopic(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo topic thành công", response));
    }

    @GetMapping(UrlConstant.TOPIC.GET_TOPICS)
    public ResponseEntity<BaseResponse<List<GetTopicResponse>>> getAll(@RequestParam UUID projectId) {
        List<GetTopicResponse> response = topicService.getTopics(projectId);
        return ResponseEntity.ok(new BaseResponse<>("200", "Danh sách topic", response));
    }
}
