package com.parkyangji.openmarket.backend.admin.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.parkyangji.openmarket.backend.admin.mapper.AdminSqlMapper;
import com.parkyangji.openmarket.backend.config.ImageConfig;
import com.parkyangji.openmarket.backend.dto.ProductCombinationValueDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductImageDto;
import com.parkyangji.openmarket.backend.dto.ProductKeywordValueDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionCombinationDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionInventoryDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionValueDto;

// Service: 비즈니스 로직을 처리.
@Service
public class ProductService {

  @Autowired
  private AdminSqlMapper adminSqlMapper;
  @Autowired
  private ImageConfig imageConfig;

  // 각 클래스, 메서드는 하나의 역할만 수행 (SRP)
  public void registerProduct(ProductDto productDto, Map<String, Object> uploadimageList, List<String> keywords, 
  List<Map<String, Object>> option_combinations, List<Integer> quantity, List<Integer> price){

    // 1. 상품 기본 정보 저장 (카테고리, 상품명 등)
    getIdAndResigterProduct(productDto);
    System.out.println(productDto.getProduct_id());
    // 2. 이미지 업로드
    saveProductImages(productDto, uploadimageList);

    // 3. 키워드 등록
    if (keywords!=null) {
      saveProductKeywords(productDto.getProduct_id(), keywords);
    }

    // 4. 옵션 및 재고 등록
    saveProductOptionsAndInventory(productDto.getProduct_id(), option_combinations, quantity, price);
  }

  public int getIdAndResigterProduct(ProductDto params) {
    return adminSqlMapper.insertProductAndGetId(params);
  }

  public void saveProductKeywords(int product_id, List<String> keywords){
    for (String keyword : keywords) {
      ProductKeywordValueDto productKeywordValueDto = new ProductKeywordValueDto();
      productKeywordValueDto.setProduct_id(product_id);
      productKeywordValueDto.setKeyword_id(adminSqlMapper.selectKeywordGetId(keyword));

      adminSqlMapper.insertProductKeywordId(productKeywordValueDto);
    }
  }

  public void saveProductOptionsAndInventory(int product_id, 
  List<Map<String, Object>> option_combinations, List<Integer> quantity, List<Integer> price){
    // 동시성 문제 체크할것
    
    /*
    [
      {사이즈 : [S,M,L] },
      {색상 : [검정, 흰색] },
    ]
    */
    

    List<String> option_name = new ArrayList<>(); // [사이즈, 색상]
    for (Map<String, Object> map : option_combinations) {
        option_name.addAll(map.keySet());
    }

    // 상품별 옵셥 이름 넣기 => product_option
    List<ProductOptionDto> options = new ArrayList<>();
    for (String optionName : option_name) {
        ProductOptionDto dto = new ProductOptionDto();
        dto.setProduct_id(product_id); // 상품 ID 설정
        dto.setOptionvalue(optionName); // 옵션 이름 설정
        adminSqlMapper.insertProductOption(dto);
        options.add(dto);
    }

    //adminSqlMapper.insertProductOptionsReturnId(options); // MyBatis는 생성된 기본 키를 options 리스트의 각 객체에 자동으로 채웁니다.
    
    // 옵션 이름에 옵션 값들 넣기 => product_option_value
    List<ProductOptionValueDto> optionValuesList = new ArrayList<>();
    for (ProductOptionDto productOptionDto : options) {
        String getOptionName = productOptionDto.getOptionvalue(); // 사이즈, 색상
        int getOptionId = productOptionDto.getOption_id();

        for (Map<String, Object> map : option_combinations) {
            if (map.containsKey(getOptionName)) {
                List<String> optionValues = (List<String>) map.get(getOptionName);
                for (String optionValue : optionValues) {
                    ProductOptionValueDto valueDto = new ProductOptionValueDto();
                    valueDto.setOption_id(getOptionId); // 해당 옵션 이름에 저장된 아이디 설정
                    valueDto.setOptionvalue(optionValue); // 옵션 값 설정
                    adminSqlMapper.insertProductOptionValue(valueDto);
                    optionValuesList.add(valueDto);
                }
            }
        }
    }
      
    // 조합 번호 생성 (경우의 수) => product_option_combination
    int option_cases_count = 1;
    for (Map<String, Object> map : option_combinations) {
        for (Object values : map.values()) {
            option_cases_count *= ((List<?>) values).size();
        }
    }

    List<ProductOptionCombinationDto> combinations = new ArrayList<>();
    for (int i = 1; i <= option_cases_count; i++) {
        ProductOptionCombinationDto productOptionCombinationDto = new ProductOptionCombinationDto();
        productOptionCombinationDto.setProduct_id(product_id);
        adminSqlMapper.createProductOptionCombinationId(productOptionCombinationDto);
        combinations.add(productOptionCombinationDto);
    }


    // 옵션 조합에 옵션 값들 추가 => product_combination_value
    for (ProductOptionCombinationDto combination : combinations) {
      int combination_id = combination.getCombination_id();
      for (ProductOptionValueDto valueDto : optionValuesList) {
          ProductCombinationValueDto combinationValueDto = new ProductCombinationValueDto();
          combinationValueDto.setCombination_id(combination_id);
          combinationValueDto.setOption_value_id(valueDto.getOption_id());
          adminSqlMapper.insertProductCombinationValue(combinationValueDto);
      }
    }

    
    // 조합에 재고 수량과 가격 추가 => product_option_inventory
    for (int i = 0; i < combinations.size(); i++) {
        ProductOptionCombinationDto combination = combinations.get(i);
        ProductOptionInventoryDto inventoryDto = new ProductOptionInventoryDto();
        inventoryDto.setCombination_id(combination.getCombination_id());
        inventoryDto.setQuantity(quantity.get(i));
        inventoryDto.setPrice(price.get(i));
        adminSqlMapper.insertProductOptionInventory(inventoryDto);
    }
  }


