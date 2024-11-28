const exampleModalEl = document.getElementById('exampleModal');
const tempModalInstance = bootstrap.Modal.getOrCreateInstance(exampleModalEl);
// console.log("부트스트랩 모달" + tempModalInstance)

async function tempModal(event) {
  event.preventDefault();
  event.stopPropagation();

  let self = event.currentTarget;
  let productId = self.dataset.productId;

  const previewPopupEl = document.getElementById('previewPopup');
  previewPopupEl.classList.remove('d-none');

  tempModalInstance.show();

  // ajax !!!
  const result = await fetchPreviewProductId(productId);

  /*
  document.getElementById('preview-content').innerHTML = `
  <div class="thumbnail-images">
    <img class="img-fluid" src="${result.data.product.detailsImages.thumbnail[0]}" alt="상품 썸네일">
  </div>
  <!-- 제품설명 -->
  <div class="container-sm p-4">
    <!-- 브랜드명 -->
    <div>
      <a href="/brand?name=${result.data.product.store_name}" class="d-flex align-items-center mb-2 pb-2 main-text-sub-color">
        <span>${result.data.product.store_name}</span>
        <i class="bi bi-chevron-right" style="font-size: 13px; margin-left: 3px;"></i>
      </a>
    </div>
    <!-- 상품명 -->
    <div>
      <p class="fw-semibold fs-1-5 mb-1">${result.data.product.title}</p>
    </div>
    <!-- 평점/리뷰 -->
    ${result.data.product.avgRating !== null ? `
      <div>
        <p class="m-0">
          <span class="rating-color">★ </span><span class="me-1" style="font-size: 14px;">${result.data.product.avgRating}</span>
          ${result.data.product.reviewCount !== 0 ? `
            <span class="text-decoration-underline" style="font-size: 13px;">리뷰 ${result.data.product.reviewCount}개</span>
          ` : ''}
        </p>
      </div>
    ` : ''}
    <!-- 가격 -->
    <div>
      ${result.data.product.discount_rate !== null ? `
        <div class="mt-3 d-flex flex-column">
          <span class="rep_price text-decoration-line-through text-secondary">${result.data.product.rep_price.toLocaleString()}원</span>
          <p class="d-flex gap-2 m-0">
            <span id="discount_rate" class="fw-bold fs-5 main-text-strong-color">${result.data.product.discount_rate}%</span>
            <span id="rep_sale_price" class="fw-bold fs-5 letter-spacing-05">${result.data.product.rep_sale_price.toLocaleString()}원</span>
          </p>
        </div>
      ` : `
        <p class="mt-3">
          <span class="rep_price fw-bold fs-5">${result.data.product.rep_price.toLocaleString()}원</span>
        </p>
      `}
    </div>
  </div>
  <!-- 차트 -->
  <div>
    <canvas id="myChart"></canvas>
  </div>
  `;
  */
  // 부모 요소 가져오기
  const previewContent = document.getElementById('preview-content');

  // 초기화
  previewContent.innerHTML = '';

  // 썸네일 이미지
  const thumbnailDiv = document.createElement('div');
  thumbnailDiv.className = 'thumbnail-images';
  const thumbnailImg = document.createElement('img');
  thumbnailImg.className = 'img-fluid';
  thumbnailImg.src = result.data.product.detailsImages.thumbnail[0];
  thumbnailImg.alt = '상품 썸네일';
  thumbnailDiv.appendChild(thumbnailImg);
  previewContent.appendChild(thumbnailDiv);

  // 제품 설명 컨테이너
  const containerDiv = document.createElement('div');
  containerDiv.className = 'container-sm p-4';

  // 브랜드명
  const brandDiv = document.createElement('div');
  const brandLink = document.createElement('a');
  brandLink.href = `/brand?name=${encodeURIComponent(result.data.product.store_name)}`;
  brandLink.className = 'd-flex align-items-center mb-2 pb-2 main-text-sub-color';
  const brandSpan = document.createElement('span');
  brandSpan.textContent = result.data.product.store_name;
  const brandIcon = document.createElement('i');
  brandIcon.className = 'bi bi-chevron-right';
  brandIcon.style.fontSize = '13px';
  brandIcon.style.marginLeft = '3px';

  brandLink.appendChild(brandSpan);
  brandLink.appendChild(brandIcon);
  brandDiv.appendChild(brandLink);
  containerDiv.appendChild(brandDiv);

  // 상품명
  const titleDiv = document.createElement('div');
  const titleP = document.createElement('p');
  titleP.className = 'fw-semibold fs-1-5 mb-1';
  titleP.textContent = result.data.product.title;
  titleDiv.appendChild(titleP);
  containerDiv.appendChild(titleDiv);

  // 평점/리뷰
  if (result.data.product.avgRating !== null) {
    const ratingDiv = document.createElement('div');
    const ratingP = document.createElement('p');
    ratingP.className = 'm-0';

    const ratingStarSpan = document.createElement('span');
    ratingStarSpan.className = 'rating-color';
    ratingStarSpan.textContent = '★ ';
    const ratingValueSpan = document.createElement('span');
    ratingValueSpan.className = 'me-1';
    ratingValueSpan.style.fontSize = '14px';
    ratingValueSpan.textContent = result.data.product.avgRating;

    ratingP.appendChild(ratingStarSpan);
    ratingP.appendChild(ratingValueSpan);

    if (result.data.product.reviewCount !== 0) {
      const reviewSpan = document.createElement('span');
      reviewSpan.className = 'text-decoration-underline';
      reviewSpan.style.fontSize = '13px';
      reviewSpan.textContent = `리뷰 ${result.data.product.reviewCount}개`;
      ratingP.appendChild(reviewSpan);
    }

    ratingDiv.appendChild(ratingP);
    containerDiv.appendChild(ratingDiv);
  }

  // 가격
  const priceDiv = document.createElement('div');
  if (result.data.product.discount_rate !== null) {
    const discountDiv = document.createElement('div');
    discountDiv.className = 'mt-3 d-flex flex-column';

    const originalPriceSpan = document.createElement('span');
    originalPriceSpan.className = 'rep_price text-decoration-line-through text-secondary';
    originalPriceSpan.textContent = `${result.data.product.rep_price.toLocaleString()}원`;

    const discountP = document.createElement('p');
    discountP.className = 'd-flex gap-2 m-0';

    const discountRateSpan = document.createElement('span');
    discountRateSpan.id = 'discount_rate';
    discountRateSpan.className = 'fw-bold fs-5 main-text-strong-color';
    discountRateSpan.textContent = `${result.data.product.discount_rate}%`;

    const salePriceSpan = document.createElement('span');
    salePriceSpan.id = 'rep_sale_price';
    salePriceSpan.className = 'fw-bold fs-5 letter-spacing-05';
    salePriceSpan.textContent = `${result.data.product.rep_sale_price.toLocaleString()}원`;

    discountP.appendChild(discountRateSpan);
    discountP.appendChild(salePriceSpan);
    discountDiv.appendChild(originalPriceSpan);
    discountDiv.appendChild(discountP);

    priceDiv.appendChild(discountDiv);
  } else {
    const priceP = document.createElement('p');
    priceP.className = 'mt-3';

    const priceSpan = document.createElement('span');
    priceSpan.className = 'rep_price fw-bold fs-5';
    priceSpan.textContent = `${result.data.product.rep_price.toLocaleString()}원`;

    priceP.appendChild(priceSpan);
    priceDiv.appendChild(priceP);
  }

  containerDiv.appendChild(priceDiv);

  // 컨테이너 추가
  previewContent.appendChild(containerDiv);

  // 차트 캔버스
  const chartDiv = document.createElement('div');
  const canvas = document.createElement('canvas');
  canvas.id = 'myChart';
  chartDiv.appendChild(canvas);
  previewContent.appendChild(chartDiv);

  chartset(result.data.ratingData);

}

exampleModalEl.addEventListener('hidden.bs.modal', function (event) {
  // do something...

  const previewPopupEl = document.getElementById('previewPopup');
  previewPopupEl.classList.add('d-none');
})

