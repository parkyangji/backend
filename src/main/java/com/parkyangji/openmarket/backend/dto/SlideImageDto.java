package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class SlideImageDto {
  private int slide_image_id; // 고유 id
  private String image_url; // 이미지 url
  private int product_id; // 제품 연결시 (클릭시 이동 위함)
  private int slide_type_id; // slide_type 고유 id
}
