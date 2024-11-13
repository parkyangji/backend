package com.parkyangji.openmarket.backend.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductOrderDto;
import com.parkyangji.openmarket.backend.dto.ProductReviewDto;
import com.parkyangji.openmarket.backend.dto.SellerDto;

@Mapper
public interface AdminSqlMapper {

    // 로그인 고객 찾기
  public SellerDto selectLoginCheck(SellerDto sellerDto);

  public void insertProduct(ProductDto productDto);

  public List<ProductDto> selectSellerProducts(int seller_id);

  public List<ProductOrderDto> selectOrderByProductId(int product_id);

  public List<ProductReviewDto> selectReviewByProductId(int product_id);

  public void inserReply(ProductReviewDto productReviewDto);

  public void insertSeller(SellerDto sellerDto);

}