  public int getImageTypeId(String string) {
    return adminSqlMapper.selectGetImageTypeId(string);
  }

  public void saveProductImages(ProductDto productDto, Map<String, Object> uploadimageList) {

      // 추출된 값은 final로 선언하여 메서드 내에서 값이 변경되지 않도록 보장.
      final int sellerId = productDto.getSeller_id();
      final int subCategory = productDto.getCategory_id();
      final int productId = productDto.getProduct_id();
      final MultipartFile[] thumbnailImages = (MultipartFile[]) uploadimageList.get("thumbnail");
      final MultipartFile[] detailImages = (MultipartFile[]) uploadimageList.get("detail");

      String rootPath = "/Users/parkyangji/uploadFiles/";

      // 폴더 생성
      createFolderPath(rootPath, sellerId, subCategory);

      String url = "ProductUploadImage/" + sellerId + "/" + subCategory + "/";
      String inserUrl = imageConfig.getBasePath() + url;
      String rootUrl = rootPath + url;

      // 이미지 저장 및 DTO 생성
      List<ProductImageDto> imageList = new ArrayList<>();
      processImages(thumbnailImages, "thumbnail", rootUrl, inserUrl, productId, imageList);
      processImages(detailImages, "detail", rootUrl, inserUrl, productId, imageList);

      // DB 저장
      for (ProductImageDto image : imageList) {
        adminSqlMapper.insertImage(image);
      }
  }

  private String createFolderPath(String rootPath ,int sellerId, int subCategory) {
      String folderPath = rootPath + "ProductUploadImage/" + sellerId + "/" + subCategory + "/";
      File folder = new File(folderPath);
      if (!folder.exists()) {
          if (folder.mkdirs()) {
              System.out.println("폴더 생성 성공: " + folder.getAbsolutePath());
          } else {
              throw new RuntimeException("폴더 생성 실패: " + folder.getAbsolutePath());
          }
      }
      return folderPath;
  }

    private void processImages(MultipartFile[] images, String imageType, String rootUrl, String inserUrl, int productId,
                               List<ProductImageDto> imageList) {
        int typeId = getImageTypeId(imageType); // 이미지 타입 ID 가져오기

        for (MultipartFile image : images) {
            try {
                // 파일 저장 및 URL 생성
                String filename = saveImageToFileSystem(image, rootUrl);

                String imageUrl = inserUrl + filename; // 상대경로 url
                //DTO 생성
                ProductImageDto productImageDto = new ProductImageDto();
                productImageDto.setImage_type_id(typeId);
                productImageDto.setImage_url(imageUrl);
                productImageDto.setProduct_id(productId);
                imageList.add(productImageDto);

            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패: " + image.getOriginalFilename(), e);
            }
        }
    }

    private String saveImageToFileSystem(MultipartFile image, String folderPath) throws IOException {
        String originalFilename = image.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        String filename = uuid + "_" + currentTime + ext;

        // 파일 저장
        File destination = new File(folderPath + filename);
        image.transferTo(destination);

        return filename;
    }
}
