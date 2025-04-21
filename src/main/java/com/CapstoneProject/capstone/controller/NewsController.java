package com.CapstoneProject.capstone.controller;

import com.CapstoneProject.capstone.constant.UrlConstant;
import com.CapstoneProject.capstone.dto.request.news.CreateNewsRequest;
import com.CapstoneProject.capstone.dto.response.BaseResponse;
import com.CapstoneProject.capstone.dto.response.news.CreateNewsResponse;
import com.CapstoneProject.capstone.dto.response.news.GetNewsResponse;
import com.CapstoneProject.capstone.service.INewsService;
import com.CapstoneProject.capstone.service.impl.AppwriteStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(UrlConstant.NEWS.NEWS)
@RequiredArgsConstructor
public class NewsController {
    private final INewsService newsService;
//    private final GoogleDriveService googleDriveService;
    private final AppwriteStorageService appwriteStorageService;

    @PostMapping(UrlConstant.NEWS.CREATE)
    public ResponseEntity<BaseResponse<CreateNewsResponse>> create(@RequestBody CreateNewsRequest request) {
        CreateNewsResponse response = newsService.createNews(request);
        return ResponseEntity.ok(new BaseResponse<>("200", "Tạo news thành công", response));
    }

    @GetMapping(UrlConstant.NEWS.GET_ALL_NEWS)
    public ResponseEntity<BaseResponse<List<GetNewsResponse>>> getAll() {
        List<GetNewsResponse> response = newsService.getAllNews();
        return ResponseEntity.ok(new BaseResponse<>("200", "List news", response));
    }

    @GetMapping(UrlConstant.NEWS.GET_NEWS)
    public ResponseEntity<BaseResponse<GetNewsResponse>> getById(@PathVariable UUID id) {
        GetNewsResponse response = newsService.getNews(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Lấy news thành công", response));
    }

    @DeleteMapping(UrlConstant.NEWS.DELETE_NEWS)
    public ResponseEntity<BaseResponse<Boolean>> delete(@PathVariable UUID id) {
        boolean response = newsService.deleteNews(id);
        return ResponseEntity.ok(new BaseResponse<>("200", "Xóa news thành công", response));
    }

    @PostMapping(value = UrlConstant.NEWS.UPLOAD_IMAGE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> create(@RequestParam MultipartFile file ) throws IOException, InterruptedException {
        String response = appwriteStorageService.uploadFileToAppwrite(file);
        return ResponseEntity.ok(new BaseResponse<>("200", "Thêm hình ảnh thành công", response));
    }
}
