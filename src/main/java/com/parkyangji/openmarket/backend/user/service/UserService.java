package com.parkyangji.openmarket.backend.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.user.mapper.UserSqlMapper;
import com.parkyangji.openmarket.backend.vo.ProductThumbnailVo;

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



  public Map<String, Object> getCategoryProductList(int parent_category_id){
    Map<String, Object> productData = new HashMap<>();

    // 상단 메뉴 이름
    productData.put("category_name", userSqlMapper.selectCategoryName(parent_category_id));

    // 하단 2뎁스 탭 메뉴
    // category_id에서 parent_id가 같은걸 찾아 모두 출력해줘야함! (상품등록은 2뎁스 기준으로 되어있음)
    List<Map<String, Object>> subCategoryIdAndName = userSqlMapper.selectSubCategorys(parent_category_id);
    //System.out.println( userSqlMapper.selectSubCategorys(parent_category_id).toString());
    
    List<String> categoryNames = new ArrayList<>();
    for (Map<String, Object> category : subCategoryIdAndName) {
      categoryNames.add((String) category.get("category_name"));
    }
    productData.put("sub_categorys", categoryNames);
    

    // 1뎁스 기준 상품 출력
    List<Integer> categoryId = new ArrayList<>();
    for (Map<String, Object> category : subCategoryIdAndName) {
      categoryId.add((Integer) category.get("category_id"));
    }
    System.out.println(categoryId);
    // List<ProductDto> products = userSqlMapper.selectCategoryIdProducts(categoryId);

    List<ProductThumbnailVo> categoryAllProductList = userSqlMapper.selectProductWithThumbnail(categoryId);

    productData.put("productList", categoryAllProductList);
    
    /* 
    productData.put("category_name", userSqlMapper.selectCategory(category_id).getCategory_name());

    List<ProductDto> products = userSqlMapper.selectCategoryIdProducts(category_id);

    List<Map<String,Object>> new_productList = new ArrayList<>();
    for (ProductDto product : products) {
      Map<String, Object> data = new HashMap<>();
      data.put("product", product);
      data.put("store_name", userSqlMapper.selectStoreName(product.getSeller_id())); // 브랜드명
      data.put("avgRating", userSqlMapper.selectAvgRating(product.getProduct_id())); // 평균 별점
      data.put("reviewCount", userSqlMapper.selectReviews(product.getProduct_id()).size()); // 개수만
      new_productList.add(data);
    }
    productData.put("productList", new_productList);
    */
    return productData;
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
    Float avgRating =  userSqlMapper.selectAvgRating(product_id);
    productData.put("avgRating", avgRating);

    // 리뷰 => 상품 후기 등록 구현시 가능
    List<Map<String,Object>> reviewData = new ArrayList<>();

    List<ProductReviewDto> getReviews = userSqlMapper.selectReviews(product_id);
    for (ProductReviewDto review : getReviews) {
      Map<String, Object> data = new HashMap<>();
      data.put("review", review);
      data.put("reviewer", userSqlMapper.selectCustomer(review.getCustomer_id()));
      reviewData.add(data);
    }
    productData.put("reviewData", reviewData);

    // 문의 => 나중에 업데이트시!!!!!!!

    return productData;
  }

  public List<String> getAddressList(int customer_id){
    return userSqlMapper.selectAddressList(customer_id);    
  }

  public void createOrderAndUpdateCount(OrderDto productOrderDto){
    userSqlMapper.insertOrder(productOrderDto);
    userSqlMapper.updateTotalQuantity(productOrderDto);
  }

  public List<Map<String, Object>> getOrderList(int customer_id) {
    List<Map<String, Object>> orderData = new ArrayList<>();

    List<OrderDto> orderList = userSqlMapper.selectOrderList(customer_id);
    
    // for (OrderDto order : orderList) {
    //   Map<String, Object> product = new HashMap<>();
    //   ProductDto productDto = userSqlMapper.selectProductDto(order.getProduct_id());
    //   product.put("ProductDto", productDto);

    //   ProductReviewDto productReviewDto = new ProductReviewDto();
    //   productReviewDto.setProduct_id(order.getProduct_id());
    //   productReviewDto.setCustomer_id(order.getCustomer_id());
    //   productReviewDto.setOrder_id(order.getOrder_id());
    //   // System.out.println(order.getOrder_id());
    //   product.put("reviewer", userSqlMapper.selectCustomer(order.getCustomer_id()));
    //   product.put("ProductReview", userSqlMapper.selectReviewByProductAndCustomer(productReviewDto));
    //   product.put("ProductOrderDto", order);
    //   product.put("store_name", userSqlMapper.selectStoreName(productDto.getSeller_id())); // 브랜드명 

    //   orderData.add(product);
    // }

    return orderData;
  }

  public Boolean hasReviewsState(int customer_id) {
    // 주문 목록을 조회합니다.
    List<OrderDto> orderList = userSqlMapper.selectOrderList(customer_id);

    // 배송완료 상태이며 리뷰가 없는 항목이 있는지 확인
    return orderList.stream()
        .anyMatch(order -> {
            // 해당 상품에 대한 리뷰 조회
            ProductReviewDto productReviewDto = new ProductReviewDto();
            // productReviewDto.setProduct_id(order.getProduct_id());
            // productReviewDto.setCustomer_id(order.getCustomer_id());
            // productReviewDto.setOrder_id(order.getOrder_id());

            ProductReviewDto review = userSqlMapper.selectReviewByProductAndCustomer(productReviewDto);

            // 배송완료 상태이고 리뷰가 없는 경우 true 반환
            return "배송완료".equals(order.getStatus()) && review == null;
        });
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
