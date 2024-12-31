package com.parkyangji.openmarket.backend.user.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parkyangji.openmarket.backend.common.DebugUtil;
import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.BrandSummaryDto;
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.KeywordDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionSummaryDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderSummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductSummaryDto;
import com.parkyangji.openmarket.backend.user.service.UserService;
import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;

import com.parkyangji.openmarket.backend.common.SessionConstants;

import jakarta.servlet.http.HttpSession;

@Controller
//@RequestMapping("user")
public class UserController {

  @Autowired
  private UserService userService;

  @RequestMapping("")
  public String homePage(HttpSession httpSession, Model model){
    
    System.out.println("home "+httpSession.getAttribute(SessionConstants.USER_INFO));

    List<String> exList = Arrays.asList("SlideType/Main/main_slide1.jpg", "SlideType/Main/main_slide2.jpg", "SlideType/Main/main_slide3.jpg", "SlideType/Main/main_slide4.webp");
    model.addAttribute("exList", exList);

    // 상단 슬라이드
    return "user/main";
  }

  @RequestMapping("menu")
  public String menuPage(){

    return "user/menu";
  }

  @RequestMapping("login")
  public String loginPage(){

    return "user/login";
  }

  @RequestMapping("mypage")
  public String myPage(){

    return "user/mypage";
  }

  @RequestMapping("userRegister")
  public String userRegisterPage(){
    return "user/register";
  }

  @RequestMapping("loginProcess")
  public String loginProcess(HttpSession httpSession, CustomerDto params) {
    
    // 로그인 확인 하기 sql
    CustomerDto customerDto = userService.loginCheck(params);
    if (customerDto == null) {
      return "redirect:/login"; // 유효성 검사 필요
    }

    httpSession.setAttribute(SessionConstants.USER_INFO, customerDto);

    // 비회원 장바구니 아이템을 확인하고 로그인된 사용자의 장바구니로 이동
    List<CartItemDto> UnUserTempCart = (List<CartItemDto>) httpSession.getAttribute(SessionConstants.TEMP_CART);
    // System.out.println(UnUserTempCart);
    if (UnUserTempCart != null && !UnUserTempCart.isEmpty()) {
      userService.setCartItem(customerDto, UnUserTempCart);
      
      httpSession.removeAttribute(SessionConstants.TEMP_CART);
    }

    return "redirect:/";
  }

  @RequestMapping("logout")
  public String logout(HttpSession httpSession){

    httpSession.invalidate(); // 세션 재구성 

    return "redirect:/";
  }

  @RequestMapping("registerProcess")
  public String userRegisterProcess(
    @RequestParam("username") String username,
    @RequestParam("password") String password,
    @RequestParam("nickname") String nickname,
    @RequestParam("name") String name,
    @RequestParam("gender") String gender,
    @RequestParam("phone") String phone,
    @RequestParam("address") String address){

    CustomerDto customerDto = new CustomerDto();
    customerDto.setUsername(username);
    customerDto.setPassword(password);
    customerDto.setNickname(nickname);

    AddressDto addressDto = new AddressDto();
    addressDto.setName(name);
    addressDto.setPhone(phone);
    addressDto.setAddress(address);

    userService.userRegister(customerDto, addressDto);

    return "redirect:/";
  }

  @RequestMapping("category")
  public String productListPage(Model model, HttpSession httpSession, @RequestParam("menu_id") Integer parent_category_id){
    //System.out.println("카테고리넘버"+ category_id);

    // 상단이름
    String category_name = userService.getCategoryName(parent_category_id);
    model.addAttribute("category_name", category_name);

    // 서브 카테고리들
    List<Map<String, Object>> subCategoryIdAndName = userService.getSubTabCategorys(parent_category_id);
    model.addAttribute("sub_categorys", subCategoryIdAndName);

    // 키워드
    List<KeywordDto> keywords = userService.getAllKeywords();
    model.addAttribute("keywords", keywords);

    // => 상품 리스트 ajax로!!

    // 디버깅용
    model.addAttribute("subCategoryIdAndName", DebugUtil.toJsonString(subCategoryIdAndName));
    model.addAttribute("keywords", DebugUtil.toJsonString(keywords));

    return "user/productList";
  }

