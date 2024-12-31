// 검색 기능 관련 전역에 productList 저장
let saveProductList = {
  productList : null,
  likeList : null
};

function updateSaveList(productdata, likedata){
  saveProductList.productList = productdata;
  saveProductList.likeList = likedata;
}

// 키워드 필터
async function keywordClick(event) {
  event.preventDefault();
  event.stopPropagation();
  
  let self = event.currentTarget;
  let keyword = self.innerText;

  const urlParams = new URLSearchParams(window.location.search);

  // 'menu_id'의 값을 가져오기
  const menuId = urlParams.get('menu_id');
  const sub_category_id = urlParams.get('sub_category_id');

  console.log(menuId, sub_category_id)
  const result = await fetchKeywordFromCategory(keyword, menuId, sub_category_id);
  console.log(result);
  // 검색 구현을 위한 데이터 전역 저장
  if (result.result !== 'fail') updateSaveList(result.data.productList, result.data.likeList);

  for (keyword of document.getElementById('keywords').children) keyword.classList.remove("keyword-on");
  self.classList.add("keyword-on");

  updateProductList(container, result.data.productList, result.data.likeList); 
}


// 네비 
async function subTabClick(event, type, store_name = null){
  event.preventDefault();
  event.stopPropagation();

  // console.log(event, type, store_name)

  let self = event.currentTarget;
  let category_id = self.dataset.categoryId;

  const subNavChild = Array.from(document.querySelector("#subNav").children);
  if (type == 'list') { 
    const keywordFilter = document.getElementById('keywords').children
    for (keyword of keywordFilter) keyword.classList.remove("keyword-on");

    const searchContainer = document.querySelector('.search-container');
    searchContainer.classList.remove('active');
    document.getElementById('keywords').classList.remove('invisible');
  }

  // 현재 URL 가져오기
  const url = new URL(window.location.href);

  // 쿼리 스트링에 sub_category_id 값 추가 또는 업데이트
  if (category_id !== undefined || category_id !== null) url.searchParams.set('sub_category_id', category_id);
  if (subNavChild[0] == self)  url.searchParams.delete('sub_category_id');

  // 주소창 URL 업데이트 (페이지 리로드 없이) !!!!!!!!!!!!
  // window.history.pushState({}, '', url);
  window.history.replaceState({}, '', url);

  let result;
  
  if (type == 'list') {
    if (subNavChild[0] == self) {
      result = await fetchProductList(allSubCategoryIds);
    } else {
      result = await fetchProductList(category_id);
    }
  }
  if (type == 'brand') {
    if (subNavChild[0] == self) {
      result = await fetchBrandProductList(store_name);
    } else {
      result = await fetchBrandProductList(store_name, category_id);
    }
  }
  
  console.log(result);
  // 검색 구현을 위한 데이터 전역 저장
  if (result.result !== 'fail') updateSaveList(result.data.productList, result.data.likeList);

  container.replaceChildren();

  subNavChild.forEach(nav => {nav.classList.remove("menu-on");})
  self.classList.add("menu-on");

  // 리스트 재렌더링
  updateProductList(container, result.data.productList, result.data.likeList); 
  // 서브네비는 ui가 동적이지 않아서 한번만 이벤트 걸어줘야함!!
}

// 좋아요
async function like(event) {
  // 이벤트 실행시, 이벤트에 등록된 함수가 실행되는데요, 이때 이 함수에 event 객체(이름은 어느것이든 좋습니다.)가 매개변수로 주어진다는 뜻입니다
  event.preventDefault();
  event.stopPropagation();

  let self = event.currentTarget;
  // console.log(self.dataset.productId)

  const result = await fetchCheckIsUserLike(self.dataset.productId);
  console.log(result);
  // 검색 구현을 위한 데이터 전역 저장
  if (result.result !== 'fail') updateSaveList(result.data.productList, result.data.likeList);

  if (result.result === 'fail') {
    failMessagePopup(result.reason);
  }
  if (result.result == 'success') {
    const heart = self.querySelector('input');
    if (result.data.isLike) {
      heart.checked = true;
      heart.classList.add('like-on');
    } else {
      heart.checked = false;
      heart.classList.remove('like-on');
    }
  }
}

/**
 * updateProductList
 * @param {*} container // 부모 컨테이너
 * @param {*} products  // product 데이터
 * @param {*} likeData  // product 데이터
 */
