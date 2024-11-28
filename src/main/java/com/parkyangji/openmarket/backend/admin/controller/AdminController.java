package com.parkyangji.openmarket.backend.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.parkyangji.openmarket.backend.admin.service.SellerService;
import com.parkyangji.openmarket.backend.common.DebugUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkyangji.openmarket.backend.admin.service.OrderService;
import com.parkyangji.openmarket.backend.admin.service.ProductService;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

import jakarta.servlet.http.HttpSession;

// Controller: HTTP 요청/응답 처리 및 사용자와의 인터페이스를 담당.
@Controller
@RequestMapping("admin")
public class AdminController {

  @Autowired
  private ProductService productService; 
  @Autowired
  private OrderService orderService;
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
      @RequestParam("combinations") String combinationsJson, // 옵션 부분 추후 js 처리!!!
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
    List<Map<String, Object>> option_combinations;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
        option_combinations = objectMapper.readValue(combinationsJson, new TypeReference<List<Map<String, Object>>>() {});
        System.out.println("Parsed option combinations: " + option_combinations);
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to parse combinations JSON", e);
    }

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

    List<ProductDto> productList = productService.sellerProducts(sellerDto.getSeller_id());

    model.addAttribute("productList", productList);
    model.addAttribute("productListJson", DebugUtil.toJsonString(productList));

    return "admin/admin_product";
  }

  // 상품 정보 변경 - 페이지
  @RequestMapping("edit")
  public String edit(
    @RequestParam("product_id") int product_id,
    @RequestParam(value = "sub_category", required = false) Integer sub_category,
    @RequestParam(value = "title", required = false) String title,
    @RequestParam(value = "discount_rate", required = false) Integer discount_rate,
    @RequestParam(value = "change_discount_rate", required = false) Integer change_discount_rate,
    @RequestParam(value = "is_discount", required = false) String is_discount,
    @RequestParam(value = "quantity", required = false) Integer quantity,
    @RequestParam(value = "price", required = false) Integer price,
    @RequestParam(value = "combination_id", required = false) Integer combination_id,
    @RequestParam(value = "combination_value_id", required = false) Integer combination_value_id,
    @RequestParam(value = "keyword[]", required = false) List<String> keywords,
    @RequestParam(value = "deleteKeyword[]", required = false) List<String> deleteKeyword,
    Model model){

    //if (sub_category == 0) return "redirect:/admin/admin_register";

    /* 나중에는 js rest로 처리할 것!! */
    if (sub_category != null) { // 카테고리 변경
      productService.replaceProductCategoryId(product_id, sub_category);
    }
    if (title != null) { // 상품명 변경
      productService.replaceTitle(product_id, title);
    }
    if (discount_rate != null) { // 할인율 적용
      productService.registerDiscountRate(product_id, discount_rate);
    }
    if (change_discount_rate != null) { // 할인율 변경
      productService.replaceChangeDiscountRate(product_id, change_discount_rate);
    }
    if (is_discount != null) { // 할인율 종료
      productService.deleteDiscountRate(product_id);
    }
    if (quantity != null) {
      productService.replaceOptionQuantity(combination_id, quantity);
    }
    if (price != null) {
      productService.replaceOptionPrice(combination_id, price);
    }
    if (keywords != null) {
      // 키워드 인서트 안된건 해주고
      productService.saveProductKeywords(product_id, keywords);
    }
    if (deleteKeyword != null) {
      productService.deleteProductKeyword(product_id, deleteKeyword);
    }

    // 카테고리(사이트 관리자 지정) 모두 불러오기
    model.addAttribute("categoryList", productService.getAllCategory());

    // 키워드(사이트 관리자 지정) 불러오기
    model.addAttribute("keywords", productService.getAllKeyword());

    Map<String, Object> productEdit = productService.getProductDetail(product_id);

    model.addAllAttributes(productEdit);
    model.addAttribute("data", DebugUtil.toJsonString(productEdit));

    model.addAttribute("product_id", product_id);

    return "admin/admin_edit";
  }

  //주문 조회
  @RequestMapping("order")
  public String orders(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");
    
    model.addAttribute("orderList", orderService.getAllOrders(sellerDto.getSeller_id()));
    model.addAttribute("data", DebugUtil.toJsonString(orderService.getAllOrders(sellerDto.getSeller_id())));
    // System.out.println(orderService.getAllOrders(sellerDto.getSeller_id()));
    return "admin/admin_order";
  }

  @RequestMapping("updateStatus")
  public String updateStatus(
    @RequestParam("order_detail_id") int order_detail_id, 
    @RequestParam("status") String status
    ){
    System.out.println(order_detail_id);
    System.out.println(status);

    orderService.updateOrderStatus(order_detail_id, status);
    
    return "redirect:./order";
  }

  @RequestMapping("review")
  public String review(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    List<Map<String, Object>> reviewList = sellerService.getAllReviews(sellerDto.getSeller_id());
    // System.out.println(reviewList);
    
    model.addAttribute("reviewList", reviewList);
    model.addAttribute("reviewListJson", DebugUtil.toJsonString(reviewList));

    return "admin/admin_review";
  }

  @RequestMapping("replyProcess")
  public String replyProcess(
    @RequestParam("order_review_id") int order_review_id,
    @RequestParam("seller_reply") String seller_reply
  ){
    sellerService.addReply(order_review_id, seller_reply);
    return "redirect:/admin/review";
  }
}
