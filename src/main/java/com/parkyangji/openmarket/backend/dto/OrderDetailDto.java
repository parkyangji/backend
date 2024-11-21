package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data
public class OrderDetailDto {
  private int order_detail_id;
  private int order_id;
  private int combination_id;
  private int quantity;
  private int price;
  private String status; // 기본 결제대기
  // "결제완료","결제대기","배송준비중","배송중","배송완료" , "취소대기", "취소완료", "교환/반품신청", "교환/반품수거완료", "교환/반품완료"
}
