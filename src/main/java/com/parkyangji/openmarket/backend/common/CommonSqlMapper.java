package com.parkyangji.openmarket.backend.common;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.parkyangji.openmarket.backend.dto.ProductDetailReturnDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.CartItemReturnDto;
import com.parkyangji.openmarket.backend.vo.ProductOptionInventoryVo;

@Mapper
public interface CommonSqlMapper {
  public String selectCategoryName(int category_id);
  public List<Map<String, Object>> selectCategory(int category_id);
  
  // 상품관련
  public ProductDto selectProductDto(int product_id);
  public List<Map<String, Object>> selectProductAllImages(int product_id);
  public List<String> selectProductKeywords(int product_id);
  public List<ProductOptionInventoryVo> selectProductOptionAndInventory(int product_id);
  public ProductDetailReturnDto selectProductById (int product_id);
  public String selectThumbnailImage(int product_id); 

}
