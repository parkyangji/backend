package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

@Data
public class ProductDetailReturnDto {
  private int product_id;
  private String store_name;
  private String store_eng_name;
  private int category_id;
  private String title;
  private Map<String, ?> images;
  private Integer origin_price;
  private Integer discount_rate;
  private Integer sale_price;
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
