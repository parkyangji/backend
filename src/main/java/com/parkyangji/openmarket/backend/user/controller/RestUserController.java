package com.parkyangji.openmarket.backend.user.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parkyangji.openmarket.backend.common.DebugUtil;
import com.parkyangji.openmarket.backend.common.ShuffleUtil;
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderSummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionSummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductSummaryDto;
import com.parkyangji.openmarket.backend.dto.RestResponseDto;
import com.parkyangji.openmarket.backend.user.service.UserService;

import com.parkyangji.openmarket.backend.common.SessionConstants;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("api")
public class RestUserController {

  @Autowired
  private UserService userService;

  @RequestMapping("favorite/isLike")
  public RestResponseDto isUserLike(HttpSession httpSession, @RequestParam("id") int id){
    RestResponseDto responseDto = new RestResponseDto();

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    // System.out.println(id);
    if (customerDto == null) {
      responseDto.setResult("fail");
      responseDto.setReason("로그인 후 이용 가능합니다.");
    } else {
      responseDto.setResult("success");
      ProductFavoriteDto productFavoriteDto = new ProductFavoriteDto();
      productFavoriteDto.setCustomer_id(customerDto.getCustomer_id());
      productFavoriteDto.setProduct_id(id);
      
      responseDto.add("isLike", userService.toggleLike(productFavoriteDto));
    }
    return responseDto;
  }

  @RequestMapping("category")
  public RestResponseDto subCategoryProductList(HttpSession httpSession ,@RequestParam("id") List<Integer> ids){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");
    // System.out.println(id);
    
    List<ProductSummaryDto> productList = userService.getCategoryProductList(ids);
    // ShuffleUtil.shuffle(productList);
    
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    if (customerDto != null) {
      // 위에 순서 섞인 productList로 
      List<ProductFavoriteDto> likeList = userService.isLikeCheck(productList, customerDto.getCustomer_id());
      responseDto.add("likeList", likeList);
    } else {
      responseDto.add("likeList", null);
    }
        
    responseDto.add("productList", productList);

    return responseDto;
  }

  @RequestMapping("brand")
  public RestResponseDto brandProductList(HttpSession httpSession, @RequestBody Map<String, Object> params){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    String name = (String) params.get("name");
    Integer parent_category_id = (params.get("categoryId") != null) ? Integer.parseInt(params.get("categoryId").toString()) : null;

    // System.out.println(name);
    // System.out.println(categoryId);

    List<ProductSummaryDto> productList = new ArrayList<>();
    if (parent_category_id == null) {
      productList = userService.getBrandProducts(name);
    } else {
      productList = userService.getBrandProductsByCategoryId(name, parent_category_id);
    }
    // ShuffleUtil.shuffle(productList);

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    if (customerDto != null) {
      // 위에 순서 섞인 productList로 
      List<ProductFavoriteDto> likeList = userService.isLikeCheck(productList, customerDto.getCustomer_id());
      responseDto.add("likeList", likeList);
    } else {
      responseDto.add("likeList", null);
    }

    responseDto.add("productList", productList);

    return responseDto;
  }

  @RequestMapping("keywordFilter")
  public RestResponseDto keywordFilter(HttpSession httpSession, @RequestBody Map<String, Object> params){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    Map<String, Object> categorys = new HashMap<>(); // 서브 카테고리가 없다면 부모 카테고리 기준 검색(전체), 있다면 서브 카테고리 기준(탭)
    try {
      // menu_id를 String으로 가져온 후 Integer로 변환
      String menuIdStr = params.get("menu_id").toString();
      Integer menuId = Integer.parseInt(menuIdStr);
      categorys.put("parent_category_id", menuId);

      // categoryId도 동일하게 처리 (null 체크 필요)
      if (params.get("categoryId") != null) {
          String categoryIdStr = params.get("categoryId").toString();
          Integer categoryId = Integer.parseInt(categoryIdStr);
          categorys.put("sub_category_id", categoryId);
      } else {
          categorys.put("sub_category_id", null);
      }
    } catch (NumberFormatException e) {
        // 숫자 변환 오류 처리
        System.err.println("Invalid number format: " + e.getMessage());
        responseDto.setResult("fail");
        responseDto.setReason("Invalid number format in request parameters");
    }
    String keyword = (String) params.get("keyword");
    
    List<ProductSummaryDto> productList = userService.getKeywordFilter(keyword, categorys);
    
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    if (customerDto != null) {
      // 위에 순서 섞인 productList로 
      List<ProductFavoriteDto> likeList = userService.isLikeCheck(productList, customerDto.getCustomer_id());
      responseDto.add("likeList", likeList);
    } else {
      responseDto.add("likeList", null);
    }

    responseDto.add("productList", productList);

    return responseDto;
  }

