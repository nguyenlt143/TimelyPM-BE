package com.CapstoneProject.capstone.dto.request.news;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateNewsRequest {
    private String title;
    private String content;
}
