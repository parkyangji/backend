package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data // 상품
public class ProductDto {
  private int product_id;
  private int category_id;
  private int seller_id;
  private String title;
  private String description;
  private int price;
  private String main_page_url;
  private int total_quantity;
  private Date created_date;
}