  @RequestMapping("preview")
  public RestResponseDto previewProduct(HttpSession httpSession, @RequestParam("id") int product_id){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    ProductSummaryDto productData = userService.getProductDate(product_id);
    responseDto.add("product", productData);

    List<Map<String, Object>> ratingData = userService.getProductRatingChartData(product_id);
    responseDto.add("ratingData", ratingData);

    return responseDto;
  }

  @RequestMapping("product/option")
  public RestResponseDto productOption(@RequestParam("id") int product_id){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    List<ProductOptionSummaryDto> options = userService.tempoptionChoice(product_id);
    responseDto.add("options", options);

    return responseDto;
  }

  @RequestMapping("product/cart")
  public RestResponseDto handleCart(@RequestBody List<CartItemDto> selectedOptions, HttpSession httpSession) {
      RestResponseDto responseDto = new RestResponseDto();
      responseDto.setResult("success");

      // 회원 여부 확인
      CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
  
      if (customerDto != null) {
        // 회원인 경우 DB에 저장
        userService.setCartItem(customerDto, selectedOptions); // [{quantity=1, combination_id=67}]
      } else {
        // 비회원인 경우
        List<CartItemDto> existingOptions = (List<CartItemDto>) httpSession.getAttribute(SessionConstants.TEMP_CART);

        if (existingOptions == null) {
            existingOptions = new ArrayList<>();
        }

        // 새로운 데이터 병합
        userService.mergeOptions(existingOptions, selectedOptions);

        // 병합된 데이터를 세션에 저장
        httpSession.setAttribute(SessionConstants.TEMP_CART, existingOptions);
        System.out.println(existingOptions);
      }
  
      // 리다이렉트 URL 추가
      responseDto.add("message", "장바구니에 상품을 담았습니다");
  
      return responseDto;
  }

  @RequestMapping("product/cart/delete")
  public RestResponseDto deleteCartItem(HttpSession httpSession ,@RequestParam("id") int combination_id){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    // 회원 여부 확인
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    if (customerDto != null) {
      userService.removeCustomerOption(customerDto, combination_id);
    } else {
      List<CartItemDto> existingOptions = (List<CartItemDto>) httpSession.getAttribute(SessionConstants.TEMP_CART);
      
      userService.removeOptions(existingOptions, combination_id);
    }

    // 리다이렉트 URL 추가
    responseDto.add("redirectUrl", "/cart");
    return responseDto;
  }

  @RequestMapping("product/updateCartQuantity")
  public RestResponseDto updateCartItem(HttpSession httpSession , @RequestBody CartItemDto data){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    // 회원 여부 확인
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    if (customerDto != null) {
      userService.UpdateCustomerCartItem(customerDto, data);
    } else {
      List<CartItemDto> existingOptions = (List<CartItemDto>) httpSession.getAttribute(SessionConstants.TEMP_CART);
      // System.out.println(existingOptions);
      // System.out.println("받아온" + data);
      userService.UpdateTempCartItem(existingOptions, data);
    }

    // responseDto.add("redirectUrl", "/cart");
    return responseDto;
  }
  

