package com.parkyangji.openmarket.backend.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkyangji.openmarket.backend.user.service.UserService;

@RestController
@RequestMapping("api")
public class RestUserController {

  @Autowired
  private UserService userService;

  
}
