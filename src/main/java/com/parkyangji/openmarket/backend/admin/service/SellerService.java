package com.parkyangji.openmarket.backend.admin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.admin.mapper.AdminSqlMapper;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductRatingDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.ReviewSellerReplyDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

@Service
public class SellerService {

  @Autowired
  private AdminSqlMapper adminSqlMapper;

  public SellerDto loginCheck(SellerDto sellerDto) {
    return adminSqlMapper.selectLoginCheck(sellerDto);
  }

  public void sellerRegister(SellerDto sellerDto) {
    adminSqlMapper.insertSeller(sellerDto);
  }

  public List<Map<String, Object>> getAllReviews(int seller_id) {
    List<Map<String, Object>> reviewList = new ArrayList<>();

    // 셀러가 판매하는 상품 목록을 조회
    List<ProductDto> products = adminSqlMapper.selectSellerProducts(seller_id);

    // 각 상품에 대해 주문 목록을 조회하고, 주문별로 Map에 추가
    for (ProductDto product : products) {
      List<ProductReviewDto> reviews = adminSqlMapper.selectReviewByProductId(product.getProduct_id());
      List<ProductRatingDto> ratings = adminSqlMapper.selectRatingByProductId(product.getProduct_id());

      // 주문이 있는 경우에만 orderList에 개별 주문 항목 추가
      for (ProductReviewDto review : reviews) {
          Map<String, Object> reveiwData = new HashMap<>();
          reveiwData.put("product", product);    // 각 주문에 대한 상품 정보
          reveiwData.put("review", review);        // 개별 주문 정보

          int targetIndex = IntStream.range(0, ratings.size())
          .filter(i -> ratings.get(i).getOrder_detail_id() == review.getOrder_detail_id())
          .findFirst()
          .orElse(-1);

          reveiwData.put("rating", ratings.get(targetIndex));
          reveiwData.put("reply", adminSqlMapper.selectReviewReplyByOrderReviewId(review.getOrder_review_id())); 
          reviewList.add(reveiwData);
      }
    }

    return reviewList;
  }

  public void addReply(int order_review_id, String seller_reply) {
    ReviewSellerReplyDto reviewSellerReplyDto = new ReviewSellerReplyDto();
    reviewSellerReplyDto.setOrder_review_id(order_review_id);
    reviewSellerReplyDto.setSeller_reply(seller_reply);

    adminSqlMapper.inserReply(reviewSellerReplyDto);
  }
}