  @RequestMapping("product/purchase")
  public RestResponseDto PurchaseProcess(HttpSession httpSession , @RequestBody List<CartItemDto> orderdata){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    // System.out.println(id);

    httpSession.setAttribute(SessionConstants.ORDER_ITEMS, orderdata);
    System.out.println("Setting orderItems in session: " + orderdata); // 디버깅 출력

    if (customerDto == null) {
      responseDto.setResult("fail");
      responseDto.setReason("로그인 후 이용 가능합니다.");
      responseDto.add("redirectUrl", "/login");
    } else {
      responseDto.add("redirectUrl", "/order");
    }
    return responseDto;
  }

  @RequestMapping("product/payment")
  public RestResponseDto PaymentProcess(HttpSession httpSession , @RequestBody Map<String, Object> addressdata){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    Integer customerId = (Integer) Integer.parseInt(addressdata.get("customerId").toString());
    Integer addressId = (Integer) Integer.parseInt(addressdata.get("addressId").toString());
    String deliveryMessage = (String) addressdata.get("deliveryMessage");

    if (customerDto.getCustomer_id() != customerId) {
      responseDto.setResult("fail");
      responseDto.setReason("주문 회원이 일치하지 않아요.");
      responseDto.add("redirectUrl", "/login");
      return responseDto;
    }

    OrderDto orderDto = userService.createOrderAndDelivery(customerDto.getCustomer_id(), addressId, deliveryMessage);

    List<CartItemDto> orderItems = (List<CartItemDto>) httpSession.getAttribute(SessionConstants.ORDER_ITEMS);

    userService.setOrderDetail(customerId, orderDto.getOrder_id(), orderItems);

    // 장바구니 리프레쉬
    userService.refreshCustomerCart(orderDto);

    responseDto.add("redirectUrl", "/orderComplete");
    return responseDto;
  }

  @RequestMapping("product/detailImages")
  public RestResponseDto productTabDetailImages(@RequestParam("id") int product_id){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    List<String> detailImages = userService.getProductDetailImages(product_id);
    responseDto.add("detailImages", detailImages);

    return responseDto;
  }

  @RequestMapping("product/review")
  public RestResponseDto productTabReview(@RequestParam("id") int product_id){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    List<Map<String,Object>> reviewData = userService.getProductReviews(product_id);
    responseDto.add("reviewData", reviewData);

    return responseDto;
  }

  @RequestMapping("product/question")
  public RestResponseDto productTabQuestion(@RequestParam("id") int product_id){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    return responseDto;
  }

  @RequestMapping("statistics/othersBestTop5")
  public RestResponseDto othersBestTop5(HttpSession httpSession, @RequestParam("id") int product_id){
    RestResponseDto responseDto = new RestResponseDto();
    
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    if (customerDto != null) {
      responseDto.setResult("success");
      List<ProductSummaryDto> othersbestTop5 = userService.getProductTop5(customerDto.getCustomer_id(), product_id);
      responseDto.add("othersbestTop5", othersbestTop5);
    } else {
      responseDto.setResult("fail");
      responseDto.setReason("로그인 하셔야 데이터를 조회할 수 있습니다");
    }
    return responseDto;
  }

  @RequestMapping("mydata/review")
  public RestResponseDto reviewData (HttpSession httpSession, @RequestParam("tab") String tab){
    RestResponseDto responseDto = new RestResponseDto();
    responseDto.setResult("success");

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    if (tab.equals("write") ) {
      List<OrderSummaryDto> writeProducts = userService.getOrderReviewWrite(customerDto.getCustomer_id());
      responseDto.add("writeProducts", writeProducts);
      responseDto.add("redirectUrl", "/mypage/review?tab=writeReview");
    }
    if (tab.equals("written")) {
      List<OrderSummaryDto> writtenProducts = userService.getOrderReviewWritten(customerDto.getCustomer_id());
      responseDto.add("writtenProducts", writtenProducts);
      responseDto.add("redirectUrl", "/mypage/review?tab=writtenReview");
    }

    return responseDto;
  }

}
