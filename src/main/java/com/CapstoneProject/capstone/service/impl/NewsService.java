package com.CapstoneProject.capstone.service.impl;

import com.CapstoneProject.capstone.dto.request.news.CreateNewsRequest;
import com.CapstoneProject.capstone.dto.response.news.CreateNewsResponse;
import com.CapstoneProject.capstone.dto.response.news.GetNewsResponse;
import com.CapstoneProject.capstone.exception.NotFoundException;
import com.CapstoneProject.capstone.model.News;
import com.CapstoneProject.capstone.repository.NewsRepository;
import com.CapstoneProject.capstone.service.INewsService;
import com.CapstoneProject.capstone.util.HtmlSanitizerUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService implements INewsService {
    private final NewsRepository newsRepository;
    @Override
    public CreateNewsResponse createNews(CreateNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle());
        String sanitizedContent = HtmlSanitizerUtil.sanitize(request.getContent());
        sanitizedContent = sanitizedContent.replaceAll("&amp;mode=admin", "")
                .replaceAll("&mode=admin", "");
        news.setContent(sanitizedContent);
        news.setActive(true);
        news.setCreatedAt(LocalDateTime.now());
        news.setUpdatedAt(LocalDateTime.now());
        newsRepository.save(news);
        CreateNewsResponse response = new CreateNewsResponse();
        response.setId(news.getId());
        response.setTitle(news.getTitle());
        response.setContent(sanitizedContent);
        return response;
    }

    @Override
    public List<GetNewsResponse> getAllNews() {
        List<News> newsList = newsRepository.findAll();
        List<GetNewsResponse> responses = newsList.stream().map(news -> {
            GetNewsResponse response = new GetNewsResponse();
            response.setId(news.getId());
            response.setTitle(news.getTitle());
            response.setContent(news.getContent());
            return response;
        }).collect(Collectors.toList());
        return responses;
    }

    @Override
    public GetNewsResponse getNews(UUID id) {
        News news = newsRepository.findById(id).orElseThrow(() -> new NotFoundException("News not found"));
        GetNewsResponse response = new GetNewsResponse();
        response.setId(news.getId());
        response.setTitle(news.getTitle());
        response.setContent(news.getContent());
        return response;
    }

    @Override
    public Boolean deleteNews(UUID id) {
        News news = newsRepository.findById(id).orElseThrow(() -> new NotFoundException("News not found"));
        newsRepository.delete(news);
        return true;
    }
}
