# MapTest

## 설명

### <li> 안드로이드 스튜디오를 사용하여 지도 어플 만들기
### <li> 실행은 repository root directory의 MapTest.apk 파일을 다운받아서 설치 후 사용

## 사용한 API

### <li> 네이버 지도 API - Mobile Dynamic Map, Reverse Geocoding https://www.ncloud.com/product/applicationService/maps

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

### <li> 2021-06-01
1. 구역 경계 좌표 정보를 이전에 매번 새로 해당하는 구역의 json파일을 열고 가져오는 것에서 하나로 통합된 json 파일 하나에서 액티비티 시작 시 전부 가져와 배열로 저장 후 사용 
2. showAll 버튼 추가 (버튼 클릭 시 모든 광역시도 구역을 한번에 보여줌)
3. 처음 화면에서 뒤로가기 클릭 시 앱이 바로 종료되지 않고 alertdialog를 띄워 종료를 재확인하도록 수정
4. 모바일 웹화면 레이아웃 수정

### <li> 2021-06-02
1. 공공데이터 API 코로나 관련 데이터 활용
2. 코로나 관련 데이터를 각 광역시도에 맞게 맵에 올리는 기능 추가 (확진자, 확진자 증가량, 격리자, 사망자)
3. 맵 프래그먼트와 툴바가 서로 겹치는 현상 수정 (리니어 레이아웃 사용)
4. 전국 코로나 데이터 표시 기능 추가

### <li> 2021-06-08
1. 공공데이터 API 연결 오류 수정 (인증키 관련)
2. APK 파일 제작

## 앱 화면 
<p><img src="https://user-images.githubusercontent.com/37135305/119787195-ee63de80-bf0b-11eb-89ec-0f59f54b8ec2.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778952-1438b580-bf03-11eb-8483-2f586164a1ac.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778962-1733a600-bf03-11eb-87fd-4d7597e6e730.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778971-18fd6980-bf03-11eb-841b-2cc1ff2a62f7.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778977-1b5fc380-bf03-11eb-9e51-ece13e2b07a5.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119778989-1d298700-bf03-11eb-86bc-07142924b8b0.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/119779001-20247780-bf03-11eb-9d2c-b48c121d3086.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/120275243-fe086c00-c2eb-11eb-8671-46eced56b2b3.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/120439341-5f4c4000-c3bd-11eb-816d-240e0b74435e.jpg" width="15%">
<img src="https://user-images.githubusercontent.com/37135305/120439377-6b380200-c3bd-11eb-923e-f1b1e2a4b62c.jpg" width="15%"></p>

## 소스코드 보기
https://github.com/Banghyungjin/MapTest/tree/main/app/src/main/java/com/example/maptest
