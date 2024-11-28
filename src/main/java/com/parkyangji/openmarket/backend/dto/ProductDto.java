package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data // 상품
public class ProductDto {
  private int product_id;
  private int seller_id;
  private int category_id;
  private String title;
  private Date created_date;
}
