package com.parkyangji.openmarket.backend.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductImageDto;
import com.parkyangji.openmarket.backend.dto.ProductKeywordValueDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionCombinationDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionInventoryDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionValueDto;
import com.parkyangji.openmarket.backend.common.CommonSqlMapper;
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductCombinationValueDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

@Mapper
public interface AdminSqlMapper extends CommonSqlMapper{

  // 로그인 고객 찾기
  public SellerDto selectLoginCheck(SellerDto sellerDto);

  public void insertProduct(ProductDto productDto);

  public List<ProductDto> selectSellerProducts(int seller_id);

  public List<OrderDto> selectOrderByProductId(int product_id);

  public List<ProductReviewDto> selectReviewByProductId(int product_id);

  public void inserReply(ProductReviewDto productReviewDto);

  public void insertSeller(SellerDto sellerDto);

  public int insertProductAndGetId(ProductDto productDto);

  public int selectGetImageTypeId(String string);

  public void insertImage(ProductImageDto productImageDto);

  // 카테고리
  public List<ProductCategoryDto> selectAllCategory();

  // 키워드
  public List<String> selectAllKeyword();
  public int selectKeywordGetId(String keyword_name);
  //public List<String> selectProductKeywords(int product_id);
  public void insertProductKeywordId(ProductKeywordValueDto productKeywordValueDto);
  public void deleteKeyword(int keyword_id);

  // 옵션
  public void insertProductOption(ProductOptionDto productOptionDto);
  public void insertProductOptionValue(ProductOptionValueDto productOptionValueDto);
  public void createProductOptionCombinationId(ProductOptionCombinationDto productOptionCombinationDto);
  public void insertProductCombinationValue(ProductCombinationValueDto combinationValueDto);
  public void insertProductOptionInventory(ProductOptionInventoryDto inventoryDto);

  // 주문조회
  public List<OrderItemReturnDto> selectSellerOrder(int seller_id);

  public void updateCategory(@Param("product_id") int product_id ,@Param("category_id") int sub_category);
  public void updateTitle(@Param("product_id") int product_id, @Param("title") String title);
  public void insertDiscountRate(@Param("product_id") int product_id, @Param("discount_rate") int discount_rate);
  public void updateDiscountRate(@Param("product_id") int product_id, @Param("change_discount_rate") int change_discount_rate);
  public void deleteDiscountRate(int product_id);
  public void updatePrice(@Param("combination_id") int combination_id, @Param("price") int price);
  public void updateQuantity(@Param("combination_id") int combination_id, @Param("quantity") int quantity);

  public void updateOrderStatus(@Param("order_detail_id") int order_detail_id, @Param("status") String status);
}
