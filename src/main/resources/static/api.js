// const fetchMainPageData = async () => {
//   const response = await fetch('http://localhost:8888/api/board/mainPage', {
//       credentials: 'include',
//   });
//   if (!response.ok) throw new Error('Failed to fetch data');
//   return response.json();
//   };

// const result = await fetchMainPageData();
// console.log(result);

// fetch("/api/board/registerComment", {
//   method:"post",
//   headers : {
//     //"Content-Type" : 'application/json' // body를 json으로 
//    //"Content-Type" : 'multipart/form-data' // body에 new formdata 필요, ajax- 파일전송시
//     "Content-Type" : 'application/x-www-form-urlencoded' // 이거로 진행
//   },
//   body : `article_id=${articleId}&content=${inputText.value}`
// })
// .then(r=>r.json())
// .then(response => {
//   inputText.value='';
//   reloadCommentList();
// })

/*
JSON 데이터를 서버로 보낼 때 > 1. fetch 2. formData 3. 고전적인 form
단 fetch는 페이지로 반환은 불가능하고 JSON 형태로 반환받아야 하기 때문에
페이지 이동(리다이렉트)을 수동으로 처리해야 합니다
*/


const fetchCheckIsUserLike = async (productId) => {
  const response = await fetch('/api/favorite/isLike?id='+productId);
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchProductList = async (categoryId) => {
  let response;
  let category_params; 
  if (typeof(categoryId)==='object') {
    category_params = categoryId.map(id => `id=${id}`).join('&');
  } else {
    category_params = `id=${categoryId}`;
  }
  response = await fetch('/api/category?'+category_params);
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchBrandProductList = async (name , categoryId=null) => {
  const type = {
    name: name,
    categoryId: categoryId
  };

  const response = await fetch('/api/brand', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/json', // URL 인코딩된 데이터 전송 시 필요
    },
    body : JSON.stringify(type) // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchKeywordFromCategory = async (keyword, menu_id, categoryId) => {
  const type = {
    keyword: keyword,
    menu_id: menu_id, // 부모 카테고리
    categoryId: categoryId // 서브 카테고리 => null이면 부모 카테고리 기준으로!
  };
  
  const response = await fetch('/api/keywordFilter', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/json', // URL 인코딩된 데이터 전송 시 필요
    },
    body : JSON.stringify(type) // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchPreviewProductId = async (productId) => {
  const response = await fetch('/api/preview?id='+productId);
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
} 

const fetchGetProductOption = async (productId) => {
  const response = await fetch('/api/product/option?id='+productId);
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchPostChoiceOption = async (data) => {
  const response = await fetch('/api/product/cart', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/json', // URL 인코딩된 데이터 전송 시 필요
    },
    body : JSON.stringify(data) // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchDeleteCartItem = async (combinationId) => {
  const response = await fetch('/api/product/cart/delete', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body : `id=${combinationId}` // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchUpdateQuantity = async (requestData) => {
  const response = await fetch("/api/product/updateCartQuantity", {
    method: "POST",
    headers: {
        "Content-Type": "application/json",
    },
    body: JSON.stringify(requestData),
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}


const fetchPurchaseProcess = async (data) => { // 상품 정보
  const response = await fetch('/api/product/purchase', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/json', // URL 인코딩된 데이터 전송 시 필요
    },
    body : JSON.stringify(data) // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchPaymentProcess = async (data) => { // 배송에 대한 정보
  const response = await fetch('/api/product/payment', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/json', // URL 인코딩된 데이터 전송 시 필요
    },
    body : JSON.stringify(data) // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}


const fetchProductDetailImages = async (productId) => {
  const response = await fetch('/api/product/detailImages', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body : `id=${productId}` // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchProductReview = async (productId) => {
  const response = await fetch('/api/product/review', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body : `id=${productId}` // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchProductQuestion = async (productId) => {
  const response = await fetch('/api/product/question', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body : `id=${productId}` // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}

const fetchStatisticsOthersBestTop5 = async (productId) => {
  const response = await fetch('/api/statistics/othersBestTop5', {
    method : 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body : `id=${productId}` // 직렬화 시켜 보내기
  });
  if (!response.ok) throw new Error('Failed to fetch data');
  return response.json();
}