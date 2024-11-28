package com.parkyangji.openmarket.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductOptionSummaryDto { 
  private int combination_id;
  private ProductInventorySummaryDto optionDetails; // 옵션 정보 요약
}
