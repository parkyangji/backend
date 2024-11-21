package com.parkyangji.openmarket.backend.vo;

import java.text.NumberFormat;
import java.util.Locale;

import lombok.Value;

// @Value // getter만 있음 불볍객체로 만듬 
//VO 객체가 모든 필드를 가져야 한다!!!
//MyBatis 매핑 과정에서 모든 필드가 일치하지 않으면 IndexOutOfBoundsException이 발생할 수 있으므로, 
// SQL 결과와 VO 필드 간의 일관성을 맞추는 것이 중요함.
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
