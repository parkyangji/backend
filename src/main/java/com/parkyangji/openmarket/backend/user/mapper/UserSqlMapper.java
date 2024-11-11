package com.parkyangji.openmarket.backend.user.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;

@Mapper
public interface UserSqlMapper {
  // 회원가입
  public void insertCustomer(CustomerDto customerDto);
  public void insertAddress(AddressDto addressDto);

  // 로그인 고객 찾기
  public CustomerDto selectLoginCheck(CustomerDto customerDto);

  // 상품 상세 페이지
  public String searchStoreName(int seller_id);
  public ProductDto selectProductDto(int product_id);
}
