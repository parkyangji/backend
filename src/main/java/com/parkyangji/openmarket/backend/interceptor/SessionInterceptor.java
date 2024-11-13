package com.parkyangji.openmarket.backend.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class SessionInterceptor implements HandlerInterceptor{

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    System.out.println("인터셉터 실행됨!!");

    HttpSession session = request.getSession();
    String requestURI = request.getRequestURI();

    if (requestURI.startsWith("/admin")) { // 관리자 페이지 요청일 때

      if (session.getAttribute("sellerInfo") == null) { // 관리자 로그인 여부 확인
          response.sendRedirect("/admin");
          return false;
      }

    } else { // 사용자 페이지 요청일 때

        if (session.getAttribute("sessionInfo") == null) { // 사용자 로그인 여부 확인
            response.sendRedirect("/login"); // 사용자 로그인 페이지로 리디렉션
            return false;
        }

    }
    return true;
  }
}
