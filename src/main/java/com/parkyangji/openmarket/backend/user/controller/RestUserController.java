package com.parkyangji.openmarket.backend.user.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.parkyangji.openmarket.backend.common.DebugUtil;
import com.parkyangji.openmarket.backend.common.ShuffleUtil;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductSummaryDto;
import com.parkyangji.openmarket.backend.dto.RestResponseDto;
import com.parkyangji.openmarket.backend.user.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("api")
public class RestUserController {

  @Autowired
  private UserService userService;

  @RequestMapping("favorite/isLike")
  public RestResponseDto isUserLike(HttpSession httpSession, @RequestParam("id") int id){
    RestResponseDto responseDto = new RestResponseDto();

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
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
    
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
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

    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
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
    
    CustomerDto customerDto = (CustomerDto) httpSession.getAttribute("sessionInfo");
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
}
