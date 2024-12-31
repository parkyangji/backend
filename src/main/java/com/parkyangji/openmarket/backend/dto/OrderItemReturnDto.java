package com.parkyangji.openmarket.backend.dto;

import java.util.Date;

import lombok.Data;

@Data
public class OrderItemReturnDto {
  private Date order_date;
  private int order_detail_id;
  private int order_id;
  private int customer_id;
  private String delivery_message;
  private String nickname; // 변경필요
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
  private int buy_price; // 주문 가격
  private Integer rating;
  private Date rating_created;
  private String review_content;
  private Date review_created;
  private String seller_reply;
}
