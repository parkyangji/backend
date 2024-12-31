package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

@Data
public class ProductInventorySummaryDto {
  private int combination_id;
  private int order_detail_id;
  private List<Map<String, Object>> option; // 옵션명과 값을 한꺼번에 담은 리스트
  private int quantity;
  private String status;
  private int origin_price; // 옵션당 원래가격
  private Integer discount_rate;
  private Integer sale_price;
  private int buy_price; // 구입 가격
  private Integer rating;
  private Date rating_created;
  private String review_content;
  private Date review_created;
  private String seller_reply;
  private Date seller_reply_created;
}
