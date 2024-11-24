package com.parkyangji.openmarket.backend.user.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.dto.AddressDto;
import com.parkyangji.openmarket.backend.dto.BrandSummaryDto;
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerCartDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.DeliveryInfoDto;
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
    int count = userSqlMapper.getAddressCountByCustomerId(addressDto.getCustomer_id());

    // 첫 번째 주소일 경우 기본 주소로 설정
    if (count == 0) {
      addressDto.set_default(true);
    } else {
      addressDto.set_default(false);
    }

    userSqlMapper.insertAddress(addressDto);
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

    userSqlMapper.insertOrder(orderDto);

    DeliveryInfoDto deliveryInfoDto = new DeliveryInfoDto();
    deliveryInfoDto.setOrder_id(orderDto.getOrder_id());
    deliveryInfoDto.setAddress_id(address_id);
    deliveryInfoDto.setDelivery_message(deliveryMessage);
    userSqlMapper.insertDeliveryInfo(deliveryInfoDto);

    return orderDto;
  }


  public void setOrderDetail(int customer_id, int order_id) {
    List<OrderDetailDto> orderDetailDtos = new ArrayList<>();

    List<CartItemReturnDto> cartitems = userSqlMapper.selectCustomerOrderDetails(customer_id);
    // System.out.println(cartitems);
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

    // 주문 ID로 그룹핑
    Map<Integer, List<OrderItemReturnDto>> groupByOrderId = orderList.stream()
        .collect(Collectors.groupingBy(OrderItemReturnDto::getOrder_id));

    // 각 주문별로 제품 및 옵션을 나누어 OrderSummaryDto에 매핑
    List<OrderSummaryDto> orderSummaryList = groupByOrderId.entrySet().stream()
    .map(orderEntry -> {
        int orderId = orderEntry.getKey();
        List<OrderItemReturnDto> items = orderEntry.getValue();

        // 주문 요약 정보 생성
        OrderSummaryDto orderSummary = new OrderSummaryDto();
        orderSummary.setOrder_id(orderId);
        orderSummary.setOrder_date(items.get(0).getOrder_date());
        // orderSummary.setStatus(items.get(0).getStatus());

        // 제품 ID로 그룹핑
        Map<Integer, List<OrderItemReturnDto>> groupByProductId = items.stream()
            .collect(Collectors.groupingBy(OrderItemReturnDto::getProduct_id));

        List<ProductSummaryDto> productSummaryList = groupByProductId.entrySet().stream()
            .map(productEntry -> {
                List<OrderItemReturnDto> productItems = productEntry.getValue();
                OrderItemReturnDto representativeItem = productItems.get(0); // 대표 아이템

                // 제품 요약 정보 생성
                ProductSummaryDto productSummary = new ProductSummaryDto();
                productSummary.setProduct_id(representativeItem.getProduct_id());
                productSummary.setStore_name(representativeItem.getStore_name());
                productSummary.setTitle(representativeItem.getTitle());
                productSummary.setImage_url(userSqlMapper.selectThumbnailImage(representativeItem.getProduct_id()));
               // productSummary.setOrigin_price(representativeItem.getOrigin_price());
                productSummary.setDiscount_rate(representativeItem.getDiscount_rate());
               // productSummary.setSale_price(representativeItem.getSale_price());

                // combinationId로 옵션 그룹핑
                Map<Integer, List<OrderItemReturnDto>> groupByCombinationId = productItems.stream()
                    .collect(Collectors.groupingBy(OrderItemReturnDto::getCombination_id));

                  // 옵션 정보 생성
                List<ProductOptionSummaryDto> optionList = groupByCombinationId.entrySet().stream()
                    .map(combinationEntry -> {
                        List<OrderItemReturnDto> combinationItems = combinationEntry.getValue();
                        OrderItemReturnDto representativeCombination = combinationItems.get(0);

                        ProductOptionSummaryDto option = new ProductOptionSummaryDto();
                        option.setCombination_id(representativeCombination.getCombination_id());
                        //option.setOrder_detail_id(representativeCombination.getOrder_detail_id());

                        ProductInventorySummaryDto inventorySummary = new ProductInventorySummaryDto();
                        inventorySummary.setQuantity(representativeCombination.getQuantity());
                        inventorySummary.setStatus(representativeCombination.getStatus());
                        inventorySummary.setOrigin_price(representativeCombination.getOrigin_price());
                        inventorySummary.setDiscount_rate(representativeCombination.getDiscount_rate());
                        inventorySummary.setSale_price(representativeCombination.getSale_price());
                        inventorySummary.setRating(representativeCombination.getRating());
                        inventorySummary.setReview_content(representativeCombination.getReview_content());
                        inventorySummary.setSeller_reply(representativeCombination.getSeller_reply());

                        // 옵션의 이름과 값을 리스트 형태로 저장
                        List<Map<String, Object>> optionDetails = combinationItems.stream()
                            .map(item -> {
                                Map<String, Object> optionMap = new HashMap<>();
                                optionMap.put(item.getOptionname(), item.getOptionvalue());
                                return optionMap;
                            }).collect(Collectors.toList());

                        inventorySummary.setOption(optionDetails);

                       // option.setOptionDetails(List.of(inventorySummary));
                       option.setOptionDetails(inventorySummary);

                        return option;
                    }).collect(Collectors.toList());

               // productSummary.setOptions(optionList);
                return productSummary;
            }).collect(Collectors.toList());

        orderSummary.setProducts(productSummaryList);
        return orderSummary;
    }).collect(Collectors.toList());

    return orderSummaryList;
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

  public void setCartItem(CustomerDto customerDto, int productId, List<Integer> combination_id, List<Integer> quantity) {

    if (userSqlMapper.selectCustomerCart(customerDto.getCustomer_id()) == null) {
      // 고객 장바구니가 없으면 장바구니 추가
      CustomerCartDto customerCartDto = new CustomerCartDto();
      customerCartDto.setCustomer_id(customerDto.getCustomer_id());
      userSqlMapper.insertCustomerCart(customerCartDto);
    }

    CustomerCartDto customerCart = userSqlMapper.selectCustomerCart(customerDto.getCustomer_id());

    // 아이템 추가 
    List<CartItemDto> items = new ArrayList<>();
    for (int i=0; i<combination_id.size(); i++) {
      CartItemDto item = new CartItemDto();
      item.setCart_id(customerCart.getCart_id());
      item.setCombination_id(combination_id.get(i));
      item.setQuantity(quantity.get(i));
      items.add(item);
    }
   // userSqlMapper.insertCartItem(items);

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

    // 아이템 + 수량 + 가격 정보!! => cart 페이지 띄울때 필요함
  }

public List<BrandSummaryDto> getCustomerCartItem(int customer_id) {
    // 고객의 장바구니 항목 조회
    List<CartItemReturnDto> cartItems = userSqlMapper.selectCustomerCartItems(customer_id);

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
                    // productSummary.setRep_price(representativeItem.getOrigin_price());
                    productSummary.setDiscount_rate(representativeItem.getDiscount_rate());
                    // productSummary.setRep_sale_price(representativeItem.getSale_price());

                    // combination_id로 옵션 그룹핑하여 옵션 정보 생성
                    Map<Integer, List<CartItemReturnDto>> combinationMap = productItems.stream()
                        .collect(Collectors.groupingBy(CartItemReturnDto::getCombination_id));

                    List<ProductInventorySummaryDto> inventorySummaryList = combinationMap.entrySet().stream()
                        .map(combinationEntry -> {
                            List<CartItemReturnDto> combinationItems = combinationEntry.getValue();
                            CartItemReturnDto combinationRepresentative = combinationItems.get(0);

                            ProductInventorySummaryDto inventorySummary = new ProductInventorySummaryDto();
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

  public Map<Integer, Map<Integer, List<CartItemReturnDto>>> getTempCartItem(int productId, List<Integer> combination_id, List<Integer> quantity) {
    // Step 1: Fetch cart items from the database and set image URLs
    List<CartItemReturnDto> cartitems = userSqlMapper.selectTempCartItems(productId, combination_id);
    for (CartItemReturnDto item : cartitems) {
        item.setImage_url(userSqlMapper.selectThumbnailImage(item.getProduct_id()));
    }

    // Step 2: Group cart items by product_id first
    Map<Integer, List<CartItemReturnDto>> groupByProductId = cartitems.stream()
            .collect(Collectors.groupingBy(CartItemReturnDto::getProduct_id));

    // Step 3: Within each product group, group items by combination_id
    Map<Integer, Map<Integer, List<CartItemReturnDto>>> groupByProductAndCombination = groupByProductId.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey, // product_id as the key
                    entry -> {
                        List<CartItemReturnDto> items = entry.getValue();
                        
                        // Update quantities for each combination_id
                        for (int i = 0; i < combination_id.size(); i++) {
                            int combinationKey = combination_id.get(i);
                            int newQuantity = quantity.get(i);

                            // Update quantity for all items matching the combination_id
                            items.stream()
                                    .filter(item -> item.getCombination_id() == combinationKey)
                                    .forEach(item -> item.setQuantity(newQuantity));
                        }

                        // Group by combination_id
                        return items.stream().collect(Collectors.groupingBy(CartItemReturnDto::getCombination_id,
                        LinkedHashMap::new,Collectors.toList()));
                    }
            ));

    return groupByProductAndCombination;
  }


  public Map<Integer, Map<Integer, List<CartItemReturnDto>>> updateTempCartItem(
        Map<Integer, Map<Integer, List<CartItemReturnDto>>> existingCartItems,
        Map<Integer, Map<Integer, List<CartItemReturnDto>>> newItems) {

    if (existingCartItems == null) {
        existingCartItems = new HashMap<>();
    }

    for (Map.Entry<Integer, Map<Integer, List<CartItemReturnDto>>> productEntry : newItems.entrySet()) {
        Integer productId = productEntry.getKey();
        Map<Integer, List<CartItemReturnDto>> newCombinationMap = productEntry.getValue();

        // 기존 제품 그룹이 있는지 확인하고 병합
        existingCartItems.merge(productId, newCombinationMap, (existingCombinationMap, newCombinationMapToMerge) -> {
            // combination_id별로 병합
            for (Map.Entry<Integer, List<CartItemReturnDto>> combinationEntry : newCombinationMapToMerge.entrySet()) {
                Integer combinationId = combinationEntry.getKey();
                List<CartItemReturnDto> newItemList = combinationEntry.getValue();

                // 기존 combination_id 그룹이 있는지 확인하고 병합
                existingCombinationMap.merge(combinationId, newItemList, (existingList, newList) -> {
                    existingList.addAll(newList);
                    return existingList;
                });
            }
            return existingCombinationMap;
        });
    }

    return existingCartItems;
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

}
