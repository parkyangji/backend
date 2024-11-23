package com.parkyangji.openmarket.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductOptionSummaryDto { 
private int combination_id;
//private int order_detail_id; // ??
private ProductInventorySummaryDto optionDetails; // 옵션 정보 요약
// private List<Map<String, Object>> optionDetails; // 옵션명과 값을 한꺼번에 담은 리스트 // 여기 combination_value_id
// private int quantity;
// private String status;
// private int origin_price;
// private Integer discount_rate;
// private Integer sale_price;
// private Integer rating;
// private String review_content;
// private String seller_reply;
}
