package com.parkyangji.openmarket.backend.vo;

import java.text.NumberFormat;
import java.util.Locale;

import lombok.Value;

@Value // getter만 있음
public class ProductThumbnailVo {
  private int product_id;
  private String store_name;
  private int category_id;
  private String title;
  private String image_url;
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
