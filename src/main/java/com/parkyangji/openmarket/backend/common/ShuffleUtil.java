package com.parkyangji.openmarket.backend.common;

import java.util.Collections;
import java.util.List;

public class ShuffleUtil {

  // Collections는 Java 표준 라이브러리의 유틸리티 클래스로, 컬렉션 관련 기능을 제공합니다. 
  // 이 클래스의 메서드들은 모두 static으로 정의되어 있기 때문에, 인스턴스를 생성하지 않고도 바로 사용할 수 있습니다.
  //  Collections는 인스턴스를 생성할 수 없는 클래스이기 때문에 이렇게 선언하는 것은 의미가 없습니다.
  
  public static <T> List<T> shuffle(List<T> list) {
    Collections.shuffle(list); 
    return list; 
  }
}
