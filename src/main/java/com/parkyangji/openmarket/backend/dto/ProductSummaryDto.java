package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

@Data
public class ProductSummaryDto { 
  private int product_id;
  private String store_name;
  private int category_id;
  private String title;
  private String image_url; // 썸네일용
  // private Map<String, ?> detailsImages;
  private Integer origin_price;
  private Integer discount_rate;
  private Integer sale_price;
  private List<ProductOptionSummaryDto> options; // 제품의 옵션 목록
  private Float avgRating;
  private Integer reviewCount;
  private List<String> keywords;

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
