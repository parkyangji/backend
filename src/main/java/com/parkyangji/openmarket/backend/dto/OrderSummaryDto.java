package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lombok.Data;

@Data
public class OrderSummaryDto { // 주문의 기본 정보를 담고 있는 객체
  private int order_id;
  private Date order_date;
  private AddressDto addressDto;
  private String delivery_message;
  private List<BrandSummaryDto> brands; // 브랜드별 그룹화 
  private List<ProductSummaryDto> products; // 주문번호별 상품 => 이후 요구사항에 따라 분리, 통합 결정
}