package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data // 상품 카테고리
public class ProductCategoryDto {
  private int category_id;
  private int parent_id; // 부모 카테고리 id > null일 경우 최상위 카테고리
  private String category_name;
}
