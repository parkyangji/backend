package com.parkyangji.openmarket.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class BrandSummaryDto {
    private String store_name;
    private List<ProductSummaryDto> products;
}
