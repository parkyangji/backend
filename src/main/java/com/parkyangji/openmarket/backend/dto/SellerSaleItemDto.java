package com.parkyangji.openmarket.backend.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class SellerSaleItemDto {
  private int product_id;
  private int discount_rate;
}
