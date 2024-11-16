package com.parkyangji.openmarket.backend.dto;

import lombok.Data;

@Data
public class ImageTypeDto {
  private int image_type_id;
  private String type_name; //"thumbnail", "detail"
}
