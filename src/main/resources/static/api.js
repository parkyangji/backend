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