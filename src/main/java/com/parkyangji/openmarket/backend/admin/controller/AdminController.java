package com.parkyangji.openmarket.backend.admin.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.checkerframework.checker.units.qual.s;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.parkyangji.openmarket.backend.admin.service.SellerService;
import com.parkyangji.openmarket.backend.admin.service.ProductService;
import com.parkyangji.openmarket.backend.config.ImageConfig;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductImageDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

import jakarta.servlet.http.HttpSession;

// Controller: HTTP 요청/응답 처리 및 사용자와의 인터페이스를 담당.
@Controller
@RequestMapping("admin")
public class AdminController {

  @Autowired
  private ProductService productService; 

  @Autowired
  private SellerService sellerService; // 어드민 서비스


  @RequestMapping("dashboard")
  public String dashboardPage(HttpSession httpSession, Model model){

    System.out.println("admin "+httpSession.getAttribute("sellerInfo"));

    return "admin/admin_dashboard";
  }

  @RequestMapping("")
  public String loginPage(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    model.addAttribute("sellerInfo", sellerDto);
    
    return "admin/admin_login";
  }

  @RequestMapping("loginProcess")
  public String loginProcess(HttpSession httpSession, SellerDto params){

    SellerDto sellerDto = sellerService.loginCheck(params);

    httpSession.setAttribute("sellerInfo", sellerDto);

    return "redirect:/admin/dashboard";
  }

  @RequestMapping("logout")
  public String logout(HttpSession httpSession){

    httpSession.invalidate(); // 세션 재구성 

    return "redirect:/admin";
  }

  @RequestMapping("register")
  public String sellerRegisterPage(){
    return "admin/admin_register";
  }

  @RequestMapping("registerProcess")
  public String sellerRegisterProcess(SellerDto params){
    // System.out.println(params);
    sellerService.sellerRegister(params);
    return "redirect:/admin";
  }

  // 상품 등록 
  @RequestMapping("productRegister")
  public String productRegisterPage(Model model){

    // 카테고리(사이트 관리자 지정) 모두 불러오기
    model.addAttribute("categoryList", productService.getAllCategory());
    
    // 키워드(사이트 관리자 지정) 불러오기
    model.addAttribute("keywords", productService.getAllKeyword());


    return "admin/admin_productRegister";
  }

  @RequestMapping("productRegisterProcess")
  public String productRegisterProcess(
      @RequestParam("seller_id") int seller_id,
      @RequestParam("sub_category") int sub_category, 
      @RequestParam("title") String title,
      @RequestParam(value = "thumbnail", required = false) MultipartFile[] thumbnailImages,
      @RequestParam(value = "detail", required = false) MultipartFile[] detailImages,
      @RequestParam(value = "keyword[]", required = false) List<String> keywords,
      @RequestParam("option_name[]") List<String> option_name, // 옵션 부분 추후 js 처리!!!
      @RequestParam("option_name_value1[]") List<String> option_value1, 
      @RequestParam("option_name_value2[]") List<String> option_value2,
      @RequestParam("quantity[]") List<Integer> quantity,
      @RequestParam("price[]") List<Integer> price
    ){

    if (sub_category == 0) return "redirect:/admin/admin_register";

    // 1. 상품dto 
    ProductDto params = new ProductDto();
    params.setCategory_id(sub_category); // 부모 카테고리는 정해져 있음
    params.setSeller_id(seller_id);
    params.setTitle(title);

    // 2. 이미지
    Map<String, Object> imageList = new HashMap<>();
    if (thumbnailImages[0].isEmpty() && detailImages[0].isEmpty()) {
      imageList = null;
    } else {
      imageList.put("thumbnail", thumbnailImages); 
      imageList.put("detail", detailImages);
    }
    // 3. 키워드
    //System.out.println(keywords);

    // 4. 옵션이름, 옵션값, 재고수량, 가격 

    // combinations: [["S", "빨강"], ["S", "파랑"], ["M", "빨강"], ["M", "파랑"]]
    // quantity[]: [100, 50, 80, 60]
    // price[]: [10000, 11000, 12000, 13000]

    // HashMap 생성 => 나중에 js 동적 예정 !!!!!
    /*
    [
      {이름 : 값[] },
      {이름 : 값[] },
      {이름 : 값[] }, ...
    ]
    */
    // 임시 json처럼 생성
    List<Map<String, Object>> option_combinations = new ArrayList<>();

    for (String name : option_name) {
      Map<String, Object> option = new HashMap<>();
      if (name.equals("사이즈") ) {
        option.put(name, option_value1);
      }
      if (name.equals("색상")) {
        option.put(name, option_value2);
      }
      option_combinations.add(option);
    }
    //
    //System.out.println(option_combinations);

    int option_cases_count = 1;
    for (Map<String, Object> map : option_combinations) {
        for (Object values : map.values()) {
          option_cases_count *= ((List<?>) values).size();
        }
    }

    if (option_cases_count == quantity.size() &&  quantity.size() == price.size()) {
      productService.registerProduct(params , imageList, keywords, option_combinations, quantity, price);
    } else {
      throw new IllegalArgumentException("option_combinations, quantity, and price lists must have the same size.");
    }

    return "redirect:/admin/dashboard";
  }


  
  // 상품 조회
  @RequestMapping("products")
  public String products(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    //List<ProductDto> productList = sellerService.sellerProducts(sellerDto.getSeller_id());

    //model.addAttribute("productList", productList);

    return "admin/admin_product";
  }

  //주문 조회
  @RequestMapping("order")
  public String orders(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    List<Map<String, Object>> orderList = sellerService.getAllOrders(sellerDto.getSeller_id());
    // System.out.println(orderList);
    
    model.addAttribute("orderList", orderList);

    return "admin/admin_order";
  }

  @RequestMapping("review")
  public String review(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    List<Map<String, Object>> reviewList = sellerService.getAllReviews(sellerDto.getSeller_id());
    // System.out.println(reviewList);
    
    model.addAttribute("reviewList", reviewList);

    return "admin/admin_review";
  }

  @RequestMapping("replyProcess")
  public String replyProcess(
    @RequestParam("review_id") int review_id,
    @RequestParam("seller_reply") String seller_reply
  ){
    sellerService.addReply(review_id, seller_reply);
    return "redirect:/admin/review";
  }
}
