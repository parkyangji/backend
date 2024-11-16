package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ProductCombinationValueDto { // 나중에 장바구니, 주문상세에 활용됨 (옵션값 쓸때 사용!!!)
  private int combination_value_id; 
  private int combination_id; //  1 (번 조합)
  private int option_value_id; // 빨강 (옵션값 4번) , S (옵션값 1번) => 1번 조합번호를 가진 값들을 각각의 고유번호로 삽입 !!!
}
