package com.parkyangji.openmarket.backend.user.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
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
    @RequestParam("gender") String gender,
    @RequestParam("phone") String phone,
    @RequestParam("address") String address){

    CustomerDto customerDto = new CustomerDto();
    customerDto.setUsername(username);
    customerDto.setPassword(password);
    customerDto.setNickname(nickname);
    customerDto.setGender(gender);
    customerDto.setPhone(phone);

    AddressDto addressDto = new AddressDto();
    addressDto.setAddress(address);

    userService.userRegister(customerDto, addressDto);

    return "redirect:/home";
  }

  @RequestMapping("category")
  public String productListPage(
    Model model,
    @RequestParam("depth") int depth, 
    @RequestParam("menu_id") int category_id){

    System.out.println("카테고리뎁스"+ depth);
    System.out.println("카테고리넘버"+ category_id);

    List<String> productList = Arrays.asList("ProductImage/Women/Top/Topten/3599319_16995905976345_big.webp", "ProductImage/Women/Top/Topten/3588843_16995835333549_big.webp", "ProductImage/Women/Top/Topten/3587760_16953655495488_big.webp", "ProductImage/Women/Top/Topten/3327837_16850679163243_big.webp");
    model.addAttribute("productList", productList);

    return "user/productList";
  }

  @RequestMapping("product")
  public String productPage(@RequestParam("id") int product_id, Model model){
    System.out.println("상품번호" + product_id);

    Map<String, Object> productData = userService.getProductDate(product_id);
    model.addAllAttributes(productData);

    System.out.println("상품 페이지");
    System.out.println(productData);
    return "user/product";
  }
  
}
