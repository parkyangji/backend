package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data // 핀메자찜
public class SellerFavoriteDto {
  private int favorite_id;
  private int customer_id;
  private int seller_id;
}