  @RequestMapping("brand")
  public String brandPage(@RequestParam("name") String name, Model model){
    System.out.println(name);

    model.addAttribute("storename", name);

    List<Map<String, Object>> subCategoryIdAndName = userService.getSubTabCategorys(null);
    List<String> categoryNames = new ArrayList<>();
    for (Map<String, Object> category : subCategoryIdAndName) {
      categoryNames.add((String) category.get("category_name"));
    }
    model.addAttribute("sub_categorys", categoryNames);

    // 디버깅용
    model.addAttribute("storenameJson", DebugUtil.toJsonString(name));
    model.addAttribute("subCategoryIdAndNameJson", DebugUtil.toJsonString(subCategoryIdAndName));

    return "user/brand";
  }

  @RequestMapping("product")
  public String productPage(HttpSession httpSession, Model model, @RequestParam("id") int product_id){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    if (customerDto != null) {
      ProductFavoriteDto productFavoriteDto = new ProductFavoriteDto();
      productFavoriteDto.setCustomer_id(customerDto.getCustomer_id());
      productFavoriteDto.setProduct_id(product_id);

      ProductFavoriteDto likeData = userService.findLike(productFavoriteDto);
      model.addAttribute("likeData", likeData);
      model.addAttribute("likeDataJson", DebugUtil.toJsonString(likeData));
    }

    ProductSummaryDto productData = userService.getProductDate(product_id);
    model.addAttribute("product", productData);
    model.addAttribute("productDataJson", DebugUtil.toJsonString(productData));

    return "user/product";
  }

  @RequestMapping("/mypage/like")
  public String myLike(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    List<Map<String, Object>> likeList = userService.getLikeList(customerDto.getCustomer_id());

    model.addAttribute("likeList", likeList);
    System.out.println(likeList);

    return "user/myLike";
  }

  @RequestMapping("/mypage/orderDetail")
  public String myorderDetail(HttpSession httpSession , @RequestParam("order_id") int order_id, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    OrderSummaryDto orderDetail = userService.findOrderDetailByOrderId(customerDto, order_id);

    model.addAttribute("orderdetail", orderDetail);
    model.addAttribute("orderdetailJson", DebugUtil.toJsonString(orderDetail));

    return "user/myOrderDetail";
  }


  @RequestMapping("/mypage/order")
  public String myorder(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    
    // 주문 정보 + 상품 정보
    List<OrderSummaryDto> orderItems = userService.getOrderSummaryList(customerDto.getCustomer_id());

    model.addAttribute(SessionConstants.ORDER_ITEMS, orderItems);
    model.addAttribute("orderItemsJson", DebugUtil.toJsonString(orderItems));

    return "user/myOrder";
  }

  @RequestMapping("/mypage/review")
  public String myreview(HttpSession httpSession, Model model){

    // 데이터가 크지 않다면: 한꺼번에 데이터를 전달받아 클라이언트에서 분리. => 이 방식!!
    // 데이터가 크거나 확장성이 중요하다면: AJAX 기반으로 탭별로 데이터를 분리 요청.

    //CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    // 주문 정보 + 상품 정보
    //List<OrderSummaryDto> orderItems = userService.getOrderSummaryList(customerDto.getCustomer_id());

    // model.addAttribute(SessionConstants.ORDER_ITEMS, orderItems);
    // model.addAttribute("orderItemsJson", DebugUtil.toJsonString(orderItems));

    return "user/myReview";
  }

  @RequestMapping("ratingProcess")
  public String ratingProcess(HttpSession httpSession, 
    @RequestParam("rating") int rating, 
    @RequestParam("order_detail_id") int order_detail_id) {

    userService.saveRating(order_detail_id, rating);
    
    return "redirect:/mypage/review";
  }

