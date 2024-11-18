package com.parkyangji.openmarket.backend.common;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductImageDto;

@Mapper
public interface CommonSqlMapper {
  public String selectCategoryName(int category_id);
  public List<Map<String, Object>> selectCategory(int category_id);
  public ProductDto selectProductDto(int product_id);
  public List<Map<String, Object>> selectProductAllImages(int product_id);
}
