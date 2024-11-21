package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data // 상품 구매
public class OrderDto { 
  private int order_id;
  private int customer_id;
  //private int product_id;
  //private int quantity;
  //private String delivery_address;
  private Date order_date;
  // private String status; 
}
