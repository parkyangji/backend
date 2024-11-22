package com.parkyangji.openmarket.backend.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class ReviewSellerReplyDto {
  private int order_review_id;
  private int seller_reply;
  private Date created_date;
}
