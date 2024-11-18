package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductOptionInventoryDto { //일대일
  private int inventory_id;
  private int combination_id; // product_option_combination 에서 생성하는 조합 번호
  private Integer quantity;
  private Integer price;
}
