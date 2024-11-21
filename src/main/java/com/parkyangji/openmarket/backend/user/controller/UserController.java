package com.parkyangji.openmarket.backend.user.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.javassist.tools.framedump;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;
import com.parkyangji.openmarket.backend.user.service.UserService;
import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;

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
    // customerDto.setName(name);
    // customerDto.setGender(gender);
    // customerDto.setPhone(phone);

    AddressDto addressDto = new AddressDto();
    addressDto.setName(name);
    addressDto.setPhone(phone);
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
    System.out.println(likeList);

    return "user/myLike";
  }


  @RequestMapping("/mypage/order")
  public String myorder(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
    
    // 주문 정보 + 상품 정보
    Map<Integer, Map<String, Map<Integer, List<OrderItemReturnDto>>>> orderItems = userService.getOrderList(customerDto.getCustomer_id());

    model.addAttribute("orderItems", orderItems);
    System.out.println(orderItems);

    return "user/myOrder";
  }

  @RequestMapping("/mypage/review")
  public String myreview(HttpSession httpSession, Model model){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    // 주문 정보 + 상품 정보
    Map<Integer, Map<String, Map<Integer, List<OrderItemReturnDto>>>> orderItems = userService.getOrderList(customerDto.getCustomer_id());

    model.addAttribute("orderItems", orderItems);
    // System.out.println(orderItems);

    //model.addAttribute("hasReviewsState", userService.hasReviewsState(orderItems));

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


  @RequestMapping("brand")
  public String brandPage(@RequestParam("name") String store_eng_name){
    // System.out.println(store_eng_name);

    return "user/brand";
  }

  @RequestMapping("temp_option")
  public String tempOptionChoice(@RequestParam("productId") int productId, Model model){
    // System.out.println(productId);
    model.addAttribute("productId", productId);
    model.addAllAttributes(userService.tempoptionChoice(productId));
    return "user/temp_option";
  }

  @RequestMapping("temp_cart")
  public String temp_order(Model model, 
    HttpSession httpSession,
    @RequestParam("productId") int productId, 
    @RequestParam(value = "combination_id[]", required = false) List<Integer> combination_id,
    @RequestParam(value = "quantity[]", required = false) List<Integer> quantity
  ){
    // System.out.println(productId);
    // System.out.println(combination_id);
    // System.out.println(quantity);

    // 임시, 수량 0개로 넣었을때 & 체크한 값이 없을때
    boolean nullcheck = quantity.stream().allMatch(count -> count <= 0 || count == null);
    if (nullcheck || combination_id == null) return "redirect:/product?id=" + productId; 

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    // 가공 => 나중에 js로 처리
    List<Integer> newCombinationId = new ArrayList<>();
    List<Integer> newQuantity = new ArrayList<>();
    for (int i=0; i<quantity.size(); i++) {
      if (quantity.get(i) != 0 || quantity.get(i) == null) { // 타임리프는 배열을 인덱스로 접근가능
        newQuantity.add(quantity.get(i));
        newCombinationId.add(combination_id.get(i));
      }
    }
    
    if (customerDto != null) {
      userService.setCartItem(customerDto, productId, newCombinationId, newQuantity);
    } else {
        // 로그인되지 않은 사용자의 경우 세션에 장바구니 정보 저장
        // 기존 세션에서 장바구니를 가져옵니다.
        Map<Integer, Map<Integer, List<CartItemReturnDto>>> cartItems = (Map<Integer, Map<Integer, List<CartItemReturnDto>>>) httpSession.getAttribute("tempCart");

        // 만약 세션에 장바구니가 없다면 새로운 맵을 생성합니다.
        if (cartItems == null) {
            cartItems = new HashMap<>();
        }

        // 새로운 항목들을 장바구니에 추가합니다.
        Map<Integer, Map<Integer, List<CartItemReturnDto>>> pushItems = userService.getTempCartItem(productId, newCombinationId, newQuantity);
        
        // 서비스 계층에서 기존 장바구니와 새로운 항목 병합 처리
        cartItems = userService.updateTempCartItem(cartItems, pushItems);

        // 업데이트된 장바구니를 다시 세션에 저장합니다.
        httpSession.setAttribute("tempCart", cartItems);
    }

    return "redirect:/cart";
  }

  @RequestMapping("cart")
  public String cartPage(Model model, HttpSession httpSession){

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");

    if (customerDto != null) {
      Map<Integer, Map<Integer, List<CartItemReturnDto>>> cartItems = userService.getCustomerCartItem(customerDto.getCustomer_id());
      model.addAttribute("cartItems", cartItems);
      model.addAttribute("totalPrice", userService.totalOrderItemsPrice(cartItems));
    } else {
      Map<Integer, Map<Integer, List<CartItemReturnDto>>> cartItems = (Map<Integer, Map<Integer, List<CartItemReturnDto>>>) httpSession.getAttribute("tempCart");
      model.addAttribute("cartItems", cartItems);
      if (cartItems != null) {
        model.addAttribute("totalPrice", userService.totalOrderItemsPrice(cartItems));
      }
    }

    return "user/cart";
  }

  
  @RequestMapping("order")
  public String orderPage(HttpSession httpSession, Model model){

    // 로그인 고객 정보
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
    // System.out.println(customerDto);
    // 고객 배송지 목록 => 나중에 버튼 추가시
    //List<String> address = userService.getAddressList(customerDto.getCustomer_id());

    // 전화번호, 주소, 기본 배송지
    AddressDto addressDto = userService.getDefaultAddress(customerDto.getCustomer_id());

    // 주문 상품, 주문 개수
    // 그 고객의 장바구니 아이템 모두 보여주기!!!
    Map<Integer, Map<Integer, List<CartItemReturnDto>>> cartItems = userService.getCustomerCartItem(customerDto.getCustomer_id());
   // Map<Integer, List<CartItemReturnDto>> cartItems = userService.getCustomerCartItem(customerDto.getCustomer_id());

    // 할인 (쿠폰, 포인트 등)

    // 결제 수단

    // 결제 금액

    // model.addAttribute("product_id", product_id);
    model.addAttribute("addressDto", addressDto);
    // model.addAllAttributes(userService.getProductDate(product_id));
    // model.addAttribute("address", address);
    model.addAttribute("customerDto", customerDto);
    model.addAttribute("cartItems", cartItems);
    model.addAttribute("totalPrice", userService.totalOrderItemsPrice(cartItems));
    
    return "user/order";
  }

  @RequestMapping("orderProcess")
  public String orderProcess(
    HttpSession httpSession,
    @RequestParam("customerId") int customer_id,
    @RequestParam("addressId") int address_id,
    @RequestParam("deliveryMessage") String deliveryMessage,
    RedirectAttributes redirectAttributes
  ){
    //리다이렉트 시 데이터 전달 문제 해결: Model은 리다이렉트 후에는 사용할 수 없기 때문에, 리다이렉트 후 데이터를 전달하기 위한 대안으로 플래시 속성이 사용됩니다.

    //제품 번호, 수량 정보 => 나중에 옵션 추가하면 변경, 배송지, 진행상태("결제완료") => 결제수단 따로 있으면 "결제대기"
    OrderDto orderDto = userService.createOrderAndDelivery(customer_id, address_id, deliveryMessage);
    userService.setOrderDetail(customer_id, orderDto.getOrder_id());

    redirectAttributes.addFlashAttribute("order", orderDto);

    return "redirect:/orderComplete";
  }

  @RequestMapping("orderComplete")
  public String orderComplete(@ModelAttribute("order") OrderDto order, Model model){
    System.out.println(order);
    
    // order가 있는 detail 에 combination_id 지우기!!!
    if (order != null) {
      userService.refreshCustomerCart(order);
    }
    
    model.addAttribute("order", order);
    
    return "user/orderComplete";
  }
}
