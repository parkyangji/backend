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
import com.parkyangji.openmarket.backend.dto.OrderDto;
import com.parkyangji.openmarket.backend.dto.ProductCombinationValueDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

@Mapper
public interface AdminSqlMapper {

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

  // 키워드
  public int selectKeywordGetId(String keyword_name);
  public void insertProductKeywordId(ProductKeywordValueDto productKeywordValueDto);

  // 옵션
  public void insertProductOption(ProductOptionDto productOptionDto);
  public void insertProductOptionValue(ProductOptionValueDto productOptionValueDto);
  public void createProductOptionCombinationId(ProductOptionCombinationDto productOptionCombinationDto);
  public void insertProductCombinationValue(ProductCombinationValueDto combinationValueDto);
  public void insertProductOptionInventory(ProductOptionInventoryDto inventoryDto);
}
