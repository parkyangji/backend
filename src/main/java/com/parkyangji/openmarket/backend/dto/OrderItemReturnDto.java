package com.parkyangji.openmarket.backend.dto;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Data;

@Data
public class OrderItemReturnDto {
  private Date order_date;
  private int order_detail_id;
  private int order_id;
  private int customer_id;
  private String nickname;
  private String status; //"결제완료","결제대기","배송준비중","배송중","배송완료" , "취소대기", "취소완료", "교환/반품신청", "교환/반품수거완료", "교환/반품완료"
  private int seller_id;
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
  private Integer total_order_price;
  // 리뷰
  // 평점

  private String formatPrice(Integer price) {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(price);
  }

  public String getFormattedOriginPrice() {
      return formatPrice(origin_price);
  }

  public String getFormattedSalePrice() {
      return formatPrice(sale_price);
  }

  // 주문 날짜를 yyyy.MM.dd 형식으로 반환
  public String getFormattedOrderDate() {
    if (order_date == null) {
        return "";
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    return dateFormat.format(order_date);
  }

  // 주문 시간을 HH:mm 형식으로 반환 (24시간 기준)
  public String getFormattedOrderTime() {
    if (order_date == null) {
        return "";
    }
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    return timeFormat.format(order_date);
  }
}
