package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.util.Locale;

import lombok.Data;

@Data
public class CartItemReturnDto {
  private int cart_id;
  private int product_id;
  private String store_name;
  private String title;
  private String image_url;
  private int combination_id;
  private String optionname;
  private String optionvalue;
  private int quantity;
  private int origin_price;
  private Integer discount_rate;
  private Integer sale_price;
}
