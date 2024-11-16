package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductOptionValueDto {
  private int option_value_id;
  private int option_id;
  private String optionvalue; // S M L
}
