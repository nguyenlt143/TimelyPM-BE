package com.CapstoneProject.capstone.dto.response.news;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GetNewsResponse {
    private UUID id;
    private String title;
    private String content;
}
