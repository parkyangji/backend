package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductOptionDto {
  private int option_id;
  private int product_id;
  private String optionvalue; // 사이즈
}
