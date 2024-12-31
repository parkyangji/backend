const subMenus = document.getElementById('subMenu').querySelectorAll('li');

subMenus.forEach((menu, i)=> {
  menu.addEventListener('click', function(e){
    e.preventDefault();
    e.stopPropagation();

    subMenus.forEach((el)=>el.classList.remove('product-sub-menu-active'));
    this.classList.add('product-sub-menu-active');

    const detailContainer = document.getElementById('subContainer');

    detailContainer.innerHTML = ``;

    const urlParams = new URLSearchParams(window.location.search);
    const productId = parseInt(urlParams.get('id'));  

    // product.html
    if (this === document.getElementById('productDetailImages')) {
      productDetailImages(detailContainer, productId);
    }
    if (this === document.getElementById('productReview')) {
      productReview(detailContainer, productId);
    }
    if (this === document.getElementById('productQuestion')) {
      productQuestion(detailContainer, productId);
    }

    // myReview.html
    if (this === document.getElementById('writeReview')) {
      writeReview(detailContainer)
    }
    if (this === document.getElementById('writtenReview')) {
      writtenReview(detailContainer)
    }
    
  })
})

async function writeReview(container) {
  console.log('writeReview')

  const result = await fetchReviewData("write");

  console.log(result.data.writeProducts)

  history.replaceState(null, '', result.data.redirectUrl);

  result.data.writeProducts.forEach((order) => {
    order.products.forEach((product) => {
      product.options.forEach((item) => {

        // item.option은 배열이므로 반복
        const optionDetails = item.option.map((map) => {
          const key = Object.keys(map)[0]; // 키 추출 (예: "사이즈")
          const value = map[key]; // 값 추출 (예: "Free")
          return `${value}`;
        }).join(' / '); // 옵션들을 " / "로 구분

        const stars = Array(5).fill().map((_, i) => { // _는 사용하지 않는 변수임을 나타내는 관례적인 이름.
          return i < item.rating ? '<span class="rating-color">★</span>' : '<span>★</span>'
        }).join('');
  
        // HTML 생성
        const Html = `
          <div class="m-3">
            <div class="fs-14 m-0 row">
              <div class="col-3 me-2 p-0"><img class="img-fluid" src="${product.image_url}" alt=""></div>
              <div class="col p-0">
                <p class="mb-1">${product.title}</p>
                <div class="main-text-sub-color">
                  <span>${optionDetails}</span>
                </div>
              </div>
            </div>
            ${ item.rating != null ? 
             `<div class="bg-light border-top my-3 p-2 pt-2 position-relative">
                <div class="d-flex align-items-center">
                    <div class="rating">
                        ${stars}
                    </div>
                </div>
              </div>` 
            : 
              `<form action="/ratingProcess" method="POST">
                  <input type="hidden" name="order_detail_id" value="${item.order_detail_id}">
                  <div class="d-flex fs-3 gap-2 p-2">
                      <input type="submit" name="rating" value="1" id="star1-${item.order_detail_id}" class="d-none ratingValue">
                      <label for="star1-${item.order_detail_id}" class="star">☆</label>
                      <input type="submit" name="rating" value="2" id="star2-${item.order_detail_id}" class="d-none ratingValue">
                      <label for="star2-${item.order_detail_id}" class="star">☆</label>
                      <input type="submit" name="rating" value="3" id="star3-${item.order_detail_id}" class="d-none ratingValue">
                      <label for="star3-${item.order_detail_id}" class="star">☆</label>
                      <input type="submit" name="rating" value="4" id="star4-${item.order_detail_id}" class="d-none ratingValue">
                      <label for="star4-${item.order_detail_id}" class="star">☆</label>
                      <input type="submit" name="rating" value="5" id="star5-${item.order_detail_id}" class="d-none ratingValue">
                      <label for="star5-${item.order_detail_id}" class="star">☆</label>
                  </div>
                  <!-- <button type="submit" class="btn btn-dark w-100">별점 제출</button> -->
                </form>`
              }
            ${ item.rating != null && item.review_content == null ?
            `<form action="/reviewContentProcess" method="POST">
                  <input type="hidden" name="order_detail_id" value="${item.order_detail_id}">
                  <textarea class="form-control mb-2" name="review_content" placeholder="리뷰를 입력하세요"></textarea>
                  <button type="submit" class="border-0 btn btn-primary main-bg-strong-color w-100">리뷰 작성</button>
              </form>`  
            : ``}
          </div>
        `;
  
        // HTML 추가
        container.insertAdjacentHTML('beforeend', Html);
      });
    });
  });
}
async function writtenReview(container) {
  console.log('writtenReview')

  const result = await fetchReviewData("written");
  console.log(result.data.writtenProducts)

  history.replaceState(null, '', result.data.redirectUrl);

  result.data.writtenProducts.forEach((order) => {
    order.products.forEach((product) => {
      product.options.forEach((item) => {

        // item.option은 배열이므로 반복
        const optionDetails = item.option.map((map) => {
          const key = Object.keys(map)[0]; // 키 추출 (예: "사이즈")
          const value = map[key]; // 값 추출 (예: "Free")
          return `${value}`;
        }).join(' / '); // 옵션들을 " / "로 구분

        const stars = Array(5).fill().map((_, i) => { // _는 사용하지 않는 변수임을 나타내는 관례적인 이름.
          return i < item.rating ? '<span class="rating-color">★</span>' : '<span>★</span>'
        }).join('');
  
        // HTML 생성
        const Html = `
          <div class="m-3">
            <div class="fs-14 m-0 row">
              <div class="col-3 me-2 p-0"><img class="img-fluid" src="${product.image_url}" alt=""></div>
              <div class="col p-0">
                <p class="mb-1">${product.title}</p>
                <div class="main-text-sub-color">
                  <span>${optionDetails}</span>
                </div>
              </div>
            </div>
            <div class="bg-light border-top mt-3 p-2 pt-2 position-relative">
              <div class="d-flex align-items-center">
                  <div class="rating">
                      ${stars}
                  </div>
              </div>
              <div class="d-flex align-items-center">
                <p class="mx-1 my-3">${item.review_content}</p>
              </div>
              <span class="m-1 bottom-0 end-0 fs-14 position-absolute text-secondary">${formatOrderDate(item.review_created)}</span>
            </div>
          </div>
        `;
  
        // HTML 추가
        container.insertAdjacentHTML('beforeend', Html);
      });
    });
  });


}

