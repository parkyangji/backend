package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class DeliveryInfoDto {
  private int delivery_id;
  private int order_id;
  private int address_id;
  private String delivery_message;
  private String delivery_status; // 배송준비중, 배송중, 배송완료
}
