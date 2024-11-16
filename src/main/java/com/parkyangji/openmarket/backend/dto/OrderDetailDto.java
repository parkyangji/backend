package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data
public class OrderDetailDto {
  private int order_id;
  private int customer_id;
  private Date order_date;
  private String status;
}
