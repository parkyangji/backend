package com.parkyangji.openmarket.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class CartSummaryDto {
  private int cart_id;
  private List<ProductSummaryDto> products;
}
