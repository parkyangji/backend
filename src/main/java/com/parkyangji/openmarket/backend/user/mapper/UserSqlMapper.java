package com.parkyangji.openmarket.backend.user.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.parkyangji.openmarket.backend.common.CommonSqlMapper;
import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerCartDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.DeliveryInfoDto;
import com.parkyangji.openmarket.backend.dto.OrderDetailDto;
import com.parkyangji.openmarket.backend.dto.ProductDetailReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductRatingDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;

@Mapper
public interface UserSqlMapper extends CommonSqlMapper{
  // 회원가입
  public int getAddressCountByCustomerId(int customerId);
  public void insertCustomer(CustomerDto customerDto);
  public void insertAddress(AddressDto addressDto);
  public AddressDto selectDefalutAddress(int customerId);

  // 로그인 고객 찾기
  public CustomerDto selectLoginCheck(CustomerDto customerDto);

  // 상품 상세 페이지
  public String selectStoreName(int seller_id);
  public Float selectAvgRating(int product_id);
  public List<ProductReviewDto> selectReviews(int product_id);
  public CustomerDto selectCustomer(int customer_id);

  // 주문 넣기
  public void insertOrder(OrderDto orderDto);
  public void insertDeliveryInfo(DeliveryInfoDto deliveryInfoDto);

  // 배송지 가져오기
  public List<String> selectAddressList(int customer_id);

  // 주문 내역 가져오기
  //public List<OrderDto> selectOrderList(int customer_id);

  // 리뷰
  public ProductRatingDto selectRatingCheck(int order_detail_id);
  public void insertRating(@Param("order_detail_id") int order_detail_id, @Param("rating") int rating);
  public void insertReviwContent(@Param("order_detail_id") int order_detail_id, @Param("review_content") String review_content);
  // public void insertReview(ProductReviewDto productReviewDto);
  // public void updateReview(ProductReviewDto productReviewDto);selectProductOptionAndInventory

  // 좋아요
  public ProductFavoriteDto selectLike(ProductFavoriteDto productFavoriteDto);
  public List<ProductFavoriteDto> selectLike(List<ProductFavoriteDto> productFavoriteDto);
  public void insertLike(ProductFavoriteDto productFavoriteDto);
  public void deleteLike(ProductFavoriteDto productFavoriteDto);

  public List<ProductFavoriteDto> selectUserFavoriteList(int customer_id);

  public List<ProductDto> selectCategoryIdProducts(List<Integer> categoryIds);
  //public List<ProductDetailReturnDto> selectProductListByCategoryId(List<Integer> categoryIds);
  public List<ProductDetailReturnDto> selectProductListByCategoryId(Integer categoryId);

  public List<ProductDetailReturnDto> selectBrandProducts(String store_name);
  public List<ProductDetailReturnDto> selectBrandProductByCategoryId(@Param("store_name") String store_name, @Param("category_id") int category_id);

  public List<ProductDetailReturnDto> selectKeywordFromCategory(@Param("keyword_name") String keyword_name, @Param("category_id") Integer category_id);
  // 상품 상세 페이지
 // public ProductDetailReturnDto selectProductDetail(int product_id);

  // 장바구니
  public CustomerCartDto selectCustomerCart(int customer_id);
  public void insertCustomerCart(CustomerCartDto customerCartDto);
  public void insertCartItem(CartItemDto cartItemDto);
  public Integer checkExistingCartItem(@Param("cart_id") int cart_id, @Param("combination_id") int combination_id);
  public void updateCartItemQuantity(@Param("cart_id") int cart_id,  @Param("combination_id") int combination_id, @Param("quantity") int quantity);
  public void updateCartItemExQuantity(CartItemDto cartItemDto);
  public void deleteCart(int cart_id);
  public void deleteCartItem(CartItemDto cartItemDto);
  public List<CartItemReturnDto> selectCustomerCartItems(int customer_id);
  public List<CartItemReturnDto> selectTempCartItems(@Param("product_id") int productId, @Param("combination_id") List<Integer> combinationId);
  public List<CartItemReturnDto> selectCustomerOrderDetails(int customer_id);
  public void insertOrderDetails(List<OrderDetailDto> orderDetailDtos);
  public List<CartItemDto> selectExQuantityByOrderId(int order_id);

  // 마이페이지
  public List<OrderItemReturnDto> selectOrderList(int customer_id);

}
