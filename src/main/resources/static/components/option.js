
const orderInput = document.getElementById('orderInput');
const option_container = document.getElementById('option-container');

// 사용자가 선택한 옵션 값을 추적하는 객체
const AjaxSendData= {
  productId : null,
  optionData : []
};
const PriceData = {
  TotalOriginPrice: 0,
  DiscountAmount: 0,
  TotalPrice: 0
};

let options = null; // ajax에서 받아온 옵션 데이터 

// 사용자가 선택한 옵션 값을 추적하는 객체
const selectedOptions = {};
// 특성별 옵션을 저장하는 객체 초기화
const attributesOptions = {};
// 옵션 데이터를 분석하여 각 키(특성)를 수집
const uniqueAttributes = new Set();

function optionOpen(){
  option_container.parentElement.classList.remove('invisible');
  option_container.classList.add('option-bar-active');

  // 초기 특성 선택 UI 생성
  createAttributeSelectors(attributesOptions);
}


function optionOpenEvent(event){
  // event.preventDefault();  // document에는 안됌 
  // event.stopPropagation();

  // console.log(event.target)
  if (event.target == document.querySelector('#popupback') || event.target == option_container.querySelector('button')) {
    option_container.parentElement.classList.add('invisible');
    option_container.classList.remove('option-bar-active');
    document.removeEventListener('click', optionOpenEvent);
  }
}

function optionReset(){
  option_container.parentElement.classList.add('invisible');
  document.getElementById('option-container').classList.remove('option-bar-active');
  document.getElementById('option-container').classList.remove('order-bar-active');
  // const container = document.getElementById('option-content');
  // container.querySelectorAll('.attribute-container').forEach((el)=>el.remove())
  Array.from(document.getElementById('option-content').children).forEach((el)=>el.remove())
  Array.from(document.getElementById('option-choice-result').children).forEach((el)=>el.remove())
  Array.from(document.getElementById('order-result').children).forEach((el)=>el.remove())

  AjaxSendData.optionData = [];
  AjaxSendData.productId = null;
  PriceData.DiscountAmount = 0;
  PriceData.TotalOriginPrice = 0;
  PriceData.TotalPrice = 0;
  Object.keys(selectedOptions).forEach(key => delete selectedOptions[key]);
  Object.keys(attributesOptions).forEach(key => delete attributesOptions[key]);
  Object.keys(uniqueAttributes).forEach(key => delete uniqueAttributes[key]);

  createAttributeSelectors(attributesOptions);
}



orderInput.addEventListener('click', async (event) => {
  event.preventDefault(); 
  event.stopPropagation();

  // 장바구니, 바로구매, 팝업 이벤트 주입
  document.getElementById('cartAdd').addEventListener('click', cartAdd);
  document.getElementById('directOrder').addEventListener('click', directOrder);
  document.addEventListener('click', optionOpenEvent);

  const urlParams = new URLSearchParams(window.location.search);
  const productId = urlParams.get('id');  
  AjaxSendData.productId = productId;

  const result = await fetchGetProductOption(productId);
  console.log(result)

  // 데이터 가공
  options = result.data.options;

  options.forEach(option => {
    option.optionDetails.option.forEach(attr => {
        Object.keys(attr).forEach(key => uniqueAttributes.add(key));
    });
  });

  uniqueAttributes.forEach(attribute => {
    attributesOptions[attribute] = new Set();
  });

  // 옵션 데이터를 순회하면서 각 특성별 옵션 값을 수집
  options.forEach(option => {
    option.optionDetails.option.forEach(attr => {
        Object.entries(attr).forEach(([key, value]) => {
            attributesOptions[key].add(value);
        });
    });
  });

  optionOpen();
})

// HTML 요소 생성 함수
function createButton(value, onClickHandler) {
  const button = document.createElement('button');
  button.textContent = value;
  button.className = 'option-button text-start w-100';
  button.addEventListener('click', () => onClickHandler(value));
  return button;
}


// 옵션 선택 처리 함수
function handleOptionSelection(attribute, selectedValue) {
  selectedOptions[attribute] = selectedValue;
  console.log(`선택된 ${attribute}: ${selectedValue}`);

  filterAvailableOptions();
  updateSelectedOptionsDisplay();

}

