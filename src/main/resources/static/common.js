// window.addEventListener('pageshow', (event) => {
//   if (event.persisted) {
//     // 캐시된 페이지일 경우 리로드 (뒤로가기시 ajax 재랜더링)
//     window.location.reload();
//   }
// });

function back(){
  //window.history.back();

  const currentUrl = window.location.pathname;
  console.log(currentUrl)
  console.log(window.history)
  
  // 특정 URL에서는 뒤로가기 금지
  if (currentUrl === '/cart') {
    // console.warn('Back navigation is not allowed from /cart');
    // window.location.href = '/home'; // 안전한 페이지로 이동
    window.history.back();
    return;
  }
  if (currentUrl === '/mypage/review') {
    window.location.href = '/mypage'; // 안전한 페이지로 이동
    return;
  }

  // 이전 페이지가 있을 경우 뒤로가기
  if (window.history.length > 1) {
    window.history.back();
  } else {
    // 이전 페이지가 없을 경우 기본 페이지로 이동
    window.location.href = '/home';
  }
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


function failMessagePopup(text){
  document.querySelector('#f-popup').classList.remove('d-none');
  document.querySelector('#f-popup').classList.add("popup");
  document.querySelector('#f-popup-message').innerText = text;
  document.querySelector('#f-popup').addEventListener('animationend', ()=>{
    document.querySelector('#f-popup').classList.add('d-none');
    document.querySelector('#f-popup').classList.remove("popup");
  })
}