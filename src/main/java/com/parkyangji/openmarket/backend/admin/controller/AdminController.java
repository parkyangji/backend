package com.parkyangji.openmarket.backend.admin.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.parkyangji.openmarket.backend.admin.service.AdminService;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("admin")
public class AdminController {

  @Autowired
  private AdminService adminService;

  @RequestMapping("dashboard")
  public String dashboardPage(HttpSession httpSession, Model model){

    System.out.println("admin "+httpSession.getAttribute("sellerInfo"));

    return "admin/admin_dashboard";
  }

  @RequestMapping("")
  public String loginPage(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");
    if (sellerDto!=null){
      return "redirect:/admin/dashboard";
    }

    model.addAttribute("sellerInfo", sellerDto);
    
    return "admin/admin_login";
  }

  @RequestMapping("loginProcess")
  public String loginProcess(HttpSession httpSession, SellerDto params){

    SellerDto sellerDto = adminService.loginCheck(params);
    
    if (sellerDto == null) {
      return "redirect:/admin"; // 나중에 js 처리!!!!!!
    }

    httpSession.setAttribute("sellerInfo", sellerDto);

    return "redirect:/admin/dashboard";
  }

  @RequestMapping("logout")
  public String logout(HttpSession httpSession){

    httpSession.invalidate(); // 세션 재구성 

    return "redirect:/admin";
  }

  @RequestMapping("registerProcess")
  public String sellerRegisterProcess(){

    return "redirect:/admin";
  }

  @RequestMapping("productRegisterProcess")
  public String productRegisterProcess(ProductDto params ,@RequestParam("uploadFiles") MultipartFile uploadFile){
    
    // 폴더에 업로드!!!
    String rootPath = "/Users/parkyangji/uploadFiles/";

    String folder = "ProductImage/" + params.getSeller_id() + "/" + params.getCategory_id() + "/";

    File createFolder = new File(rootPath+folder);

    if (!createFolder.exists()) {
      createFolder.mkdirs();
    }

    String originalFilename = uploadFile.getOriginalFilename();

    // 확장자명 추출
    String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
    
    String uuid = UUID.randomUUID().toString();
    long currentTime = System.currentTimeMillis();

    String filename = uuid + "_" + currentTime + ext;

    try {
      uploadFile.transferTo(new File(rootPath+folder+filename));
    } catch (Exception e) {
      e.printStackTrace();
    }
    //

    String url = folder+filename;

    ProductDto productDto = new ProductDto();
    productDto.setCategory_id(params.getCategory_id());
    productDto.setSeller_id(params.getSeller_id());
    productDto.setTitle(params.getTitle());
    productDto.setPrice(params.getPrice());
    productDto.setMain_page_url(url);
    productDto.setTotal_quantity(params.getTotal_quantity());

    adminService.saveProduct(productDto);

    return "redirect:/admin/dashboard";
  }

  // 상품 등록 
  @RequestMapping("productRegister")
  public String productRegisterPage(){

    return "admin/admin_productRegister";
  }
  // 상품 조회
  @RequestMapping("products")
  public String products(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    List<ProductDto> productList = adminService.sellerProducts(sellerDto.getSeller_id());

    model.addAttribute("productList", productList);

    return "admin/admin_product";
  }

  //주문 조회
  @RequestMapping("order")
  public String orders(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    List<Map<String, Object>> orderList = adminService.getAllOrders(sellerDto.getSeller_id());
    // System.out.println(orderList);
    
    model.addAttribute("orderList", orderList);

    return "admin/admin_order";
  }

  @RequestMapping("review")
  public String review(HttpSession httpSession, Model model){

    SellerDto sellerDto = (SellerDto) httpSession.getAttribute("sellerInfo");

    List<Map<String, Object>> reviewList = adminService.getAllReviews(sellerDto.getSeller_id());
    // System.out.println(reviewList);
    
    model.addAttribute("reviewList", reviewList);

    return "admin/admin_review";
  }

  @RequestMapping("replyProcess")
  public String replyProcess(
    @RequestParam("review_id") int review_id,
    @RequestParam("seller_reply") String seller_reply
  ){
    adminService.addReply(review_id, seller_reply);
    return "redirect:/admin/review";
  }
}
