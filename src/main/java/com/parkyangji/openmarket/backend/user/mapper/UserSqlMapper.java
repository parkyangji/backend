package com.parkyangji.openmarket.backend.user.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.vo.ProductThumbnailVo;

@Mapper
public interface UserSqlMapper {
  // 회원가입
  public void insertCustomer(CustomerDto customerDto);
  public void insertAddress(AddressDto addressDto);

  // 로그인 고객 찾기
  public CustomerDto selectLoginCheck(CustomerDto customerDto);

  // 상품 상세 페이지
  public String selectStoreName(int seller_id);
  public ProductDto selectProductDto(int product_id);
  public Float selectAvgRating(int product_id);
  public List<ProductReviewDto> selectReviews(int product_id);
  public CustomerDto selectCustomer(int customer_id);

  // 주문 넣기
  public void insertOrder(OrderDto productOrderDto);
  public void updateTotalQuantity(OrderDto productOrderDto);

  // 배송지 가져오기
  public List<String> selectAddressList(int customer_id);

  // 주문 내역 가져오기
  public List<OrderDto> selectOrderList(int customer_id);

  // 리뷰
  public ProductReviewDto selectReviewByProductAndCustomer(ProductReviewDto productReviewDto);
  public void insertReview(ProductReviewDto productReviewDto);
  public void updateReview(ProductReviewDto productReviewDto);

  // 좋아요
  public ProductFavoriteDto selectLike(ProductFavoriteDto productFavoriteDto);
  public void insertLike(ProductFavoriteDto productFavoriteDto);
  public void deleteLike(ProductFavoriteDto productFavoriteDto);

  public List<ProductFavoriteDto> selectUserFavoriteList(int customer_id);

  // 카테고리별 전체 상품
  public String selectCategoryName(int category_id);
  public List<Map<String, Object>> selectSubCategorys(int category_id);
  //public List<ProductDto> selectCategoryIdProducts(int category_id);
  public List<ProductDto> selectCategoryIdProducts(List<Integer> categoryIds);
  public List<ProductThumbnailVo> selectProductWithThumbnail(List<Integer> categoryIds);
}
