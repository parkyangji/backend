package com.parkyangji.openmarket.backend.admin.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.parkyangji.openmarket.backend.admin.mapper.AdminSqlMapper;
import com.parkyangji.openmarket.backend.config.ImageConfig;
import com.parkyangji.openmarket.backend.dto.ProductCategoryDto;
import com.parkyangji.openmarket.backend.dto.ProductCombinationValueDto;
import com.parkyangji.openmarket.backend.dto.ProductDto;
import com.parkyangji.openmarket.backend.dto.ProductImageDto;
import com.parkyangji.openmarket.backend.dto.ProductKeywordValueDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionCombinationDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionInventoryDto;
import com.parkyangji.openmarket.backend.dto.ProductOptionSummaryDto;
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
    if (uploadimageList!=null) {
      saveProductImages(productDto, uploadimageList);
    }

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
      productKeywordValueDto.setKeyword_id(getKeywordGetId(keyword));

      adminSqlMapper.insertProductKeywordId(productKeywordValueDto);
    }
  }

  public void deleteProductKeyword(int product_id, List<String> keywords) {
    for (String keyword : keywords) {
      adminSqlMapper.deleteKeyword(getKeywordGetId(keyword));
    }
  }

  public int getKeywordGetId(String keyword){
    return adminSqlMapper.selectKeywordGetId(keyword);
  }

  public void saveProductOptionsAndInventory(int product_id, 
  List<Map<String, Object>> option_combinations, List<Integer> quantity, List<Integer> price){
    // 동시성 문제 체크할것
    
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
    //System.out.println(options.toString());
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
    System.out.println(optionValuesList.toString());

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


    // 오십 조합에 오십 값들 추가 => product_combination_value
    List<ProductCombinationValueDto> combinationOptions = new ArrayList<>();
    for (int i = 0; i < combinations.size(); i++) {
        ProductOptionCombinationDto combination = combinations.get(i);
        int combination_id = combination.getCombination_id();

        // 모든 옵션 조합의 경우의 수 생성
        int index = i;
        for (ProductOptionDto option : options) {
            List<ProductOptionValueDto> values = optionValuesList.stream()
                    .filter(value -> value.getOption_id() == option.getOption_id())
                    .collect(Collectors.toList());
            int valueIndex = index % values.size();
            index /= values.size();

            ProductCombinationValueDto combinationOption = new ProductCombinationValueDto();
            combinationOption.setCombination_id(combination_id);
            combinationOption.setOption_value_id(values.get(valueIndex).getOption_value_id());
            adminSqlMapper.insertProductCombinationValue(combinationOption);
            combinationOptions.add(combinationOption);
        }
    }
    //System.out.println(combinationOptions.toString()); 

    
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



  public Map<String, Object> getProductDetail(int product_id) {
    Map<String, Object> detailEdit = new HashMap<>();

    // 정보
    ProductDto productDto = adminSqlMapper.selectProductDto(product_id);
    detailEdit.put("product", productDto);

    //
    Map<String, Object> resultMap = new HashMap<>();
    List<Map<String,Object>> categorys = adminSqlMapper.selectCategory(productDto.getCategory_id());
    
    Map<String, Object> parentCategory = categorys.stream()
    .filter(category -> category.get("parent_id") == null)
    .findFirst()
    .orElse(null);

    Map<String, Object> subCategory = categorys.stream()
    .filter(category -> category.get("parent_id") != null)
    .findFirst()
    .orElse(null);

    resultMap.put("parent_category", parentCategory);
    resultMap.put("sub_category", subCategory);

    detailEdit.put("category", resultMap);

    // 이미지
    Map<String, List<String>> imageMap = adminSqlMapper.selectProductAllImages(product_id).stream()
    .collect(Collectors.groupingBy(
            image -> (String) image.get("type_name"),
            Collectors.mapping(image -> (String) image.get("image_url"), Collectors.toList())
    ));
    detailEdit.put("imageMap", imageMap);

    // 재고/가격 관리, 할인
    // Map<Integer, List<ProductOptionSummaryDto>> groupByCombinationId 
    //   = adminSqlMapper.selectProductOptionAndInventory(product_id)
    //   .stream().collect(Collectors.groupingBy(ProductOptionSummaryDto::getCombination_id, 
    //   LinkedHashMap::new, Collectors.toList()));
    // // System.out.println(groupByCombinationId);

    // detailEdit.put("inventory", groupByCombinationId);

    // 키워드
    List<String> checkKeywords = adminSqlMapper.selectProductKeywords(product_id);
    detailEdit.put("checkKeywords", checkKeywords);

    return detailEdit;
  }


  public int getImageTypeId(String string) {
    return adminSqlMapper.selectGetImageTypeId(string);
  }

  public List<ProductDto> sellerProducts(int seller_id) {
    return adminSqlMapper.selectSellerProducts(seller_id);
  }

  public List<String> getAllKeyword(){
    return adminSqlMapper.selectAllKeyword();
  }

  public List<ProductCategoryDto> getAllCategory(){
    return adminSqlMapper.selectAllCategory();
  }

  public void replaceProductCategoryId(int product_id, int sub_category) {
    adminSqlMapper.updateCategory(product_id, sub_category);
  }

  public void replaceTitle(int product_id, String title) {
    adminSqlMapper.updateTitle(product_id, title);
  }

  public void registerDiscountRate(int product_id, int discount_rate) {
    adminSqlMapper.insertDiscountRate(product_id, discount_rate);
  }

  public void replaceChangeDiscountRate(int product_id, int change_discount_rate) {
    adminSqlMapper.updateDiscountRate(product_id, change_discount_rate);
  }

  public void deleteDiscountRate(int product_id) {
    adminSqlMapper.deleteDiscountRate(product_id);
  }

  public void replaceOptionQuantity(int combination_id, int quantity) {
    adminSqlMapper.updateQuantity(combination_id, quantity);
  }

  public void replaceOptionPrice(int combination_id, int price) {
    adminSqlMapper.updatePrice(combination_id, price);
  }

}
