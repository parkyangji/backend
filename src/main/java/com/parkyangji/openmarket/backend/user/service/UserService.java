package com.parkyangji.openmarket.backend.user.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.common.DebugUtil;
import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.BrandSummaryDto;
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerCartDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.KeywordDto;
import com.parkyangji.openmarket.backend.dto.OrderDetailDto;
import com.parkyangji.openmarket.backend.dto.ProductDetailReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductInventorySummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionSummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductRatingDto;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;
import com.parkyangji.openmarket.backend.dto.OrderSummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.ProductSummaryDto;
import com.parkyangji.openmarket.backend.user.mapper.UserSqlMapper;

import jakarta.servlet.http.HttpSession;

import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;

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
    // 고객의 기존 주소 개수 확인
    int count = userSqlMapper.getAddressCountByCustomerId(addressDto.getCustomer_id()).size() ;

    // 첫 번째 주소일 경우 기본 주소로 설정
    if (count == 0) {
      addressDto.set_default(true);
    } else {
      addressDto.set_default(false);
    }

    userSqlMapper.insertAddress(addressDto);
  }

  public List<AddressDto> getUserAddress(CustomerDto customerDto){
    return userSqlMapper.getAddressCountByCustomerId(customerDto.getCustomer_id());
  }

  public CustomerDto loginCheck(CustomerDto customerDto){
    return userSqlMapper.selectLoginCheck(customerDto); // 없으면 null
  }

  public String getCategoryName(int parent_category_id){
    return userSqlMapper.selectCategoryName(parent_category_id);
  }

  public List<KeywordDto> getAllKeywords(){
    return userSqlMapper.selectAllkeywords();
  }

  public List<Map<String, Object>> getSubTabCategorys(Integer parent_category_id) {
    return userSqlMapper.selectSubCategorys(parent_category_id);
  }

  public List<Integer> getSubCategoryIds(List<Map<String, Object>> subCategoryIdAndName) {
    // 1뎁스 기준 상품 출력
    List<Integer> categoryId = new ArrayList<>();
    for (Map<String, Object> category : subCategoryIdAndName) {
      categoryId.add((Integer) category.get("category_id"));
    }
    return categoryId;
  }

  public List<ProductSummaryDto> getCategoryProductList(List<Integer> categoryId){
    List<ProductSummaryDto> parentCategoryAllProducts = new ArrayList<>();

    for (Integer id : categoryId) {
      List<ProductDetailReturnDto> categoryProducts = userSqlMapper.selectProductListByCategoryId(id);

      for (ProductDetailReturnDto product : categoryProducts) {
        ProductSummaryDto productSummaryDto = new ProductSummaryDto();
        productSummaryDto.setProduct_id(product.getProduct_id());
        productSummaryDto.setStore_name(product.getStore_name());
        productSummaryDto.setCategory_id(product.getCategory_id());
        productSummaryDto.setTitle(product.getTitle());
        productSummaryDto.setImage_url(userSqlMapper.selectThumbnailImage(product.getProduct_id()));
        productSummaryDto.setDiscount_rate(product.getDiscount_rate());
        productSummaryDto.setRep_price(product.getRep_price());
        productSummaryDto.setRep_sale_price(product.getRep_sale_price());
        productSummaryDto.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
        productSummaryDto.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
        productSummaryDto.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));
        
        parentCategoryAllProducts.add(productSummaryDto);
      }
    }

    return parentCategoryAllProducts;
  }


  public ProductSummaryDto getProductDate(int product_id) {
    ProductDetailReturnDto productDetail = userSqlMapper.selectProductById(product_id);

    ProductSummaryDto product = new ProductSummaryDto();
    product.setProduct_id(productDetail.getProduct_id());
    product.setStore_name(productDetail.getStore_name());
    product.setCategory_id(productDetail.getCategory_id());
    product.setTitle(productDetail.getTitle());
    product.setDiscount_rate(productDetail.getDiscount_rate());
    product.setRep_price(productDetail.getRep_price());
    product.setRep_sale_price(productDetail.getRep_sale_price());
    product.setAvgRating(userSqlMapper.selectAvgRating(productDetail.getProduct_id()));
    product.setReviewCount(userSqlMapper.selectReviews(productDetail.getProduct_id()).size());
    product.setKeywords(userSqlMapper.selectProductKeywords(productDetail.getProduct_id()));

    Map<String, List<String>> transformedMap = new HashMap<>();
    for (Map<String,Object> image : userSqlMapper.selectProductAllImages(product_id)) {
      String typeName = (String) image.get("type_name");
      String imageUrl = (String) image.get("image_url");
      transformedMap.computeIfAbsent(typeName, k -> new ArrayList<>()).add(imageUrl);
    }
    product.setDetailsImages(transformedMap);

    return product;
  }


  public List<ProductSummaryDto> getBrandProducts(String store_name) {
    List<ProductSummaryDto> brandAllProducts = new ArrayList<>();

    List<ProductDetailReturnDto> selectBrandProducts = userSqlMapper.selectBrandProducts(store_name);
    for (ProductDetailReturnDto product : selectBrandProducts) {
      ProductSummaryDto productSummaryDto = new ProductSummaryDto();
      productSummaryDto.setProduct_id(product.getProduct_id());
      productSummaryDto.setStore_name(product.getStore_name());
      productSummaryDto.setCategory_id(product.getCategory_id());
      productSummaryDto.setTitle(product.getTitle());
      productSummaryDto.setImage_url(userSqlMapper.selectThumbnailImage(product.getProduct_id()));
      productSummaryDto.setDiscount_rate(product.getDiscount_rate());
      productSummaryDto.setRep_price(product.getRep_price());
      productSummaryDto.setRep_sale_price(product.getRep_sale_price());
      productSummaryDto.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
      productSummaryDto.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
      productSummaryDto.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));

      brandAllProducts.add(productSummaryDto);
    }
    return brandAllProducts;
  }

  public List<ProductSummaryDto> getBrandProductsByCategoryId(String store_name, int parent_category_id) {
    List<Integer> categoryIds = getSubCategoryIds(getSubTabCategorys(parent_category_id));

    List<ProductSummaryDto> brandProducts = new ArrayList<>();
    
    for (Integer id : categoryIds) {
      List<ProductDetailReturnDto> selectBrandProducts =  userSqlMapper.selectBrandProductByCategoryId(store_name, id);
      for (ProductDetailReturnDto product : selectBrandProducts) {
        ProductSummaryDto productSummaryDto = new ProductSummaryDto();
        productSummaryDto.setProduct_id(product.getProduct_id());
        productSummaryDto.setStore_name(product.getStore_name());
        productSummaryDto.setCategory_id(product.getCategory_id());
        productSummaryDto.setTitle(product.getTitle());
        productSummaryDto.setImage_url(userSqlMapper.selectThumbnailImage(product.getProduct_id()));
        productSummaryDto.setDiscount_rate(product.getDiscount_rate());
        productSummaryDto.setRep_price(product.getRep_price());
        productSummaryDto.setRep_sale_price(product.getRep_sale_price());
        productSummaryDto.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
        productSummaryDto.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
        productSummaryDto.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));
        
        brandProducts.add(productSummaryDto);
      }
    }

    return brandProducts;
  }

  public List<ProductSummaryDto> getKeywordFilter(String keyword, Map<String, Object> categorys){
    List<ProductSummaryDto> keywordProducts = new ArrayList<>();

    Integer parent_category_id = (Integer) categorys.get("parent_category_id");
    Integer sub_category_id = (Integer) categorys.get("sub_category_id");

    if (sub_category_id == null) { // 부모 카테고리 기준 검색
      List<Integer> categoryIds = getSubCategoryIds(getSubTabCategorys(parent_category_id)); 
      for (Integer id : categoryIds) {
        List<ProductDetailReturnDto> keywordsearchs = userSqlMapper.selectKeywordFromCategory(keyword, id);

        for (ProductDetailReturnDto product : keywordsearchs) {
          ProductSummaryDto productSummaryDto = new ProductSummaryDto();
          productSummaryDto.setProduct_id(product.getProduct_id());
          productSummaryDto.setStore_name(product.getStore_name());
          productSummaryDto.setCategory_id(product.getCategory_id());
          productSummaryDto.setTitle(product.getTitle());
          productSummaryDto.setImage_url(userSqlMapper.selectThumbnailImage(product.getProduct_id()));
          productSummaryDto.setDiscount_rate(product.getDiscount_rate());
          productSummaryDto.setRep_price(product.getRep_price());
          productSummaryDto.setRep_sale_price(product.getRep_sale_price());
          productSummaryDto.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
          productSummaryDto.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
          productSummaryDto.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));
          
          keywordProducts.add(productSummaryDto);
        }
      }
    } else {
      List<ProductDetailReturnDto> keywordsearchs = userSqlMapper.selectKeywordFromCategory(keyword, sub_category_id);

      for (ProductDetailReturnDto product : keywordsearchs) {
        ProductSummaryDto productSummaryDto = new ProductSummaryDto();
        productSummaryDto.setProduct_id(product.getProduct_id());
        productSummaryDto.setStore_name(product.getStore_name());
        productSummaryDto.setCategory_id(product.getCategory_id());
        productSummaryDto.setTitle(product.getTitle());
        productSummaryDto.setImage_url(userSqlMapper.selectThumbnailImage(product.getProduct_id()));
        productSummaryDto.setDiscount_rate(product.getDiscount_rate());
        productSummaryDto.setRep_price(product.getRep_price());
        productSummaryDto.setRep_sale_price(product.getRep_sale_price());
        productSummaryDto.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
        productSummaryDto.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
        productSummaryDto.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));
        
        keywordProducts.add(productSummaryDto);
      }
    }
    return keywordProducts;
  }

  public List<String> getAddressList(int customer_id){
    return userSqlMapper.selectAddressList(customer_id);    
  }

  public AddressDto getDefaultAddress(int customer_id){
    return userSqlMapper.selectDefalutAddress(customer_id);
  }

  public OrderDto createOrderAndDelivery(int customer_id, int address_id, String deliveryMessage){
    OrderDto orderDto = new OrderDto();
    orderDto.setCustomer_id(customer_id);
    orderDto.setAddress_id(address_id);
    orderDto.setDelivery_message(deliveryMessage);

    userSqlMapper.insertOrder(orderDto);

    return orderDto;
  }


  public void setOrderDetail(int customer_id, int order_id, List<CartItemDto> orderItems) {
    List<OrderDetailDto> orderDetailDtos = new ArrayList<>();

    List<Integer> combination_ids = new ArrayList<>();
    // List<Integer> productIds = new ArrayList<>();
    List<Integer> quantitys = new ArrayList<>();

    for (CartItemDto option : orderItems) {
      combination_ids.add(option.getCombination_id());
      quantitys.add(option.getQuantity());
    }

    List<CartItemReturnDto> cartitems = new ArrayList<>();
    int idx = 0;

    for (int i=0; i<combination_ids.size(); i++) {
      List<CartItemReturnDto> cartitem = userSqlMapper.selectTempCartItems(combination_ids.get(i));
      for (CartItemReturnDto item : cartitem) {
        item.setImage_url(userSqlMapper.selectThumbnailImage(item.getProduct_id()));
        item.setQuantity(quantitys.get(idx));
        
        cartitems.add(item);
      }
      idx++;
    }

    for (CartItemReturnDto cartitem : cartitems) {
      OrderDetailDto orderDetailDto = new OrderDetailDto();
      orderDetailDto.setOrder_id(order_id);
      orderDetailDto.setCombination_id(cartitem.getCombination_id());
      orderDetailDto.setQuantity(cartitem.getQuantity());
      if (cartitem.getDiscount_rate() == null) {
        orderDetailDto.setPrice(cartitem.getOrigin_price());
      } else {
        orderDetailDto.setPrice(cartitem.getSale_price());
      }
      orderDetailDtos.add(orderDetailDto);
    }

    userSqlMapper.insertOrderDetails(orderDetailDtos);
  }

  

  public List<OrderSummaryDto> getOrderSummaryList(int customerId) {
    List<OrderItemReturnDto> orderList = userSqlMapper.selectOrderList(customerId);
    return revertOrderSummary(orderList, false, true);
  }

  public List<OrderSummaryDto> getOrderReviewWrite(int customerId){
    List<OrderItemReturnDto> reviewWrite = userSqlMapper.selectWriteReviews(customerId);
    return revertOrderSummary(reviewWrite, true, false);
  }
  public List<OrderSummaryDto> getOrderReviewWritten(int customerId){
    List<OrderItemReturnDto> reviewWritten = userSqlMapper.selectWrittenReviews(customerId);
    return revertOrderSummary(reviewWritten, true, false);
  }


  public List<Map<String, Object>> getLikeList(int customer_id) {
    List<Map<String, Object>> LikeData = new ArrayList<>();

    List<ProductFavoriteDto> likeList = userSqlMapper.selectUserFavoriteList(customer_id);

    for (ProductFavoriteDto like : likeList) {
      Map<String, Object> product = new HashMap<>();
      product.put("like", like);
      product.put("image", userSqlMapper.selectThumbnailImage(like.getProduct_id()));

      LikeData.add(product);
    }

    return LikeData;
  }

  public void saveRating(int order_detail_id, int rating) {
    userSqlMapper.insertRating(order_detail_id, rating);
  }

  public void saveReviwContent(int order_detail_id, String review_content) {
    ProductRatingDto productRatingDto = userSqlMapper.selectRatingCheck(order_detail_id);
    if (productRatingDto != null) {
      userSqlMapper.insertReviwContent(order_detail_id, review_content);
    }
  }

  public Boolean toggleLike(ProductFavoriteDto productFavoriteDto) {

    ProductFavoriteDto existingLike = userSqlMapper.selectLike(productFavoriteDto);

    if (existingLike == null) {
     userSqlMapper.insertLike(productFavoriteDto);
      return true;
    } else {
     userSqlMapper.deleteLike(productFavoriteDto);
      return false;
    }
  }  

  public ProductFavoriteDto findLike(ProductFavoriteDto productFavoriteDto){
    return userSqlMapper.selectLike(productFavoriteDto);
  }

  public List<ProductFavoriteDto> isLikeCheck(List<ProductSummaryDto> productList, int customer_id){
    List<ProductFavoriteDto> list = new ArrayList<>();
    
    for (ProductSummaryDto productSummaryDto : productList) {
      ProductFavoriteDto productFavoriteDto = new ProductFavoriteDto();
      productFavoriteDto.setCustomer_id(customer_id);
      productFavoriteDto.setProduct_id(productSummaryDto.getProduct_id());
      
      list.add(userSqlMapper.selectLike(productFavoriteDto));
    }
    return list;
  }

  public List<ProductOptionSummaryDto> tempoptionChoice(int product_id){
    List<ProductOptionReturnDto> optionList = userSqlMapper.selectProductOptionAndInventory(product_id);
     
    // 옵션 조합별로
    Map<Integer, List<ProductOptionReturnDto>> groupByCombinationId = 
    optionList.stream().collect(Collectors.groupingBy(ProductOptionReturnDto::getCombination_id));

    // 공통 가격들
    List<ProductOptionSummaryDto> optionSummaryList =  groupByCombinationId.entrySet().stream()
    .map(entry -> {
      int combinationId = entry.getKey(); 
      List<ProductOptionReturnDto> option = entry.getValue();
      ProductOptionReturnDto rep = option.get(0);

      ProductOptionSummaryDto productOptionSummaryDto = new ProductOptionSummaryDto();
      productOptionSummaryDto.setCombination_id(combinationId);

      ProductInventorySummaryDto inventorySummary = new ProductInventorySummaryDto();
      // 옵션 이름, 값 
      List<Map<String, Object>> optionDetails = option.stream().map(item -> {
          Map<String, Object> optionMap = new HashMap<>();
          optionMap.put(item.getOptionname(), item.getOptionvalue());
          return optionMap;
      }).collect(Collectors.toList());
      inventorySummary.setOption(optionDetails);
      inventorySummary.setQuantity(rep.getQuantity());
      inventorySummary.setOrigin_price(rep.getOrigin_price());
      inventorySummary.setDiscount_rate(rep.getDiscount_rate());
      inventorySummary.setSale_price(rep.getSale_price());

      productOptionSummaryDto.setOptionDetails(inventorySummary);

      return productOptionSummaryDto;
    }).collect(Collectors.toList());
    
    return optionSummaryList;
  }

  public void setCartItem(CustomerDto customerDto, List<CartItemDto> optiondata) {

    if (userSqlMapper.selectCustomerCart(customerDto.getCustomer_id()) == null) {
      // 고객 장바구니가 없으면 장바구니 추가
      CustomerCartDto customerCartDto = new CustomerCartDto();
      customerCartDto.setCustomer_id(customerDto.getCustomer_id());
      userSqlMapper.insertCustomerCart(customerCartDto);
    }

    CustomerCartDto customerCart = userSqlMapper.selectCustomerCart(customerDto.getCustomer_id());

    // 아이템 추가 
    List<CartItemDto> items = new ArrayList<>();
    for (CartItemDto option : optiondata) {
      CartItemDto item = new CartItemDto();
      item.setCart_id(customerCart.getCart_id());
      item.setCombination_id(option.getCombination_id());
      item.setQuantity(option.getQuantity());

      items.add(item);
    }

    for (CartItemDto cartItem : items) {
      Integer existingQuantity = userSqlMapper.checkExistingCartItem(cartItem.getCart_id(), cartItem.getCombination_id());
      
      // 일단 장바구니 페이지에서는 수량 변경 안됌!!! => js시 수정되게
      if (existingQuantity != null) {
          // 기존에 항목이 있는 경우 업데이트
          userSqlMapper.updateCartItemQuantity(cartItem.getCart_id(), cartItem.getCombination_id(), cartItem.getQuantity());
      } else {
          // 기존에 항목이 없는 경우 삽입
          userSqlMapper.insertCartItem(cartItem);
      }
    }
  }

  public List<BrandSummaryDto> getCustomerCartItem(int customer_id) {
    // 고객의 장바구니 항목 조회
    List<CartItemReturnDto> cartItems = userSqlMapper.selectCustomerCartItems(customer_id);
    return revertBrandSummary(cartItems);
  }

  public List<BrandSummaryDto> getRevertCartData(List<CartItemDto> selectedOptions){
    System.out.println(selectedOptions);
    List<Integer> combination_ids = new ArrayList<>();
    // List<Integer> productIds = new ArrayList<>();
    List<Integer> quantitys = new ArrayList<>();

    
    for (CartItemDto option : selectedOptions) {
      combination_ids.add(option.getCombination_id());
      quantitys.add(option.getQuantity());
    }

    List<CartItemReturnDto> cartitems = new ArrayList<>();
    int idx = 0;

    for (int i=0; i<combination_ids.size(); i++) {
      List<CartItemReturnDto> cartitem = userSqlMapper.selectTempCartItems(combination_ids.get(i));
      for (CartItemReturnDto item : cartitem) {
        item.setImage_url(userSqlMapper.selectThumbnailImage(item.getProduct_id()));
        item.setQuantity(quantitys.get(idx));
        
        cartitems.add(item);
      }
      idx++;
    }
    return revertBrandSummary(cartitems);
  }



  public void refreshCustomerCart(OrderDto order) {
    // order가 있는 detail 에 combination_id 수량계산!!
    List<CartItemDto> ex_quantity = userSqlMapper.selectExQuantityByOrderId(order.getOrder_id());
    for (CartItemDto ex : ex_quantity) {
      if (ex.getQuantity() == 0) {
        // 삭제
        userSqlMapper.deleteCart(ex.getCart_id());
        userSqlMapper.deleteCartItem(ex);
      } else {
        // 수량 업데이트
        userSqlMapper.updateCartItemExQuantity(ex);
      }
    }
  }

  public List<Map<String,Object>> getProductRatingChartData(int product_id) {
    List<Map<String,Object>> result = userSqlMapper.selectProductIdRatingChart(product_id);
    return result;
  }

  public List<ProductSummaryDto> getProductTop5(int customer_id, int product_id) {
    List<Integer> productIds = userSqlMapper.selectTop5ProductsBoughtByOtherUsers(product_id, customer_id);

    List<ProductSummaryDto> result = new ArrayList<>();
    for (Integer productId : productIds) {
      // ProductSummaryDto productData = getProductDate(productId);
      ProductDetailReturnDto product = userSqlMapper.selectProductById(productId);

      ProductSummaryDto productSummaryDto = new ProductSummaryDto();
      productSummaryDto.setProduct_id(product.getProduct_id());
      productSummaryDto.setStore_name(product.getStore_name());
      productSummaryDto.setCategory_id(product.getCategory_id());
      productSummaryDto.setTitle(product.getTitle());
      productSummaryDto.setImage_url(userSqlMapper.selectThumbnailImage(product.getProduct_id()));
      productSummaryDto.setDiscount_rate(product.getDiscount_rate());
      productSummaryDto.setRep_price(product.getRep_price());
      productSummaryDto.setRep_sale_price(product.getRep_sale_price());
      productSummaryDto.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
      productSummaryDto.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
      productSummaryDto.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));

      result.add(productSummaryDto);
    }
    return result;
  }

  /**
   * 새로운 옵션 데이터를 기존 데이터에 병합.
   * 중복 데이터는 수량을 합치고, 중복되지 않은 데이터는 추가.
   */
  public void mergeOptions(List<CartItemDto> existingOptions, List<CartItemDto> newOptions) {
      for (CartItemDto newOption : newOptions) {
        CartItemDto matchingOption = findMatchingOption(existingOptions, newOption);

          if (matchingOption != null) {
              // 중복 데이터: 수량 합치기
              Integer existingQuantity = matchingOption.getQuantity();
              Integer newQuantity = newOption.getQuantity();

              matchingOption.setQuantity(existingQuantity + newQuantity);
          } else {
              // 중복되지 않은 새로운 데이터 추가
              CartItemDto newEntry = createNewEntry(newOption);
              existingOptions.add(newEntry);
          }
      }
  }

  /**
   * 기존 옵션 데이터에서 동일한 productId와 combination_id를 가진 항목을 찾음.
   */
  public CartItemDto findMatchingOption(List<CartItemDto> existingOptions,  CartItemDto newOption) {
      Integer newCombinationId = newOption.getCombination_id();

      for (CartItemDto existingOption : existingOptions) {
          Integer existingCombinationId = existingOption.getCombination_id();

          if (newCombinationId.equals(existingCombinationId)) {
              return existingOption;
          }
      }
      return null;
  }

  /**
   * 새로운 옵션 데이터를 생성.
   */
  public CartItemDto createNewEntry( CartItemDto newOption) {
      CartItemDto newEntry = new CartItemDto();
      // newEntry.put("product_id", newOption.get("product_id"));
      newEntry.setCombination_id(newOption.getCombination_id());
      newEntry.setQuantity(newOption.getQuantity());
      return newEntry;
  }

  public void removeOptions(List<CartItemDto> existingOptions, int combination_id){
    for (int i = 0; i < existingOptions.size(); i++) {
      CartItemDto option = existingOptions.get(i);
      if (option.getCombination_id().equals(combination_id)) {
          existingOptions.remove(i);
          break;
      }
    }
  }

  public void removeCustomerOption(CustomerDto customerDto, int combination_id) {
    CustomerCartDto customerCart = userSqlMapper.selectCustomerCart(customerDto.getCustomer_id());

    CartItemDto item = new CartItemDto();
    item.setCart_id(customerCart.getCart_id());
    item.setCombination_id(combination_id);
    
    userSqlMapper.deleteCartItem(item);
  }

  public List<String> getProductDetailImages(int product_id) {
    List<String> DetailImageUrls = new ArrayList<>();
    for (Map<String,Object> image : userSqlMapper.selectProductAllImages(product_id)) {
      String typeName = (String) image.get("type_name");
      String imageUrl = (String) image.get("image_url");

      if (typeName.equals("detail")) {
        DetailImageUrls.add(imageUrl);
      }
    }
    return DetailImageUrls;
  }

  public List<Map<String, Object>> getProductReviews(int product_id) {
    List<Map<String, Object>> reviewList = new ArrayList<>();

    List<ProductReviewDto> reviews = userSqlMapper.selectReviewByProductId(product_id);
    List<ProductRatingDto> ratings = userSqlMapper.selectRatingByProductId(product_id);

    // 주문이 있는 경우에만 orderList에 개별 주문 항목 추가
    for (ProductReviewDto review : reviews) {
        Map<String, Object> reveiwData = new HashMap<>();
        reveiwData.put("reviewer", userSqlMapper.selectReviewerByOrderDetailId(review.getOrder_detail_id()));
        reveiwData.put("review", review);        // 개별 주문 정보

        int targetIndex = IntStream.range(0, ratings.size())
        .filter(i -> ratings.get(i).getOrder_detail_id() == review.getOrder_detail_id())
        .findFirst()
        .orElse(-1);

        reveiwData.put("rating", ratings.get(targetIndex));
        reveiwData.put("reply", userSqlMapper.selectReviewReplyByOrderReviewId(review.getOrder_review_id())); 
        reviewList.add(reveiwData);
    }
    return reviewList;
  }

  public void UpdateTempCartItem(List<CartItemDto> existingOptions, CartItemDto data) {
    for (CartItemDto exist : existingOptions) {
      if (exist.getCombination_id().equals(data.getCombination_id())) {
        exist.setQuantity(data.getQuantity());
      }
    }
  }

  public void UpdateCustomerCartItem(CustomerDto customerDto, CartItemDto data) {
    CustomerCartDto customerCart = userSqlMapper.selectCustomerCart(customerDto.getCustomer_id());

    CartItemDto item = new CartItemDto();
    item.setCart_id(customerCart.getCart_id()); 
    item.setCombination_id(data.getCombination_id());
    item.setQuantity(data.getQuantity());

    userSqlMapper.updateCartItemExQuantity(item);
  }

  public OrderSummaryDto findOrderDetailByOrderId(CustomerDto customerDto, int order_id) {
    List<OrderSummaryDto> getOrders = getOrderSummaryList(customerDto.getCustomer_id());

    OrderSummaryDto orderSummaryDto = new OrderSummaryDto();

    for (OrderSummaryDto order : getOrders) {
      if (order.getOrder_id() == order_id) orderSummaryDto = order;
    }
    // 이름, 배송지, 배송메세지, 연락처. 
    orderSummaryDto.setAddressDto(userSqlMapper.selectOrderAddress(order_id));

    return orderSummaryDto;
  }


  public void addAddress(AddressDto addressDto, CustomerDto customerDto, String address){
    addressDto.setCustomer_id(customerDto.getCustomer_id());
    addressDto.setAddress(address);

    if (addressDto.is_default()) {
      userSqlMapper.updateAddressIsDefaultFalse(customerDto.getCustomer_id());
    }
    userSqlMapper.insertAddress(addressDto);
  }






  /* private */

  private List<OrderSummaryDto> revertOrderSummary(List<OrderItemReturnDto> orderList, boolean includeProducts, boolean includeBrands) {
    Map<Integer, List<OrderItemReturnDto>> groupingOrder = orderList.stream()
        .collect(Collectors.groupingBy(
            OrderItemReturnDto::getOrder_id,
            LinkedHashMap::new,
            Collectors.toList()
        ));

    List<OrderSummaryDto> dataa = groupingOrder.entrySet().stream().map((order) -> {
        int order_id = order.getKey();
        List<OrderItemReturnDto> orders = order.getValue();
        OrderItemReturnDto representativeItem = orders.get(0);

        // OrderSummaryDto 생성
        OrderSummaryDto orderSummaryDto = new OrderSummaryDto();
        orderSummaryDto.setOrder_id(order_id);
        orderSummaryDto.setDelivery_message(representativeItem.getDelivery_message());
        orderSummaryDto.setOrder_date(representativeItem.getOrder_date());

        if (includeBrands) {
            // 브랜드별 그룹화
            Map<String, Map<Integer, List<OrderItemReturnDto>>> groupedItems = orders.stream()
                .collect(Collectors.groupingBy(OrderItemReturnDto::getStore_name,
                    Collectors.groupingBy(OrderItemReturnDto::getProduct_id)));

            List<BrandSummaryDto> brandSummaryDtos = groupedItems.entrySet().stream()
                .map((entry) -> {
                    String storeName = entry.getKey();
                    Map<Integer, List<OrderItemReturnDto>> productMap = entry.getValue();

                    BrandSummaryDto brandSummary = new BrandSummaryDto();
                    brandSummary.setStore_name(storeName);

                    List<ProductSummaryDto> productSummaryList = productMap.entrySet().stream()
                        .map(productEntry -> {
                            int productId = productEntry.getKey();
                            List<OrderItemReturnDto> productItems = productEntry.getValue();
                            OrderItemReturnDto productRepresentative = productItems.get(0);

                            ProductSummaryDto productSummary = new ProductSummaryDto();
                            productSummary.setProduct_id(productId);
                            productSummary.setStore_name(productRepresentative.getStore_name());
                            productSummary.setTitle(productRepresentative.getTitle());
                            productSummary.setImage_url(userSqlMapper.selectThumbnailImage(productId));

                            // 조합별 옵션 정보 추가
                            Map<Integer, List<OrderItemReturnDto>> combinationMap = productItems.stream()
                                .collect(Collectors.groupingBy(OrderItemReturnDto::getCombination_id));

                            List<ProductInventorySummaryDto> inventorySummaryList = combinationMap.entrySet().stream()
                            .map(combinationEntry -> {
                                List<OrderItemReturnDto> combinationItems = combinationEntry.getValue();
                                OrderItemReturnDto combinationRepresentative = combinationItems.get(0);

                                ProductInventorySummaryDto inventorySummary = new ProductInventorySummaryDto();
                                inventorySummary.setCombination_id(combinationEntry.getKey());
                                inventorySummary.setOrder_detail_id(combinationRepresentative.getOrder_detail_id());
                                inventorySummary.setQuantity(combinationRepresentative.getQuantity());
                                inventorySummary.setStatus(combinationRepresentative.getStatus());
                                inventorySummary.setBuy_price(combinationRepresentative.getBuy_price());
                                inventorySummary.setRating(combinationRepresentative.getRating());
                                inventorySummary.setRating_created(combinationRepresentative.getRating_created());
                                inventorySummary.setReview_content(combinationRepresentative.getReview_content());
                                inventorySummary.setReview_created(combinationRepresentative.getReview_created());
                                inventorySummary.setSeller_reply(combinationRepresentative.getSeller_reply());

                              List<Map<String, Object>> optionDetails = combinationItems.stream()
                                      .collect(Collectors.toMap(
                                              OrderItemReturnDto::getOptionname,
                                              OrderItemReturnDto::getOptionvalue,
                                              (existing, replacement) -> existing // 중복 옵션 발생 시 기존 값 유지
                                      ))
                                      .entrySet()
                                      .stream()
                                      .map(item -> {
                                        Map<String, Object> optionMap = new HashMap<>();
                                        optionMap.put(item.getKey(), item.getValue());
                                        return optionMap;
                                      })
                                      .collect(Collectors.toList());

                                inventorySummary.setOption(optionDetails);
                                return inventorySummary;
                            }).collect(Collectors.toList());

                            productSummary.setOptions(inventorySummaryList);
                            return productSummary;
                        }).collect(Collectors.toList());

                    brandSummary.setProducts(productSummaryList);
                    return brandSummary;
                }).collect(Collectors.toList());

            orderSummaryDto.setBrands(brandSummaryDtos);
        }

        if (includeProducts) {
            // 상품별 그룹화
            List<ProductSummaryDto> productSummaries = orders.stream()
                .collect(Collectors.groupingBy(OrderItemReturnDto::getProduct_id))
                .entrySet().stream()
                .map(entry -> {
                    int productId = entry.getKey();
                    List<OrderItemReturnDto> productItems = entry.getValue();
                    OrderItemReturnDto representative = productItems.get(0);

                    ProductSummaryDto productSummary = new ProductSummaryDto();
                    productSummary.setProduct_id(productId);
                    productSummary.setStore_name(representative.getStore_name());
                    productSummary.setTitle(representative.getTitle());
                    productSummary.setImage_url(userSqlMapper.selectThumbnailImage(productId));

                    // 조합별 옵션 정보 추가
                    Map<Integer, List<OrderItemReturnDto>> combinationMap = productItems.stream()
                        .collect(Collectors.groupingBy(OrderItemReturnDto::getCombination_id));

                    List<ProductInventorySummaryDto> inventorySummaryList = combinationMap.entrySet().stream()
                        .map(combinationEntry -> {
                            List<OrderItemReturnDto> combinationItems = combinationEntry.getValue();
                            OrderItemReturnDto combinationRepresentative = combinationItems.get(0);

                            ProductInventorySummaryDto inventorySummary = new ProductInventorySummaryDto();
                            inventorySummary.setCombination_id(combinationEntry.getKey());
                            inventorySummary.setOrder_detail_id(combinationRepresentative.getOrder_detail_id());
                            inventorySummary.setQuantity(combinationRepresentative.getQuantity());
                            inventorySummary.setStatus(combinationRepresentative.getStatus());
                            inventorySummary.setBuy_price(combinationRepresentative.getBuy_price());
                            inventorySummary.setRating(combinationRepresentative.getRating());
                            inventorySummary.setRating_created(combinationRepresentative.getRating_created());
                            inventorySummary.setReview_content(combinationRepresentative.getReview_content());
                            inventorySummary.setReview_created(combinationRepresentative.getReview_created());
                            inventorySummary.setSeller_reply(combinationRepresentative.getSeller_reply());

                          List<Map<String, Object>> optionDetails = combinationItems.stream()
                                  .collect(Collectors.toMap(
                                          OrderItemReturnDto::getOptionname,
                                          OrderItemReturnDto::getOptionvalue,
                                          (existing, replacement) -> existing // 중복 옵션 발생 시 기존 값 유지
                                  ))
                                  .entrySet()
                                  .stream()
                                  .map(item -> {
                                    Map<String, Object> optionMap = new HashMap<>();
                                    optionMap.put(item.getKey(), item.getValue());
                                    return optionMap;
                                  })
                                  .collect(Collectors.toList());

                            inventorySummary.setOption(optionDetails);
                            return inventorySummary;
                        }).collect(Collectors.toList());

                    productSummary.setOptions(inventorySummaryList);
                    return productSummary;
                }).collect(Collectors.toList());

            orderSummaryDto.setProducts(productSummaries);
        }

        return orderSummaryDto;
    }).collect(Collectors.toList());

    return dataa; 
  }


  private List<BrandSummaryDto> revertBrandSummary(List<CartItemReturnDto> cartItems){
    // store_name으로 그룹핑하여 BrandSummaryDto 생성
    Map<String, Map<Integer, List<CartItemReturnDto>>> groupedItems = cartItems.stream()
        .collect(Collectors.groupingBy(CartItemReturnDto::getStore_name,
            Collectors.groupingBy(CartItemReturnDto::getProduct_id)));
    
    // 각 store_name별로 BrandSummaryDto 생성
    List<BrandSummaryDto> brandSummaryList = groupedItems.entrySet().stream()
        .map(brandEntry -> {
            String storeName = brandEntry.getKey();
            Map<Integer, List<CartItemReturnDto>> productMap = brandEntry.getValue();

            BrandSummaryDto brandSummary = new BrandSummaryDto();
            brandSummary.setStore_name(storeName);

            // 각 product_id별로 ProductSummaryDto 생성
            List<ProductSummaryDto> productSummaryList = productMap.entrySet().stream()
                .map(productEntry -> {
                    int productId = productEntry.getKey();
                    List<CartItemReturnDto> productItems = productEntry.getValue();
                    CartItemReturnDto representativeItem = productItems.get(0);

                    // 제품 요약 정보 생성
                    ProductSummaryDto productSummary = new ProductSummaryDto();
                    productSummary.setProduct_id(productId);
                    productSummary.setStore_name(representativeItem.getStore_name());
                    productSummary.setTitle(representativeItem.getTitle());
                    productSummary.setImage_url(userSqlMapper.selectThumbnailImage(productId));
                    productSummary.setRep_price(representativeItem.getOrigin_price());
                    productSummary.setDiscount_rate(representativeItem.getDiscount_rate());
                    productSummary.setRep_sale_price(representativeItem.getSale_price());

                    // combination_id로 옵션 그룹핑하여 옵션 정보 생성
                    Map<Integer, List<CartItemReturnDto>> combinationMap = productItems.stream()
                        .collect(Collectors.groupingBy(CartItemReturnDto::getCombination_id));

                    List<ProductInventorySummaryDto> inventorySummaryList = combinationMap.entrySet().stream()
                        .map(combinationEntry -> {
                            List<CartItemReturnDto> combinationItems = combinationEntry.getValue();
                            CartItemReturnDto combinationRepresentative = combinationItems.get(0);

                            ProductInventorySummaryDto inventorySummary = new ProductInventorySummaryDto();
                            inventorySummary.setCombination_id(combinationEntry.getKey());
                            inventorySummary.setQuantity(combinationRepresentative.getQuantity());
                            inventorySummary.setStatus("In Stock"); // 임의로 상태를 설정 (필요에 따라 수정 가능)
                            inventorySummary.setOrigin_price(combinationRepresentative.getOrigin_price());
                            inventorySummary.setDiscount_rate(combinationRepresentative.getDiscount_rate());
                            inventorySummary.setSale_price(combinationRepresentative.getSale_price());

                            // 옵션의 이름과 값을 리스트 형태로 저장
                            List<Map<String, Object>> optionDetails = combinationItems.stream()
                                .map(item -> {
                                    Map<String, Object> optionMap = new HashMap<>();
                                    optionMap.put(item.getOptionname(), item.getOptionvalue());
                                    return optionMap;
                                }).collect(Collectors.toList());

                            inventorySummary.setOption(optionDetails);
                            return inventorySummary;
                        }).collect(Collectors.toList());

                    productSummary.setOptions(inventorySummaryList);
                    return productSummary;
                }).collect(Collectors.toList());

            brandSummary.setProducts(productSummaryList);
            return brandSummary;
        }).collect(Collectors.toList());

    return brandSummaryList;
  }


}
