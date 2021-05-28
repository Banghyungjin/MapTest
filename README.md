# MapTest

## 설명

### <li> 안드로이드 스튜디오를 사용하여 지도 어플 만들기

## 사용한 API

### <li> 네이버 지도 API - Mobile Dynamic Map,  https://www.ncloud.com/product/applicationService/maps

## 진행상황

### <li> 2021-05-23
1. 제작 시작 
2. API 선택 
3. 초기화면 액티비티 제작
4. 네이버 맵 API를 사용해 기본 네이버맵 액티비티 제작 

### <li> 2021-05-24
1. json 파일형태의 geojson파일 읽어오는 기능 구현
2. 읽어온 geojson파일에서 지역 이름과 좌표 추출 구현
3. 좌표를 기반으로 네이버맵 폴리곤 객체 생성 (한 개의 폴리곤에 모든 좌표가 들어가 정상적인 폴리곤 출력이 되지 않음)

### <li> 2021-05-25
1. 대한민국 행정구역경계의 shp파일 다운로드 (광역시도, 시군구, 읍면동 3개)
2. shp파일을 구성요소별로 분리하여 복수의 shp파일로 분할 (현재는 광역시도만 적용)
3. 분할된 shp파일 단순화 (용량 감소)
4. shp파일을 json파일로 변환
5. 폴리곤 리스트를 통해 json파일의 좌표에 대응하는 정상적인 형태의 폴리곤 출력 기능 구현 (google의 geocoding으로는 세종특별자치시 인식 불가)

### <li> 2021-05-26
1. 다른 나라의 지역 선택 시 앱이 종료되는 부분 수정 (육지가 아닌 바다의 경우는 아직 작업 중)
2. 다른 나라의 지역 선택 시 앱이 종료되는 부분 수정 (바다의 경우도 수정, 일부 연안의 경우 오류 계속 남)
3. 초기화면 레이아웃 수정 
4. 2번 사항에서 일부 연안 선택시 앱이 종료되는 부분 수정

### <li> 2021-05-28
1. 지역 선택시 해당되는 좌표의 지역이름을 가져오는 것을 google의 geocoding에서 네이버의 reverseGeocoding api를 사용하는 것으로 구현
2. Retrofit을 사용해서 HTTP 통신으로 네이버 reverseGeocoding api 사용 기능 구현 (세종특별자치시 제대로 인식)

## 앱 화면 
<p><img src="https://user-images.githubusercontent.com/37135305/119787195-ee63de80-bf0b-11eb-89ec-0f59f54b8ec2.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778952-1438b580-bf03-11eb-8483-2f586164a1ac.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778962-1733a600-bf03-11eb-87fd-4d7597e6e730.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778971-18fd6980-bf03-11eb-841b-2cc1ff2a62f7.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778977-1b5fc380-bf03-11eb-9e51-ece13e2b07a5.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778989-1d298700-bf03-11eb-86bc-07142924b8b0.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119779001-20247780-bf03-11eb-9d2c-b48c121d3086.jpg" width="15%"></p>
