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
  private String nickname;
  // private String status;
  private List<ProductSummaryDto> products; // 주문에 포함된 제품 목록


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