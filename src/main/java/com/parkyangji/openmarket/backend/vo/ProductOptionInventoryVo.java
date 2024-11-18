package com.parkyangji.openmarket.backend.vo;

import java.text.NumberFormat;
import java.util.Locale;

import lombok.Value;

@Value
public class ProductOptionInventoryVo {
  private int product_id;
  private int combination_id;
  private int combination_value_id;
  private String optionvalue;
  private String optionname;
  private Integer quantity;
  private Integer origin_price;
  private Integer discount_rate;
  private Integer sale_price;

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
