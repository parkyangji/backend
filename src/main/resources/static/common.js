// window.addEventListener('pageshow', (event) => {
//   if (event.persisted) {
//     // 캐시된 페이지일 경우 리로드 (뒤로가기시 ajax 재랜더링)
//     window.location.reload();
//   }
// });

function back(){
  window.history.back();
}

function formatPrice(price) {
  return new Intl.NumberFormat('ko-KR').format(price);
}

function calculatePrices(originPrice, salePrice, discountRate, quantity) {
  // 총 가격 (할인 적용 전)
  const totalOriginPrice = originPrice * quantity;

  // 할인 받은 금액
  let discountAmount = 0;
  if (salePrice != null && discountRate != null) {
      discountAmount = (originPrice - salePrice) * quantity;
  }

  // 결제해야 할 금액 (할인 적용 후)
  const totalPrice = salePrice != null ? salePrice * quantity : originPrice * quantity;

  return {
      TotalOriginPrice: totalOriginPrice,
      DiscountAmount: discountAmount,
      TotalPrice: totalPrice
  };
}