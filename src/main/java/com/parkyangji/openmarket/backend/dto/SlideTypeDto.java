package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class SlideTypeDto {
  private int slide_type_id; // 고유 아이디
  private String type_name; // 슬라이드 유형명 (예: 'MAIN', 'SALE', 'EVENT')
  // 'MAIN', 'NEW', 'RANK', 'SALE' 우선 4개
}
