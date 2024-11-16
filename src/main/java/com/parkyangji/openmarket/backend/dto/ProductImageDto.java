package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data // 상품 상세 이미지
public class ProductImageDto {
  private int image_id;
  private int product_id;
  private int image_type_id;
  private String image_url;
}
