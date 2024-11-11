package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data //배송지 테이블
public class AddressDto {
  private int address_id;
  private int customer_id;
  private String address;
}
