// orderfooter.html
async function purchase(event) { // 구매하기 (cart)
  console.log('purchase')

  if (totalOriginPrice == 0 || totalOriginPrice == null) {
    return failMessagePopup('구매할 상품이 없습니다.');
  }

  // 체크박스된거 체크
  // let checked_combination = [];
  // document.querySelectorAll('.chkd[type="checkbox"]:checked').forEach((input)=>{
  //   const combination_id = input.parentElement.parentElement.parentElement.querySelector('input[type="hidden"]').value;
  //   checked_combination.push(parseInt(combination_id));
  // })

  const copyCartItems = [...cartItems]; 
  let sendCartData = [];
  copyCartItems.forEach((item)=> {
    item.products.forEach((product)=>{
      product.options.forEach((option)=>{
        if (checked_combination.indexOf(option.combination_id) != -1) {
          // console.log(option.combination_id)
          const item = {}
          item.combination_id = option.combination_id;
          item.quantity = option.quantity;
          sendCartData.push(item);
        }
      })
    })
  })

  console.log(sendCartData);

  if (sendCartData.length == 0) return failMessagePopup("구매할 상품이 없습니다.");

  const result = await fetchPurchaseProcess(sendCartData); // 주문할 아이템을 보냄 

  // window.location.replace(result.data.redirectUrl); // 현재 페이지 URL을 새로운 페이지로 대체
  window.location.href = result.data.redirectUrl; // 현재 페이지 URL을 새로운 페이지로 대체
}

async function payment(event) { // 결제하기 (order)
  console.log('payment')

  const type = {
    customerId : document.getElementById('customerId').value,
    addressId : document.getElementById('addressId').value,
    deliveryMessage : document.getElementById('deliveryMessageTextarea').value
  }

  if (document.querySelector("input[name='paymentcheck']").checked == false) 
    return failMessagePopup('결제 수단을 체크해주세요.');
  if (document.querySelector("input[name='ordercheck']").checked == false) 
    return failMessagePopup('동의 사항에 체크해주세요.');

  const result = await fetchPaymentProcess(type);

  // window.location.replace(result.data.redirectUrl); // 현재 페이지 URL을 새로운 페이지로 대체
  window.location.href = result.data.redirectUrl; // 현재 페이지 URL을 새로운 페이지로 대체
}


// option.html
async function cartAdd(){ // product.html
  //this.removeEventListener('click', cartAdd);
  console.log('cartAdd')
  if (AjaxSendData.optionData.length == 0) {
    failMessagePopup("옵션을 선택해주세요");
    return
  }

  const data = [AjaxSendData];
  let sendCartData = [];

  data.forEach((item)=> {
    item.optionData.forEach((product)=>{
      const items = {}
      // items.productId = item.productId;
      items.combination_id = product.combination_id;
      items.quantity = product.quantity;
      sendCartData.push(items);
    })
  })
  const result = await fetchPostChoiceOption(sendCartData);

  //window.location.href = result.data.redirectUrl; // 클라이언트에서 페이지 이동 (리다이렉트x) (히스토리 추가됌)
  // history.replaceState({}, '', result.data.redirectUrl); // 히스토리를 대체
  // window.location.href = result.data.redirectUrl;
  // 현재 히스토리를 /home으로 대체
  // history.replaceState({}, '', '/home');

  // /cart로 이동하며 기존 히스토리를 대체
  // window.location.replace(result.data.redirectUrl);

  optionReset();
  failMessagePopup(result.data.message);
}

async function directOrder(){ // product.html
  //this.removeEventListener('click', directOrder);
  console.log('directOrder')
  if (AjaxSendData.optionData.length == 0) {
    failMessagePopup("옵션을 선택해주세요");
    return
  }
  // failMessagePopup("서비스 준비중입니다");
  const data = [AjaxSendData];
  let sendCartData = [];

  data.forEach((item)=> {
    item.optionData.forEach((product)=>{
      const items = {}
      // items.productId = item.productId;
      items.combination_id = product.combination_id;
      items.quantity = product.quantity;
      sendCartData.push(items);
    })
  })
  const result = await fetchPurchaseProcess(sendCartData);
  if (result.result == 'success') {
    window.location.href = result.data.redirectUrl;
  } else {
    window.location.href = result.data.redirectUrl;
  }
}