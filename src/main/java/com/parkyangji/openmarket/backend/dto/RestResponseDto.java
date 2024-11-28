package com.parkyangji.openmarket.backend.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class RestResponseDto {
    private String result; //"success", "fail"
    private String reason; //"데이터가 부족합니다."
    private Map<String, Object> data = new HashMap<>();

    public void add(String name, Object value) { // "count", "isLiked", "commentList"
        data.put(name, value);
    }
}
