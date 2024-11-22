package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data // 상품 리뷰
public class ProductReviewDto { 
  private int order_review_id;
  private int order_detail_id;
  private String review_content;
  private Date created_date;
}
