package com.parkyangji.openmarket.backend.dto;

import java.sql.Date;

import lombok.Data;

@Data
public class SellerSaleItemDto {
  private int product_id;
  private int discount_rate;
  // private Date start_date;
  // private Date end_date;
}
