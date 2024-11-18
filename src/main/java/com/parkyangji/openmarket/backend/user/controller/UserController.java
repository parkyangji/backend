package com.parkyangji.openmarket.backend.user.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;
import com.parkyangji.openmarket.backend.user.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
//@RequestMapping("user")
public class UserController {

  @Autowired
  private UserService userService;

  @RequestMapping("home")
  public String homePage(HttpSession httpSession, Model model){
    
    System.out.println("home "+httpSession.getAttribute("sessionInfo"));

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
      return "redirect:/login"; // 나중에 js 처리!!!!!!
    }

    httpSession.setAttribute("sessionInfo", customerDto);


    return "redirect:/home";
  }

  @RequestMapping("logout")
  public String logout(HttpSession httpSession){

    httpSession.invalidate(); // 세션 재구성 

    return "redirect:/home";
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
    customerDto.setName(name);
    customerDto.setGender(gender);
    customerDto.setPhone(phone);

    AddressDto addressDto = new AddressDto();
    addressDto.setAddress(address);

    userService.userRegister(customerDto, addressDto);

    return "redirect:/home";
  }

  @RequestMapping("category")
  public String productListPage(Model model, HttpSession httpSession, @RequestParam("menu_id") int category_id){

    //System.out.println("카테고리넘버"+ category_id);

    boolean isLoggedIn = httpSession.getAttribute("sessionInfo") != null;
    model.addAttribute("isLoggedIn", isLoggedIn);

    Map<String, Object> productListData = userService.getCategoryProductList(category_id);
    model.addAllAttributes(productListData);

    return "user/productList";
  }

  @RequestMapping("product")
  public String productPage(HttpSession httpSession, Model model, @RequestParam("id") int product_id){
    //System.out.println("상품번호" + product_id);

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
    if (customerDto != null) {
      ProductFavoriteDto productFavoriteDto = new ProductFavoriteDto();
      productFavoriteDto.setCustomer_id(customerDto.getCustomer_id());
      productFavoriteDto.setProduct_id(product_id);

      ProductFavoriteDto likeData = userService.findLike(productFavoriteDto);
      model.addAttribute("likeData", likeData);
    }

    Map<String, Object> productData = userService.getProductDate(product_id);
    model.addAllAttributes(productData);

    // System.out.println("상품 페이지");
    // System.out.println(productData);
    return "user/product";
  }
  
  @RequestMapping("order")
  public String orderPage(HttpSession httpSession, Model model, @RequestParam("productId") int product_id){
    if (httpSession.getAttribute("sessionInfo")==null) {
      return "redirect:/login";
    }
    // 로그인 고객 정보
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    // 고객 배송지 목록
    List<String> address = userService.getAddressList(customerDto.getCustomer_id());

    // 전화번호, 주소, 기본 배송지

    // 주문 상품, 주문 개수

    // 할인 (쿠폰, 포인트 등)

    // 결제 수단

    // 결제 금액

    model.addAttribute("product_id", product_id);
    model.addAttribute("customer_id", customerDto.getCustomer_id());
    model.addAllAttributes(userService.getProductDate(product_id));
    model.addAttribute("address", address);
    model.addAttribute("customerDto", customerDto);
    
    return "user/order";
  }

  @RequestMapping("orderProcess")
  public String orderProcess(
    @RequestParam("productId") int product_id,
    @RequestParam("customerId") int customer_id,
    @RequestParam("quantity") int quantity,
    @RequestParam("delivery_address") String delivery_address
  ){
    // 재고가 모자라 주문이 완료되지 않았습니다!!!!! 넣기
    ProductDto productDto = (ProductDto) userService.getProductDate(product_id).get("productInfo");
    // if (productDto.getTotal_quantity() < quantity) {
    //   return "redirect:/home"; // 재고 모자르면 주문 완료 x
    // }

    //제품 번호, 수량 정보 => 나중에 옵션 추가하면 변경, 배송지, 진행상태("결제완료") => 결제수단 따로 있으면 "결제대기"
    OrderDto productOrderDto = new OrderDto();
    //productOrderDto.setProduct_id(product_id);
    productOrderDto.setCustomer_id(customer_id);
    //productOrderDto.setQuantity(quantity); 
    //productOrderDto.setDelivery_address(delivery_address);
    productOrderDto.setStatus("결제완료");

    userService.createOrderAndUpdateCount(productOrderDto);

    return "redirect:/orderComplete";
  }

  @RequestMapping("orderComplete")
  public String orderComplete(){
    return "user/orderComplete";
  }
  
  @RequestMapping("likeProgress")
  public String likeProcess(HttpSession httpSession, Model model, @RequestParam("productId") int product_id){
    if (httpSession.getAttribute("sessionInfo")==null) {
      return "redirect:/login";
    }

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    ProductFavoriteDto productFavoriteDto = new ProductFavoriteDto();
    productFavoriteDto.setCustomer_id(customerDto.getCustomer_id());
    productFavoriteDto.setProduct_id(product_id);
    
    userService.toggleLike(productFavoriteDto);
    
    return "redirect:/product?id=" + product_id;
  }

  @RequestMapping("/mypage/like")
  public String myLike(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    List<Map<String, Object>> likeList = userService.getLikeList(customerDto.getCustomer_id());

    model.addAttribute("likeList", likeList);

    return "user/myLike";
  }


  @RequestMapping("/mypage/order")
  public String myorder(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
    
    // 주문 정보 + 상품 정보
    List<Map<String, Object>> orderList = userService.getOrderList(customerDto.getCustomer_id());

    // System.out.println(orderList);
    model.addAttribute("orderList", orderList);

    return "user/myOrder";
  }

  @RequestMapping("/mypage/review")
  public String myreview(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    // 주문 정보 + 상품 정보
    List<Map<String, Object>> orderList = userService.getOrderList(customerDto.getCustomer_id());

    model.addAttribute("orderList", orderList);
    // System.out.println(orderList);

    model.addAttribute("hasReviewsState", userService.hasReviewsState(customerDto.getCustomer_id()));

    return "user/myReview";
  }

  @RequestMapping("/ratingProcess")
  public String ratingProcess(HttpSession httpSession, 
    @RequestParam("rating") int rating, 
    @RequestParam("order_id") int order_id,
    @RequestParam("product_id") int product_id) {

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    ProductReviewDto productReviewDto = new ProductReviewDto();
    productReviewDto.setProduct_id(product_id);
    productReviewDto.setCustomer_id(customerDto.getCustomer_id());
    productReviewDto.setOrder_id(order_id);
    productReviewDto.setRating(rating);

    userService.saveOrUpdateReview(productReviewDto);
    
    return "redirect:/mypage/review";
  }

  @RequestMapping("/reviewContentProcess")
  public String reviewContentProcess(HttpSession httpSession, 
    @RequestParam("content") String content, 
    @RequestParam("order_id") int order_id,
    @RequestParam("product_id") int product_id) {

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    ProductReviewDto productReviewDto = new ProductReviewDto();
    productReviewDto.setProduct_id(product_id);
    productReviewDto.setCustomer_id(customerDto.getCustomer_id());
    productReviewDto.setOrder_id(order_id);
    productReviewDto.setContent(content);

    userService.saveOrUpdateReview(productReviewDto);
    
    return "redirect:/mypage/review";
  }

  @RequestMapping("cart")
  public String cart(){
    return "user/cart";
  }
}
