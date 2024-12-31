package com.parkyangji.openmarket.backend.admin.service;

import java.util.*;
import java.util.stream.Collectors;

import com.parkyangji.openmarket.backend.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.admin.mapper.AdminSqlMapper;

@Service
public class OrderService {

  @Autowired
  private AdminSqlMapper adminSqlMapper;

  public Map<Integer, Map<String, Object>> getAllOrders(int seller_id) {

    List<OrderItemReturnDto> orders = adminSqlMapper.selectSellerOrder(seller_id);
     System.out.println(orders);

    // order_id 기준으로 그룹화
    Map<Integer, List<OrderItemReturnDto>> groupedOrders = orders.stream()
            .collect(Collectors.groupingBy(OrderItemReturnDto::getOrder_id, LinkedHashMap::new, Collectors.toList()));

    // 옵션 중복 제거 및 수량 정확한 합산
    Map<Integer, Map<String, Object>> refinedOrders = groupedOrders.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                      List<OrderItemReturnDto> orderItems = entry.getValue();

                      // 대표 객체 (첫 번째 항목 사용)
                      OrderItemReturnDto representative = orderItems.get(0);

                      // combination_id 기준으로 그룹화
                      Map<Integer, List<OrderItemReturnDto>> combinationGrouped = orderItems.stream()
                              .collect(Collectors.groupingBy(OrderItemReturnDto::getCombination_id));

                      // 옵션과 수량 병합
                      String mergedOptions = combinationGrouped.values().stream()
                              .map(group -> {
                                // 옵션 병합
                                Map<String, String> optionMap = group.stream()
                                        .collect(Collectors.toMap(
                                                OrderItemReturnDto::getOptionname,
                                                OrderItemReturnDto::getOptionvalue,
                                                (existing, replacement) -> existing // 중복 옵션 제거
                                        ));

                                String options = optionMap.entrySet().stream()
                                        .map(e -> e.getValue())
                                        .collect(Collectors.joining(" x "));

                                // 수량 (각 combination_id 그룹의 첫 번째 항목만 사용)
                                int quantitySum = group.get(0).getQuantity();

                                return options;
                              })
                              .collect(Collectors.joining(" x "));

                      // 전체 수량 (combination_id 그룹의 첫 번째 항목 수량만 합산)
                      int totalQuantity = combinationGrouped.values().stream()
                              .mapToInt(group -> group.get(0).getQuantity())
                              .sum();

                      Map<String, Object> refinedOrder = new LinkedHashMap<>();
                      refinedOrder.put("nickname", representative.getNickname());
                      refinedOrder.put("title", representative.getTitle());
                      refinedOrder.put("options", mergedOptions);
                      refinedOrder.put("quantity", totalQuantity);
                      refinedOrder.put("status", representative.getStatus());
                      refinedOrder.put("order_detail_id", representative.getOrder_detail_id());

                      return refinedOrder;
                    },
                    (e1, e2) -> e1,
                    LinkedHashMap::new
            ));

    return refinedOrders;
  }

  public void updateOrderStatus(int order_detail_id, String status) {
    adminSqlMapper.updateOrderStatus(order_detail_id, status);
  }

}
