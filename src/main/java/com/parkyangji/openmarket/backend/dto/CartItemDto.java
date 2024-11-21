package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class CartItemDto {
  private int cart_id;
  private int combination_id;
  private int quantity;
}
