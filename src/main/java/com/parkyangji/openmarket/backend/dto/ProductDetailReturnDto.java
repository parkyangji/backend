package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

@Data
public class ProductDetailReturnDto {  // 상품 리스트, 브랜드 , 상세 상품 페이지!!
  private int product_id;
  private String store_name;
  private int category_id;
  private String title;
  private Map<String, ?> images; // 상세 상품 페이지만 !!
  private Integer rep_price; // 대표가격
  private Integer discount_rate;
  private Integer rep_sale_price; // 대표 세일가
  private Float avgRating;
  private Integer reviewCount;
  private List<String> keywords;
}
