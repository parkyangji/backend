package com.parkyangji.openmarket.backend.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductOrderDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.user.mapper.UserSqlMapper;

@Service
public class UserService {

  @Autowired
  private UserSqlMapper userSqlMapper;
  
  public void userRegister(CustomerDto customerDto, AddressDto addressDto){
      // 1. Customer 저장
      userSqlMapper.insertCustomer(customerDto);
      
      // 2. Customer ID를 Address에 설정
      int customerId = customerDto.getCustomer_id(); // 자동 생성된 customer_id를 가져옴
      addressDto.setCustomer_id(customerId);

      // 3. Address 저장
      userSqlMapper.insertAddress(addressDto);
  }

  public CustomerDto loginCheck(CustomerDto customerDto){
    return userSqlMapper.selectLoginCheck(customerDto); // 없으면 null
  }

  public Map<String, Object> getProductDate(int product_id) {
    Map<String, Object> productData = new HashMap<>();

    // 상품정보
    ProductDto productDto = userSqlMapper.selectProductDto(product_id);
    productData.put("productInfo", productDto);

    // 판매자(브랜드)
    String storeName = userSqlMapper.selectStoreName(productDto.getSeller_id());
    productData.put("storeName", storeName);

    // 평점 통계 => 상품 구매 구현시 가능

    // 리뷰 => 상품 후기 등록 구현시 가능

    // 문의 => 나중에 업데이트시!!!!!!!
    return productData;
  }

  public List<String> getAddressList(int customer_id){
    return userSqlMapper.selectAddressList(customer_id);    
  }

  public void createOrder(ProductOrderDto productOrderDto){
    userSqlMapper.insertOrder(productOrderDto);
  }

  public List<Map<String, Object>> getOrderList(int customer_id) {
    List<Map<String, Object>> orderData = new ArrayList<>();

    List<ProductOrderDto> orderList = userSqlMapper.selectOrderList(customer_id);
    
    for (ProductOrderDto order : orderList) {
      Map<String, Object> product = new HashMap<>();
      product.put("ProductDto", userSqlMapper.selectProductDto(order.getProduct_id()));

      ProductReviewDto productReviewDto = new ProductReviewDto();
      productReviewDto.setProduct_id(order.getProduct_id());
      productReviewDto.setCustomer_id(order.getCustomer_id());
      productReviewDto.setOrder_id(order.getOrder_id());
      System.out.println(order.getOrder_id());
      product.put("ProductReview", userSqlMapper.selectReviewByProductAndCustomer(productReviewDto));
      product.put("ProductOrderDto", order);

      orderData.add(product);
    }

    return orderData;
  }

  public List<Map<String, Object>> getLikeList(int customer_id) {
    List<Map<String, Object>> LikeData = new ArrayList<>();

    List<ProductFavoriteDto> likeList = userSqlMapper.selectUserFavoriteList(customer_id);

    for (ProductFavoriteDto like : likeList) {
      Map<String, Object> product = new HashMap<>();
      product.put("ProductFavoriteDto", like);
      product.put("ProductDto", userSqlMapper.selectProductDto(like.getProduct_id()));
      LikeData.add(product);
    }

    return LikeData;
  }

  public void saveOrUpdateReview(ProductReviewDto productReviewDto) {
    // 기존 리뷰가 존재하는지 확인

    ProductReviewDto existingReview = userSqlMapper.selectReviewByProductAndCustomer(productReviewDto);

    if (existingReview == null) {
        // 리뷰가 없으면 새로 INSERT
        userSqlMapper.insertReview(productReviewDto);
    } else {
        // 리뷰가 있으면 UPDATE
        if (productReviewDto.getRating() != 0) {
            existingReview.setRating(productReviewDto.getRating());
        }
        if (productReviewDto.getContent() != null) {
            existingReview.setContent(productReviewDto.getContent());
        }
        userSqlMapper.updateReview(existingReview);
    }
  }

  public void toggleLike(ProductFavoriteDto productFavoriteDto) {

    ProductFavoriteDto existingLike = userSqlMapper.selectLike(productFavoriteDto);
    // System.out.println(existingLike);

    if (existingLike == null) {
      userSqlMapper.insertLike(productFavoriteDto);
    } else {
      userSqlMapper.deleteLike(productFavoriteDto);
    }

  }

  public ProductFavoriteDto findLike(ProductFavoriteDto productFavoriteDto){
    return userSqlMapper.selectLike(productFavoriteDto);
  }

}
