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
    String storeName = userSqlMapper.searchStoreName(productDto.getSeller_id());
    productData.put("storeName", storeName);

    // 평점 통계 => 상품 구매 구현시 가능

    // 리뷰 => 상품 후기 등록 구현시 가능

    // 문의 => 나중에 업데이트시!!!!!!!
    return productData;
  }



}
