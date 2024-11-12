package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data // 상품 리뷰
public class ProductReviewDto { 
  private int review_id;
  private int order_id; // 추가해야함...
  private int product_id;
  private int customer_id;
  private String content;
  private int rating;
  private Date created_date; // 작성일
  private String seller_reply; // 판매자 답글
  private Date reply_date; // 답글 작성일
}
