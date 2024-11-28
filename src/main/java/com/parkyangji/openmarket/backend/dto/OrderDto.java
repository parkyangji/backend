package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data // 상품 구매
public class OrderDto { 
  private int order_id;
  private int customer_id;
  private int address_id;
  private String delivery_message;
  private Date order_date;
}
