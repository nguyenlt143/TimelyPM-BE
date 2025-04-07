package com.CapstoneProject.capstone.service;

import com.CapstoneProject.capstone.dto.request.news.CreateNewsRequest;
import com.CapstoneProject.capstone.dto.response.news.CreateNewsResponse;
import com.CapstoneProject.capstone.dto.response.news.GetNewsResponse;
import com.CapstoneProject.capstone.model.News;

import java.util.List;
import java.util.UUID;

public interface INewsService {
    CreateNewsResponse createNews(CreateNewsRequest request);
    List<GetNewsResponse> getAllNews();
    GetNewsResponse getNews(UUID id);
    Boolean deleteNews(UUID id);
}
