package com.parkyangji.openmarket.backend.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DebugUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 객체를 JSON으로 변환하고, 예외가 발생하면 로그에 출력하는 메서드
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}"; // 변환에 실패했을 때 기본 JSON 문자열 반환
        }
    }

    // 객체를 JSON으로 변환하고 콘솔에 출력하는 메서드
    public static void printJson(Object object) {
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            System.out.println(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

