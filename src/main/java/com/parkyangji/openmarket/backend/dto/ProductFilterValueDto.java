package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductFilterValueDto {
  private int product_filter_id;
  private int product_id;
  private int category_filter_value_id;
}
