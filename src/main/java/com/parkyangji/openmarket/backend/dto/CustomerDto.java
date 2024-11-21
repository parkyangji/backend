package com.parkyangji.openmarket.backend.dto;


import java.util.Date;

import lombok.Data;

@Data // 고객 테이블
public class CustomerDto { 
  private int customer_id;
  private String username;
  private String password;
  private String nickname;
  private Date signup_date;
}
