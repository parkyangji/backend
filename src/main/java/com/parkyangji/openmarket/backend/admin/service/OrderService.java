package com.parkyangji.openmarket.backend.admin.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.parkyangji.openmarket.backend.admin.mapper.AdminSqlMapper;
import com.parkyangji.openmarket.backend.dto.OrderItemReturnDto;

@Service
public class OrderService {

  @Autowired
  private AdminSqlMapper adminSqlMapper;

  public Map<Integer, Map<Integer, List<OrderItemReturnDto>>> getAllOrders(int seller_id) {

    List<OrderItemReturnDto> orders = adminSqlMapper.selectSellerOrder(seller_id);
    // System.out.println(orders);

    // for (OrderItemReturnVo item : orders) {
    //   item.setImage_url(adminSqlMapper.selectThumbnailImage(item.getProduct_id()));
    // }

    // 1단계: 주문별 그룹핑
    //orders.stream().collect(Collectors.groupingBy((OrderItemReturnVo order) -> order.getCombination_id()));
    // orders.stream().collect(Collectors.groupingBy(OrderItemReturnVo::getOrder_id));

    // 2단계: 옵션별 그루핑
    Map<Integer, Map<Integer, List<OrderItemReturnDto>>> orderList = 
    orders.stream().collect(Collectors.groupingBy(
        OrderItemReturnDto::getOrder_id,
        LinkedHashMap::new,
        Collectors.groupingBy(
          OrderItemReturnDto::getCombination_id,
          LinkedHashMap::new,
          Collectors.toList()
        )
    ));

    return orderList;
  }

  public void updateOrderStatus(int order_detail_id, String status) {
    adminSqlMapper.updateOrderStatus(order_detail_id, status);
  }
}