async function productDetailImages(container, productId){
  console.log('productDetailImages')

  const result = await fetchProductDetailImages(productId);
  // console.log(result.data.detailImages)

  result.data.detailImages.forEach(img => {
    const imgHtml = `<img class="img-fluid" src="${img}" alt="상세 이미지">`
    container.insertAdjacentHTML('beforeend', imgHtml);
  })
}

async function productReview(container, productId) {
  console.log('productReview')

  const result = await fetchProductReview(productId);
  console.log(result.data.reviewData)

  result.data.reviewData.forEach(review => {
    if (result.data.reviewData.length == 0) return;

    const reviewHtml = `
    <div>
      <div class="align-items-center d-flex gap-2">
        <i class="bi bi-person-circle fs-1" style="color: #eaeaea;"></i>
        <span class="fs-14">${review.reviewer}</span>
      </div>
      <p class="mb-2">
        <span class="rating-color">★ </span>
        ${review.rating.rating!=null ? `<span class="fs-13 fw-bold"> ${review.rating.rating}</span>` : ''}
        <span style="font-size: 12px; color: #c0c0c0;">${review.review.created_date}</span>
      </p>
      ${review.review.review_content!=null ? `<p class="mx-1 my-3">${review.review.review_content}</p>` : ''} 
      ${review.reply==null ? `<p class="fs-13 m-0 p-3 text-secondary" style="background: #fafafb;">판매자가 아직 답글을 달지 않았습니다</p>` : ''} 
      <div class="position-relative">
        ${review.reply!=null ? `<p class="fs-13 m-0 p-3 text-secondary" style="background: #fafafb;">${review.reply.seller_reply}</p>` : ''} 
        ${review.reply!=null ? `<span class="position-absolute bottom-0 end-0" style="font-size: 12px; color: #c0c0c0;">${review.reply.created_date}</span>` : ''}
      </div>
    </div>
    `
    container.insertAdjacentHTML('beforeend', reviewHtml);
  })
}

async function productQuestion(container, productId){
  console.log('productQuestion')

  // const result = await fetchProductQuestion(productId);
  // console.log(result.data.questionData)
  const questionHtml = `
    <div>
      <div class="d-flex align-items-center main-text-sub-color">
        <p class="m-0">비밀글입니다.</p>
        <svg xmlns="http://www.w3.org/2000/svg" x="0px" y="0px" width="18" height="18" viewBox="0 0 24 24">
        <path d="M 12 1 C 8.6761905 1 6 3.6761905 6 7 L 6 8 C 4.9 8 4 8.9 4 10 L 4 20 C 4 21.1 4.9 22 6 22 L 18 22 C 19.1 22 20 21.1 20 20 L 20 10 C 20 8.9 19.1 8 18 8 L 18 7 C 18 3.6761905 15.32381 1 12 1 z M 12 3 C 14.27619 3 16 4.7238095 16 7 L 16 8 L 8 8 L 8 7 C 8 4.7238095 9.7238095 3 12 3 z M 12 13 C 13.1 13 14 13.9 14 15 C 14 16.1 13.1 17 12 17 C 10.9 17 10 16.1 10 15 C 10 13.9 10.9 13 12 13 z"></path>
        </svg>
      </div>
      <div class="d-flex gap-1 fs-14 text-secondary" >
        <span>답변대기</span>
        <span>d**</span>
        <span>2024.11.11</span>
      </div>
    </div>`;
  container.insertAdjacentHTML('beforeend', questionHtml);
}
