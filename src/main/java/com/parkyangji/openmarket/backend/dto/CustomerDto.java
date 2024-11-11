package com.parkyangji.openmarket.backend.dto;


import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // 고객 테이블
public class CustomerDto { 
  private int customer_id;
  private String username;
  private String password;
  private String nickname;
  private String gender;
  private String phone;
  private Date signup_date;
}