function updateProductList(container , products, likeData) {
  // console.log(container, products, likeData)
  if (products.length === 0 && likeData === null) return container.innerHTML = ``;
  container.innerHTML = ``;
  // products.forEach((data, i) => {
  //   const productDiv = document.createElement('div');
  //   productDiv.className = 'p-0 col-6 position-relative';

  //   const productLink = document.createElement('a');
  //   productLink.href = `/product?id=${data.product_id}`;

  //   const flexColumnDiv = document.createElement('div');
  //   flexColumnDiv.className = 'd-flex flex-column fs-14';

  //   // Image container
  //   const imgContainer = document.createElement('div');
  //   imgContainer.className = 'position-relative overflow-hidden';

  //   const productImg = document.createElement('img');
  //   productImg.style.cssText = 'width: 100%; height: 230px; object-fit: cover; object-position: top;';
  //   productImg.src = data.image_url;
  //   productImg.alt = data.title;

  //   const likeSpan = document.createElement('span');
  //   likeSpan.className = 'position-absolute z-2 bottom-0 end-0';
  //   likeSpan.dataset.productId = data.product_id;
  //   likeSpan.onclick = (event) => like(event);

  //   const likeCheckbox = document.createElement('input');
  //   likeCheckbox.value = 'favorite-button';
  //   likeCheckbox.name = 'favorite-checkbox';
  //   likeCheckbox.className = 'favorite';
  //   likeCheckbox.type = 'checkbox';
  //   if (likeData != null && likeData[i] != null) likeCheckbox.checked = true;

  //   const likeLabel = document.createElement('label');
  //   likeLabel.className = 'favorite-container';
  //   likeLabel.htmlFor = 'favorite';

  //   const likeSvg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  //   likeSvg.className = 'feather feather-heart';
  //   likeSvg.setAttribute('stroke-linejoin', 'round');
  //   likeSvg.setAttribute('stroke-linecap', 'round');
  //   likeSvg.setAttribute('stroke-width', '2');
  //   likeSvg.setAttribute('stroke', 'currentColor');
  //   likeSvg.setAttribute('fill', 'none');
  //   likeSvg.setAttribute('viewBox', '0 0 24 24');
  //   likeSvg.setAttribute('height', '24');
  //   likeSvg.setAttribute('width', '24');

  //   const likePath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  //   likePath.setAttribute('d', 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z');

  //   likeSvg.appendChild(likePath);
  //   likeLabel.appendChild(likeSvg);
  //   likeSpan.appendChild(likeCheckbox);
  //   likeSpan.appendChild(likeLabel);
  //   imgContainer.appendChild(productImg);
  //   imgContainer.appendChild(likeSpan);

  //   // 임시 모달 추가 -- 24.11.25 //
  //   /*
  //   const modalSpan = document.createElement('span');
  //   modalSpan.className = 'bottom-0 end-0 position-absolute text-white z-2';
  //   modalSpan.style.cssText = 'margin-bottom: 3em;';
  //   modalSpan.dataset.productId = data.product_id;
  //   modalSpan.onclick = (event) => tempModal(event);

  //   const modalIcon = document.createElement('i');
  //   modalIcon.className = 'bi bi-search d-block fs-4';
  //   modalIcon.style.cssText = 'margin-right: 0.5em;';

  //   const modalInput = document.createElement('input');
  //   modalInput.style = 'display: none';
  //   modalInput.value = 'temp-modal';
  //   modalInput.name = 'temp-modal';
  //   modalInput.type = 'checkbox';

  //   const modalLabel = document.createElement('label');
  //   modalLabel.appendChild(modalIcon);

  //   modalSpan.appendChild(modalInput);
  //   modalSpan.appendChild(modalLabel);
  //   imgContainer.appendChild(modalSpan);
  //   */
  //   //

  //   // Content container
  //   const contentDiv = document.createElement('div');
  //   contentDiv.className = 'px-3 pt-2 pb-4 mb-2';

  //   const storeNameSpan = document.createElement('span');
  //   storeNameSpan.className = 'fs-13 fw-bold';
  //   storeNameSpan.textContent = data.store_name;

  //   const titleSpan = document.createElement('span');
  //   titleSpan.className = 'lh-sm my-1';
  //   titleSpan.style.cssText = 'display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 1; overflow: hidden;';
  //   titleSpan.textContent = data.title;

  //   const priceDiv = document.createElement('div');
  //   priceDiv.className = 'd-inline-flex gap-1 my-1';
  //   if (data.discount_rate != null) {
  //       const discountSpan = document.createElement('span');
  //       discountSpan.className = 'fw-bold main-text-strong-color';
  //       discountSpan.textContent = `${data.discount_rate}%`;

  //       const priceSpan = document.createElement('span');
  //       priceSpan.className = 'fw-bold letter-spacing-05';
  //       priceSpan.textContent = `${formatPrice(data.rep_sale_price)}원`;

  //       priceDiv.appendChild(discountSpan);
  //       priceDiv.appendChild(priceSpan);
  //   } else {
  //       const priceSpan = document.createElement('span');
  //       priceSpan.className = 'fw-bold letter-spacing-05';
  //       priceSpan.textContent = `${formatPrice(data.rep_price)}원`;
  //       priceDiv.appendChild(priceSpan);
  //   }
    
  //   contentDiv.appendChild(storeNameSpan);
  //   contentDiv.appendChild(titleSpan);
  //   contentDiv.appendChild(priceDiv);

  //   // Keywords and ratings
  //   //if (data.keywords && data.keywords.length !== 0) {
  //       const keywordDiv = document.createElement('div');
  //       keywordDiv.className = 'd-flex fs-13 gap-1 mt-1 position-absolute';

  //       data.keywords.forEach((keyword) => {
  //           const keywordSpan = document.createElement('span');
  //           keywordSpan.className = 'badge rounded-0';
  //           keywordSpan.style.cssText = 'background: #f0f0f0; color: #9c9c9c;';
  //           keywordSpan.textContent = keyword;
  //           keywordDiv.appendChild(keywordSpan);
  //       });

  //       const ratingDiv = document.createElement('div');
  //       ratingDiv.className = 'fw-medium';
  //       ratingDiv.style.cssText = 'color: #99a1a8; font-size: 12px; letter-spacing: -0.5px;';

  //       if (data.avgRating != null) {
  //           const starSpan = document.createElement('span');
  //           starSpan.className = 'rating-color';
  //           starSpan.style.fontSize = '0.8em';
  //           starSpan.style.marginRight = '0.1em'
  //           starSpan.textContent = '★';

  //           const ratingSpan = document.createElement('span');
  //           ratingSpan.textContent = data.avgRating;

  //           ratingDiv.appendChild(starSpan);
  //           ratingDiv.appendChild(ratingSpan);
  //       }

  //       if (data.reviewCount && data.reviewCount != 0) {
  //           const reviewSpan = document.createElement('span');
  //           reviewSpan.textContent = `(${data.reviewCount})`;
  //           ratingDiv.appendChild(reviewSpan);
  //       }

  //       keywordDiv.appendChild(ratingDiv);
  //       contentDiv.appendChild(keywordDiv);
  //  // }

  //   flexColumnDiv.appendChild(imgContainer);
  //   flexColumnDiv.appendChild(contentDiv);
  //   productLink.appendChild(flexColumnDiv);
  //   productDiv.appendChild(productLink);

  //   container.insertAdjacentElement('afterbegin', productDiv);
  // });
  
 // innerHTML 쓰지말랭...
 
  products.forEach((data, i) => {
    const productHtml = `
      <div class="p-0 col-6 position-relative">
        <a href="/product?id=${data.product_id}">
          <div class="d-flex flex-column fs-14">
            <div class="position-relative overflow-hidden">
              <img style="width: 100%; height: 230px; object-fit: cover; object-position: top;" 
                  src="${data.image_url}" alt="${data.title}">
              <span class="position-absolute z-2 bottom-0 end-0" data-product-id="${data.product_id}" onclick="like(event)">
                <input value="favorite-button" name="favorite-checkbox" class="favorite" type="checkbox"
                ${likeData != null && likeData[i] != null ? 'checked' : ''} />
                <label class="favorite-container" for="favorite">
                  <svg class="feather feather-heart" stroke-linejoin="round" stroke-linecap="round" 
                      stroke-width="2" stroke="currentColor" fill="none" viewBox="0 0 24 24" 
                      height="24" width="24">
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                  </svg>
                </label>
              </span>
            </div>
            <div class="px-3 pt-2 pb-4 mb-3">
              <span class="fs-13 fw-bold">${data.store_name}</span>
              <span class="lh-sm my-1" style="display: -webkit-box; -webkit-box-orient: vertical; 
                    -webkit-line-clamp: 1; overflow: hidden;">${data.title}</span>
              <div class="d-inline-flex gap-1 my-1">
                ${data.discount_rate != null 
                  ? `<span class="fw-bold main-text-strong-color">${data.discount_rate}%</span>
                    <span class="fw-bold letter-spacing-05">${formatPrice(data.rep_sale_price)}원</span>`
                  : `<span class="fw-bold letter-spacing-05">${formatPrice(data.rep_price)}원</span>`}
              </div>
              <div class="d-flex fs-13 gap-1 mt-1 position-absolute">
                ${data.keywords.map(keyword => `
                  <span class="badge rounded-0" style="background: #f0f0f0; color: #9c9c9c;">${keyword}</span>
                `).join('')}
                <div class="fw-medium" style="color: #99a1a8; font-size: 12px; letter-spacing: -0.5px;">
                  ${data.avgRating != null ? `
                    <span class="rating-color" style="font-size: 0.8em;">★</span>
                    <span>${data.avgRating}</span>
                  ` : ''}
                  ${data.reviewCount && data.reviewCount != 0 ? `<span>(${data.reviewCount})</span>` : ''}
                </div>
              </div>
            </div>
          </div>
        </a>
      </div>
    `;

    container.insertAdjacentHTML('afterbegin', productHtml);
  });
  
}


// 찜 목록 페이지
async function removelike(event){
  // 이벤트 실행시, 이벤트에 등록된 함수가 실행되는데요, 이때 이 함수에 event 객체(이름은 어느것이든 좋습니다.)가 매개변수로 주어진다는 뜻입니다
  event.preventDefault();
  event.stopPropagation();

  let self = event.currentTarget;

  const result = await fetchCheckIsUserLike(self.dataset.productId);

  if (result.result == 'success') {
    const heart = self.querySelector('input');
    if (result.data.isLike) {
      heart.checked = true;
      heart.classList.add('like-on');
    } else {
      heart.checked = false;
      heart.classList.remove('like-on');
      // 요소 제거 !!
      const removeEl = self.parentElement.parentElement.parentElement.parentElement;
      removeEl.remove();
    }
  }
}