// 선택된 옵션 값을 출력하는 함수
function updateSelectedOptionsDisplay() {
  console.log(selectedOptions)

  // 모든 옵션이 선택되었는지 확인
  if (Object.keys(selectedOptions).length === uniqueAttributes.size) {
    const container = document.getElementById('option-choice-result');
    let selectedOptionsText = Object.entries(selectedOptions)
        .map(([key, value]) => `${value}`)
        .join(' / ');

    // 선택한 옵션에 해당하는 combination_id 찾기
    const selectedCombination = options.find(option => {
        return option.optionDetails.option.every(attr => {
            const [key, value] = Object.entries(attr)[0];
            return selectedOptions[key] === value;
        });
    });
    const combinationId = selectedCombination ? selectedCombination.combination_id : null;

    // 기존의 선택된 옵션이 이미 있는지 확인
    const existingSummary = Array.from(container.getElementsByClassName('selected-options-summary')).find(div =>
      div.querySelector('span').textContent === selectedOptionsText);

    if (existingSummary) {
      // 기존 옵션이 있으면 수량 증가
      const quantitySpan = existingSummary.querySelector('.quantity-container .quantity');
      quantitySpan.textContent = parseInt(quantitySpan.textContent) + 1;

      // AjaxSendData 업데이트
      const existingData = AjaxSendData.optionData.find(data => data.combination_id === combinationId);
      if (existingData) {
        existingData.quantity += 1;
      }
    } else {
      // 새로운 선택된 옵션 표시 div 생성
      if (selectedOptionsText) {
        const summaryDiv = document.createElement('div');
        summaryDiv.className = 'selected-options-summary d-flex justify-content-between';
        const optionSpan = document.createElement('span');
        optionSpan.textContent = `${selectedOptionsText}`;
        
        const quantityContainer = document.createElement('div');
        quantityContainer.className = 'quantity-container d-flex align-items-center gap-3';
        const increaseButton = document.createElement('span');
        increaseButton.textContent = '+';
        increaseButton.className = 'increase-button';
        increaseButton.addEventListener('click', () => {
          quantitySpan.textContent = parseInt(quantitySpan.textContent) + 1;
          updateAjaxSendData(combinationId, parseInt(quantitySpan.textContent));
          updatePriceData(combinationId);
        });
        
        const decreaseButton = document.createElement('span');
        decreaseButton.textContent = '-';
        decreaseButton.className = 'decrease-button';
        decreaseButton.addEventListener('click', () => {
          if (parseInt(quantitySpan.textContent) > 1) {
            quantitySpan.textContent = parseInt(quantitySpan.textContent) - 1;
            updateAjaxSendData(combinationId, parseInt(quantitySpan.textContent));
            updatePriceData(combinationId);
          }
        });
        
        const quantitySpan = document.createElement('span');
        quantitySpan.textContent = `1`;
        quantitySpan.className = 'quantity';
        
        quantityContainer.appendChild(decreaseButton);
        quantityContainer.appendChild(quantitySpan);
        quantityContainer.appendChild(increaseButton);
        
        summaryDiv.appendChild(optionSpan);
        summaryDiv.appendChild(quantityContainer);
        container.appendChild(summaryDiv);

        // const optionDetails = options.find((data)=> {
        //   return data.combination_id == combinationId}).optionDetails;
        // const calResult = calculatePrices(optionDetails.origin_price, optionDetails.sale_price ,optionDetails.discount_rate, 1);
        // Object.keys(calResult).forEach(key => {PriceData[key] += calResult[key];});
        // console.log(PriceData)

        // AjaxSendData에 combination_id와 수량 정보 추가
        if (combinationId) {
          AjaxSendData.optionData.push({combination_id : combinationId , quantity : 1})
          updatePriceData(combinationId);
        }

        // 가격 체크 띄우기
        if (!document.getElementById('option-container').classList.contains('order-bar-active')) {
          document.getElementById('option-container').classList.add('order-bar-active');
          document.getElementById('order-result').innerHTML = `
            <div class="border-light-subtle border-top">
              <div>총 금액 : ${PriceData.TotalOriginPrice}</div>
              <div>할인 받은 금액 : ${PriceData.DiscountAmount}</div>
              <div>결제 예상 금액 : ${PriceData.TotalPrice}</div>
            </div>
          `;
          scrollToBottom(document.getElementById('option-container'));
        }
      }
    }

    Object.keys(selectedOptions).forEach(key => delete selectedOptions[key]);
  }
}

