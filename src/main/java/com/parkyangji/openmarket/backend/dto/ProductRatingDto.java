package com.parkyangji.openmarket.backend.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class ProductRatingDto {
  private int rating_id;
  private int order_detail_id;
  private Integer rating;
  private Date created_date;
}
