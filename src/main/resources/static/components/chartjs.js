let existingChart = null;
function chartset(ratingData){
  // 차트 렌더링
  const ctx = document.getElementById('myChart').getContext('2d');
    // 기존 차트가 있으면 파괴
    if (existingChart) {
      existingChart.destroy();
  }

  // 차트 데이터 변환
  const labels = ratingData.map(data => `${data.rating}점`);
  const percentages = ratingData.map(data => data.star_percentage);

  existingChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
          labels: labels,
          datasets: [{
              label: '별점 분포',
              data: percentages,
              backgroundColor: [
                  'rgba(255, 99, 132, 0.6)', // 1점
                  'rgba(54, 162, 235, 0.6)', // 2점
                  'rgba(255, 206, 86, 0.6)', // 3점
                  'rgba(75, 192, 192, 0.6)', // 4점
                  'rgba(153, 102, 255, 0.6)'  // 5점
              ],
              borderColor: [
                  'rgba(255, 99, 132, 1)',
                  'rgba(54, 162, 235, 1)',
                  'rgba(255, 206, 86, 1)',
                  'rgba(75, 192, 192, 1)',
                  'rgba(153, 102, 255, 1)'
              ],
              borderWidth: 1
          }]
      },
      options: {
          responsive: true,
          plugins: {
            title: {
              display: true, // 제목 표시 여부
              text: '상품 별점 분포', // 제목 텍스트
              color: '#333', // 제목 텍스트 색상
              font: {
                  size: 16, // 폰트 크기
                  weight: 'bold', // 폰트 두께
              },
              padding: {
                  top: 10, // 상단 여백
                  bottom: 20 // 하단 여백
              },
              align: 'center' // 제목 정렬 ('start', 'center', 'end')
              },
              legend: {
                  position: 'top'
              },
              tooltip: {
                  callbacks: {
                      label: function (tooltipItem) {
                          // 툴팁에 추가 정보 표시
                          return `${tooltipItem.label}: ${tooltipItem.raw}%`;
                      }
                  }
              }
          }
      }
  });

  Chart.register({
    id: 'emptyDataPlugin',
    beforeDraw(chart) {
        const { datasets } = chart.data;
        const hasNonZeroData = datasets[0].data.some(value => value > 0);

        if (!hasNonZeroData) {
            const ctx = chart.ctx;
            const width = chart.width;
            const height = chart.height;

            ctx.save();
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.font = '16px Arial';
            ctx.fillStyle = '#666';
            ctx.fillText('표시할 평점 데이터가 없습니다', width / 2, height / 2);
            ctx.restore();
        }
    }
});

}