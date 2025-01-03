package com.parkyangji.openmarket.backend.common;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.parkyangji.openmarket.backend.dto.ProductDetailReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionSummaryDto;
import com.parkyangji.openmarket.backend.dto.ProductRatingDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.ReviewSellerReplyDto;
import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;
import com.parkyangji.openmarket.backend.dto.KeywordDto;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;

@Mapper
public interface CommonSqlMapper {
  public List<ProductCategoryDto> selectAllCategory();
  public String selectCategoryName(int category_id);
  public List<Map<String, Object>> selectCategory(int category_id);
  // 카테고리별 전체 상품
  public List<Map<String, Object>> selectSubCategorys(Integer category_id);
  // 상품관련
  public ProductDto selectProductDto(int product_id);
  public List<Map<String, Object>> selectProductAllImages(int product_id);
  public List<String> selectProductKeywords(int product_id);
  public List<KeywordDto> selectAllkeywords();
  public List<ProductOptionReturnDto> selectProductOptionAndInventory(int product_id);
  public ProductDetailReturnDto selectProductById (int product_id);
  public String selectThumbnailImage(int product_id); 
  // 평점 리뷰 답글
  public List<ProductReviewDto> selectReviewByProductId(int product_id);
  public List<ProductRatingDto> selectRatingByProductId(int product_id);
  public ReviewSellerReplyDto selectReviewReplyByOrderReviewId(int order_review_id);
  public String selectReviewerByOrderDetailId(int order_detail_id);
}
