package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductOptionCombinationDto {
  private int combination_id; // 어떤 옵션의 옵션값별 경우의 수 조합의 번호
  private int product_id;
}
