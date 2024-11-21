package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data // 판매자
public class SellerDto {
  private int seller_id;
  private String username;
  private String password;
  private String store_name;
  private String store_eng_name;
}
