package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data // 상품 찜
public class ProductFavoriteDto {
  private int favorite_id;
  private int customer_id;
  private int product_id;
}
