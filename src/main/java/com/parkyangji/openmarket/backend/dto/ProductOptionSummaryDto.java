package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

@Data
public class ProductOptionSummaryDto { 
private int combination_id;
private int order_detail_id;
// private String optionName;
// private String optionValue;
private List<Map<String, Object>> optionDetails; // 옵션명과 값을 한꺼번에 담은 리스트 // 여기 combination_value_id
private int quantity;
private String status;
private int origin_price;
private Integer discount_rate;
private Integer sale_price;
private Integer rating;
private String review_content;
private String seller_reply;

  private String formatPrice(Integer price) {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(price);
  }

  public String getFormattedOriginPrice() {
      return formatPrice(origin_price);
  }

  public String getFormattedSalePrice() {
      return formatPrice(sale_price);
  }
}