// 사용자가 선택한 옵션에 따라 사용 가능한 옵션을 필터링하는 함수
function filterAvailableOptions() {
  const filteredOptions = options.filter(option => {
      return option.optionDetails.option.every(attr => {
          const [key, value] = Object.entries(attr)[0];
          return !selectedOptions[key] || selectedOptions[key] === value;
      });
  });

  // 필터링된 옵션 데이터를 기반으로 특성 선택 UI 업데이트
  const updatedAttributesOptions = {};
  uniqueAttributes.forEach(attribute => {
      updatedAttributesOptions[attribute] = new Set();
  });

  filteredOptions.forEach(option => {
      option.optionDetails.option.forEach(attr => {
          Object.entries(attr).forEach(([key, value]) => {
              updatedAttributesOptions[key].add(value);
          });
      });
  });

  //createAttributeSelectors(updatedAttributesOptions);
  createAttributeSelectors(attributesOptions);
}

// 특성 선택 UI 생성 함수 (아코디언 스타일 적용)
function createAttributeSelectors(attributesOptions) {
  const container = document.getElementById('option-content');
  container.querySelectorAll('.attribute-container').forEach((el)=>el.remove())
  // container.innerHTML = ''; // 기존 컨텐츠 초기화

  Object.keys(attributesOptions).forEach((attribute, i) => {
      const attributeContainer = document.createElement('div');
      attributeContainer.className = 'attribute-container';

      const attributeTitle = document.createElement('span');
      attributeTitle.textContent = attribute;
      attributeTitle.className = 'attribute-title';
      attributeContainer.appendChild(attributeTitle);

      const attributeContent = document.createElement('div');
      attributeContent.className = 'attribute-content';
      attributeContent.style.display = 'none';

      attributesOptions[attribute].forEach(optionValue => {
        const button = createButton(optionValue, (selectedValue) => {
            handleOptionSelection(attribute, selectedValue);
        });
        attributeContent.appendChild(button);
      });

      attributeContainer.appendChild(attributeContent);
      container.appendChild(attributeContainer);

      attributeTitle.addEventListener('click', (event) => {
        event.preventDefault();
        const content = option_container.querySelectorAll('.attribute-content');
        content.forEach((el)=>{if (el.style.display == 'block') el.style.display = 'none';})

        const currentContent = attributeContainer.querySelector('.attribute-content');
        if (currentContent.style.display === 'none' || !currentContent.style.display) {
            currentContent.style.display = 'block';
        } else {
            currentContent.style.display = 'none';
        }
      });
  });

  const content = option_container.querySelectorAll('.attribute-content');
  const idx = Object.keys(selectedOptions).length -1;
  if (idx < content.length -1) {
    content[idx+1].style.display = 'block';
  } 
}



// AjaxSendData 업데이트 함수
function updateAjaxSendData(combinationId, quantity) {
  const existingData = AjaxSendData.optionData.find(data => data.combination_id === combinationId);
  if (existingData) {
    existingData.quantity = quantity;
  }
}

function updatePriceData(combinationId) {
  const optionDetails = options.find((data)=> {
    return data.combination_id == combinationId}).optionDetails;
  const calResult = calculatePrices(optionDetails.origin_price, optionDetails.sale_price ,optionDetails.discount_rate, 1);
  Object.keys(calResult).forEach(key => {PriceData[key] += calResult[key];});
  console.log(PriceData)
  console.log(AjaxSendData)

  // 가격 체크 띄우기
  if (document.getElementById('option-container').classList.contains('order-bar-active')) {
    document.getElementById('option-container').classList.add('order-bar-active');
    document.getElementById('order-result').innerHTML = `
      <div class="border-light-subtle border-top">
        <div>총 금액 : ${PriceData.TotalOriginPrice}</div>
        <div>할인 받은 금액 : ${PriceData.DiscountAmount}</div>
        <div>결제 예상 금액 : ${PriceData.TotalPrice}</div>
      </div>
    `;
    scrollToBottom(document.getElementById('option-container'));
  }
}


function scrollToBottom(container) {
  container.scrollTo({
    top: container.scrollHeight,
    behavior: 'smooth'  // 부드럽게 스크롤
  });
}

