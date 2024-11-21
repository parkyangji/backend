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
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.user.mapper.UserSqlMapper;
import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;
import com.parkyangji.openmarket.backend.vo.ProductOptionInventoryVo;

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


  public Map<String, Object> getCategoryProductList(int parent_category_id){
    Map<String, Object> productData = new HashMap<>();

    // 상단 메뉴 이름
    productData.put("category_name", userSqlMapper.selectCategoryName(parent_category_id));

    // 하단 2뎁스 탭 메뉴
    // category_id에서 parent_id가 같은걸 찾아 모두 출력해줘야함! (상품등록은 2뎁스 기준으로 되어있음)
    List<Map<String, Object>> subCategoryIdAndName = userSqlMapper.selectSubCategorys(parent_category_id);
    //System.out.println( userSqlMapper.selectSubCategorys(parent_category_id).toString());
    
    List<String> categoryNames = new ArrayList<>();
    for (Map<String, Object> category : subCategoryIdAndName) {
      categoryNames.add((String) category.get("category_name"));
    }
    productData.put("sub_categorys", categoryNames);
    

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
    }
    System.out.println(categoryAllProductList);

    productData.put("productList", categoryAllProductList);
    // System.out.println(categoryAllProductList);

    return productData;
  }


  public Map<String, Object> getProductDate(int product_id) {
    Map<String, Object> productData = new HashMap<>();

    ProductDetailReturnDto product = userSqlMapper.selectProductById(product_id);
    product.setKeywords(userSqlMapper.selectProductKeywords(product_id));
    // List<Map<String, Object>> imageList = userSqlMapper.selectProductAllImages(product_id);
    
    Map<String, List<String>> transformedMap = new HashMap<>();
    for (Map<String,Object> image : userSqlMapper.selectProductAllImages(product_id)) {
      String typeName = (String) image.get("type_name");
      String imageUrl = (String) image.get("image_url");
      transformedMap.computeIfAbsent(typeName, k -> new ArrayList<>()).add(imageUrl);
    }

    product.setImages(transformedMap);

    productData.put("product", product);


    // // 판매자(브랜드)
    // String storeName = userSqlMapper.selectStoreName(productDto.getSeller_id());
    // productData.put("storeName", storeName);

    // // 평점 통계 => 상품 구매 구현시 가능
    // Float avgRating =  userSqlMapper.selectAvgRating(product_id);
    // productData.put("avgRating", avgRating);

    // // 리뷰 => 상품 후기 등록 구현시 가능
    // List<Map<String,Object>> reviewData = new ArrayList<>();

    // List<ProductReviewDto> getReviews = userSqlMapper.selectReviews(product_id);
    // for (ProductReviewDto review : getReviews) {
    //   Map<String, Object> data = new HashMap<>();
    //   data.put("review", review);
    //   data.put("reviewer", userSqlMapper.selectCustomer(review.getCustomer_id()));
    //   reviewData.add(data);
    // }
    // productData.put("reviewData", reviewData);

    // 문의 => 나중에 업데이트시!!!!!!!

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

  public Map<Integer, Map<String, Map<Integer, List<OrderItemReturnDto>>>> getOrderList(int customer_id) {

    List<OrderItemReturnDto> orderList = userSqlMapper.selectOrderList(customer_id);
    
    for (OrderItemReturnDto item : orderList) {
      item.setImage_url(userSqlMapper.selectThumbnailImage(item.getProduct_id()));
    }

    // 1단계: 주문별 그룹핑
    Map<Integer, List<OrderItemReturnDto>> groupByProductId = orderList.stream()
            .collect(Collectors.groupingBy(OrderItemReturnDto::getOrder_id));

    // 2단계: 브랜드별로 그룹핑
    Map<Integer, Map<String, List<OrderItemReturnDto>>> groupByProductAndCombination = groupByProductId.entrySet().stream()
    .collect(Collectors.toMap(
            Map.Entry::getKey, // product_id
            entry -> entry.getValue().stream() // product_id에 속하는 OrderItemReturnDto 리스트
                    .collect(Collectors.groupingBy(OrderItemReturnDto::getStore_name))
    ));
    
    // 3단계 : 브랜드별로 그룹핑된 각 리스트를 다시 combination_id로 그룹핑
    Map<Integer, Map<String, Map<Integer, List<OrderItemReturnDto>>>> result = groupByProductAndCombination.entrySet().stream()
    .collect(Collectors.toMap(
        Map.Entry::getKey, // product_id
        entry -> entry.getValue().entrySet().stream() // product_id에 속하는 combination_id별로 그룹화된 맵
            .collect(Collectors.toMap(
                Map.Entry::getKey, // combination_id
                combinationEntry -> combinationEntry.getValue().stream()
                    .collect(Collectors.groupingBy(
                      OrderItemReturnDto::getCombination_id,
                      LinkedHashMap::new,
                      Collectors.toList()
                    )) // 그룹화 기준으로 묶음
            ))
    ));

    return result;
  }

  public void hasReviewsState(Map<Integer, Map<String, Map<Integer, List<OrderItemReturnDto>>>> orderItems) {
    // 배송완료 상태이며 리뷰가 없는 항목이 있는지 확인
    // return orderItems.stream()
    //     .anyMatch(order -> {
    //         // 해당 상품에 대한 리뷰 조회
    //         ProductReviewDto productReviewDto = new ProductReviewDto();
    //         // productReviewDto.setProduct_id(order.getProduct_id());
    //         // productReviewDto.setCustomer_id(order.getCustomer_id());
    //         // productReviewDto.setOrder_id(order.getOrder_id());

    //         ProductReviewDto review = userSqlMapper.selectReviewByProductAndCustomer(productReviewDto);

    //         // 배송완료 상태이고 리뷰가 없는 경우 true 반환
    //         return "배송완료".equals(order.getStatus()) && review == null;
    //     });
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

  public void saveOrUpdateReview(ProductReviewDto productReviewDto) {
    // 기존 리뷰가 존재하는지 확인

    ProductReviewDto existingReview = userSqlMapper.selectReviewByProductAndCustomer(productReviewDto);

    if (existingReview == null) {
        // 리뷰가 없으면 새로 INSERT
        userSqlMapper.insertReview(productReviewDto);
    } else {
        // 리뷰가 있으면 UPDATE
        if (productReviewDto.getRating() != 0) {
            existingReview.setRating(productReviewDto.getRating());
        }
        if (productReviewDto.getContent() != null) {
            existingReview.setContent(productReviewDto.getContent());
        }
        userSqlMapper.updateReview(existingReview);
    }
  }

  public void toggleLike(ProductFavoriteDto productFavoriteDto) {

    ProductFavoriteDto existingLike = userSqlMapper.selectLike(productFavoriteDto);
    // System.out.println(existingLike);

    if (existingLike == null) {
      userSqlMapper.insertLike(productFavoriteDto);
    } else {
      userSqlMapper.deleteLike(productFavoriteDto);
    }

  }

  public ProductFavoriteDto findLike(ProductFavoriteDto productFavoriteDto){
    return userSqlMapper.selectLike(productFavoriteDto);
  }

  public Map<String, Object> tempoptionChoice(int product_id){
    Map<String, Object> options = new HashMap<>();

    Map<Integer, List<ProductOptionInventoryVo>> groupByCombinationId 
      = userSqlMapper.selectProductOptionAndInventory(product_id)
      .stream().collect(Collectors.groupingBy(ProductOptionInventoryVo::getCombination_id, LinkedHashMap::new, Collectors.toList()));
 
    System.out.println(groupByCombinationId);
    options.put("inventory", groupByCombinationId);

    return options;
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


}
