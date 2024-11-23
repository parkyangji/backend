package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductOptionReturnDto {
  private int combination_value_id;
  private int combination_id;
  private String optionname;
  private String optionvalue;
  private int quantity;
  private String status;
  private int origin_price; // 옵션당 원래가격
  private Integer discount_rate;
  private Integer sale_price;
  private Integer rating;
  private String review_content;
  private String seller_reply;
}