  @RequestMapping("reviewContentProcess")
  public String reviewContentProcess(HttpSession httpSession, 
    @RequestParam("review_content") String review_content, 
    @RequestParam("order_detail_id") int order_detail_id) {
    
    userService.saveReviwContent(order_detail_id, review_content);
    
    return "redirect:/mypage/review?tab=writtenReview";
  }


  @RequestMapping("cart")
  public String cartPage(Model model, HttpSession httpSession){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    List<BrandSummaryDto> cartItems = null;
    if (customerDto != null) {
      cartItems = userService.getCustomerCartItem(customerDto.getCustomer_id());

      model.addAttribute("cartItems", cartItems);
      // 디버깅용
      model.addAttribute("cartItemsJson", DebugUtil.toJsonString(cartItems));
    } else {
      // 세션에서 선택한 옵션 데이터를 가져옴
      List<CartItemDto> selectedOptions = (List<CartItemDto>) httpSession.getAttribute(SessionConstants.TEMP_CART);
      if (selectedOptions != null) {
        cartItems = userService.getRevertCartData(selectedOptions);
        
        model.addAttribute("cartItems", cartItems);
        // 디버깅용
        model.addAttribute("cartItemsJson", DebugUtil.toJsonString(cartItems));
      }
    }

    return "user/cart";
  }


  @RequestMapping("order")
  public String orderPage(HttpSession httpSession, Model model) {
    // 주문 상품 정보 가져오기
    List<BrandSummaryDto> orderItems = userService.getRevertCartData((List<CartItemDto>) httpSession.getAttribute(SessionConstants.ORDER_ITEMS));
    
    if (orderItems == null || orderItems.isEmpty()) {
        return "redirect:/cart"; // 주문 데이터가 없으면 장바구니로 리다이렉트
    }

    // 고객 정보 가져오기 (로그인 여부 확인)
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    // 배송지 정보 가져오기 (회원만 가능)
    AddressDto addressDto = userService.getDefaultAddress(customerDto.getCustomer_id());

    // 고객 배송지 목록 => 나중에 버튼 추가시
    //List<String> address = userService.getAddressList(customerDto.getCustomer_id());

    // 모델에 데이터 추가
    model.addAttribute(SessionConstants.ORDER_ITEMS, orderItems); // 주문 상품
    model.addAttribute("customerDto", customerDto); // 고객 정보
    model.addAttribute("addressDto", addressDto); // 배송지 정보
    model.addAttribute("orderItemsJson", DebugUtil.toJsonString(orderItems));

    return "user/order"; // 주문 페이지로 이동
  }
  

  @RequestMapping("orderComplete")
  public String orderComplete(HttpSession httpSession, Model model){

    List<BrandSummaryDto> orderItems = userService.getRevertCartData((List<CartItemDto>) httpSession.getAttribute(SessionConstants.ORDER_ITEMS));
    model.addAttribute(SessionConstants.ORDER_ITEMS, orderItems);

    // 주문 완료 후 세션 삭제
    httpSession.removeAttribute(SessionConstants.ORDER_ITEMS);
    
    return "user/orderComplete";
  }

  @RequestMapping("order/address")
  public String orderAddress(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);
    
    List<AddressDto> allAddress = userService.getUserAddress(customerDto);

    model.addAttribute("allAddress", allAddress);

    return "user/orderAddress";
  }

  @RequestMapping("order/addressAdd")
  public String userAddressAdd(HttpSession httpSession,
    AddressDto addressDto, @RequestParam(value = "is_default", required = false, defaultValue = "false") boolean is_default,
    @RequestParam("default_address") String default_address, @RequestParam("detail_address") String detail_address, @RequestParam("zipp_code") int zipp_code){
  
    // // 고객 정보 가져오기 (로그인 여부 확인)
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute(SessionConstants.USER_INFO);

    String addressRevert = default_address + ", " + detail_address + " [" + zipp_code + "]";

    addressDto.set_default(is_default);
    userService.addAddress(addressDto, customerDto , addressRevert);

    return "redirect:/order/address";
  }
}
