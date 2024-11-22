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
import com.parkyangji.openmarket.backend.dto.CartItemDto;
import com.parkyangji.openmarket.backend.dto.CustomerCartDto;
import com.parkyangji.openmarket.backend.dto.CustomerDto;
import com.parkyangji.openmarket.backend.dto.DeliveryInfoDto;
import com.parkyangji.openmarket.backend.dto.OrderDetailDto;
import com.parkyangji.openmarket.backend.dto.ProductDetailReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductFavoriteDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionDto;
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

  public List<Map<String, Object>> getSubTabCategorys(Integer parent_category_id) {
    return userSqlMapper.selectSubCategorys(parent_category_id);
  }

  public List<ProductDetailReturnDto> getCategoryProductList(List<Map<String, Object>> subCategoryIdAndName){

    // 1뎁스 기준 상품 출력
    List<Integer> categoryId = new ArrayList<>();
    for (Map<String, Object> category : subCategoryIdAndName) {
      categoryId.add((Integer) category.get("category_id"));
    }

    List<ProductDetailReturnDto> categoryAllProductList = userSqlMapper.selectProductListByCategoryId(categoryId);
    
    for (ProductDetailReturnDto product : categoryAllProductList) {
      Map<String,String> image = new HashMap<>();
      image.put("thumbnail", userSqlMapper.selectThumbnailImage(product.getProduct_id()));

      product.setImages(image);
      product.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));
      product.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
      product.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
    }
    return categoryAllProductList;
  }


  public Map<String, Object> getProductDate(int product_id) {
    Map<String, Object> productData = new HashMap<>();

    ProductDetailReturnDto product = userSqlMapper.selectProductById(product_id);
    product.setKeywords(userSqlMapper.selectProductKeywords(product_id));
    product.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
    product.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
    
    Map<String, List<String>> transformedMap = new HashMap<>();
    for (Map<String,Object> image : userSqlMapper.selectProductAllImages(product_id)) {
      String typeName = (String) image.get("type_name");
      String imageUrl = (String) image.get("image_url");
      transformedMap.computeIfAbsent(typeName, k -> new ArrayList<>()).add(imageUrl);
    }

    product.setImages(transformedMap);

    productData.put("product", product);

    return productData;
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
                productSummary.setOrigin_price(representativeItem.getOrigin_price());
                productSummary.setDiscount_rate(representativeItem.getDiscount_rate());
                productSummary.setSale_price(representativeItem.getSale_price());

                // combinationId로 옵션 그룹핑
                Map<Integer, List<OrderItemReturnDto>> groupByCombinationId = productItems.stream()
                    .collect(Collectors.groupingBy(OrderItemReturnDto::getCombination_id));

                // 옵션 정보 생성
                List<ProductOptionSummaryDto> optionList = groupByCombinationId.entrySet().stream()
                    .map(combinationEntry -> {
                        List<OrderItemReturnDto> combinationItems = combinationEntry.getValue();
                        OrderItemReturnDto representativeCombination = combinationItems.get(0);

                        // 같은 combinationId를 가진 옵션들을 합쳐서 처리
                        ProductOptionSummaryDto option = new ProductOptionSummaryDto();
                        option.setCombination_id(representativeCombination.getCombination_id());
                        option.setOrder_detail_id(representativeCombination.getOrder_detail_id());
                        option.setQuantity(representativeCombination.getQuantity());
                        option.setStatus(representativeCombination.getStatus());

                        option.setOrigin_price(representativeCombination.getOrigin_price());
                        option.setDiscount_rate(representativeCombination.getDiscount_rate());
                        option.setSale_price(representativeCombination.getSale_price());
                        option.setRating(representativeCombination.getRating());
                        option.setReview_content(representativeCombination.getReview_content());
                        option.setSeller_reply(representativeCombination.getSeller_reply());
                        
                        // 옵션의 이름과 값을 리스트 형태로 저장
                        List<Map<String, Object>> optionDetails = combinationItems.stream()
                        .map(item -> {
                          Map<String, Object> optionMap = new HashMap<>();
                          optionMap.put(item.getOptionname(), item.getOptionvalue());
                          return optionMap;
                        }).collect(Collectors.toList());

                        option.setOptionDetails(optionDetails);

                        return option;
                    }).collect(Collectors.toList());

                productSummary.setOptions(optionList);
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

  public void toggleLike(ProductFavoriteDto productFavoriteDto) {

    ProductFavoriteDto existingLike = userSqlMapper.selectLike(productFavoriteDto);

    if (existingLike == null) {
      userSqlMapper.insertLike(productFavoriteDto);
    } else {
      userSqlMapper.deleteLike(productFavoriteDto);
    }

  }

  public ProductFavoriteDto findLike(ProductFavoriteDto productFavoriteDto){
    return userSqlMapper.selectLike(productFavoriteDto);
  }

  public List<ProductOptionSummaryDto> tempoptionChoice(int product_id){
    return userSqlMapper.selectProductOptionAndInventory(product_id);
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

  public Map<Integer, Map<Integer, List<CartItemReturnDto>>> getCustomerCartItem(int customer_id) {

    List<CartItemReturnDto> cartitems = userSqlMapper.selectCustomerCartItems(customer_id);
    for (CartItemReturnDto item : cartitems) {
      item.setImage_url(userSqlMapper.selectThumbnailImage(item.getProduct_id()));
    }

    // Map<Integer, List<CartItemReturnDto>> groupByCombinationId 
    // = cartitems.stream().collect(Collectors.groupingBy(CartItemReturnDto::getProduct_id));
    Map<Integer, Map<Integer, List<CartItemReturnDto>>> groupByCombinationId = groupByProductAndCombination(cartitems);

    System.out.println(groupByCombinationId);
    
    return groupByCombinationId;
  }

  public Map<Integer, Map<Integer, List<CartItemReturnDto>>> groupByProductAndCombination(List<CartItemReturnDto> cartItems) {
    // 1단계: 제품별 그룹핑
    Map<Integer, List<CartItemReturnDto>> groupByProductId = cartItems.stream()
            .collect(Collectors.groupingBy(CartItemReturnDto::getProduct_id));

    // 2단계: 제품별로 그룹핑된 각 리스트를 다시 combination_id로 그룹핑
    Map<Integer, Map<Integer, List<CartItemReturnDto>>> groupByProductAndCombination = groupByProductId.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey, // product_id
                    entry -> entry.getValue().stream() // product_id에 속하는 CartItemReturnDto 리스트
                            .collect(Collectors.groupingBy(CartItemReturnDto::getCombination_id, LinkedHashMap::new, Collectors.toList()))
            ));

    return groupByProductAndCombination;
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


  public int totalOrderItemsPrice(Map<Integer, Map<Integer, List<CartItemReturnDto>>> groupedItems) {
    int totalPrice = 0;

    // 첫 번째 맵: product_id 별로 그룹핑된 맵
    for (Map<Integer, List<CartItemReturnDto>> combinationMap : groupedItems.values()) {
        // 두 번째 맵: 각 product_id 내에서 combination_id 별로 그룹핑된 맵
        for (List<CartItemReturnDto> cartItemList : combinationMap.values()) {
            if (!cartItemList.isEmpty()) {
                // 마지막 인덱스의 CartItemReturnDto 가져오기
                CartItemReturnDto lastItem = cartItemList.get(cartItemList.size() - 1);

                // discount_rate에 따라 sale_price 또는 origin_price 사용
                int itemPrice = (lastItem.getDiscount_rate() != null) ? lastItem.getSale_price() : lastItem.getOrigin_price();

                // 총 가격 계산 (quantity 곱하기)
                totalPrice += itemPrice * lastItem.getQuantity();
            }
        }
    }

    return totalPrice;
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

  public List<ProductDetailReturnDto> getBrandProducts(String store_name) {
    List<ProductDetailReturnDto> brandAllProducts = userSqlMapper.selectBrandProducts(store_name);
    
    for (ProductDetailReturnDto product : brandAllProducts) {
      Map<String,String> image = new HashMap<>();
      image.put("thumbnail", userSqlMapper.selectThumbnailImage(product.getProduct_id()));

      product.setImages(image);
      product.setKeywords(userSqlMapper.selectProductKeywords(product.getProduct_id()));
      product.setAvgRating(userSqlMapper.selectAvgRating(product.getProduct_id()));
      product.setReviewCount(userSqlMapper.selectReviews(product.getProduct_id()).size());
    }
    return brandAllProducts;
  }


}
