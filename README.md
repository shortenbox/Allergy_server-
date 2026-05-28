# Allergy Server

OCR 기반 식단/음식 이미지 분석 서버입니다. 이미지 또는 텍스트에서 음식명을 추출하고, 식약처 조리식품 API와 로컬 MySQL 알러지 데이터를 이용해 영양 정보, 알러지 위험, 재료, 조리 순서, 저감 조리법 TIP을 반환합니다.

## 주요 기능

- 텍스트 기반 식단 분석
- 식단표 이미지 OCR 분석
- 메뉴판/제품명 이미지 OCR 분석
- 음식별 영양 정보 조회
- 음식별 재료 추출
- 알러지 위험 분석
- 조리 순서 `recipeSteps` 조회
- 저감 조리법 TIP `recipeTip` 조회
- 이미지 분석 기록 저장 및 조회
- 날짜별 주간 식단표 저장 및 조회

## 기술 스택

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- MySQL
- Clova OCR
- Google Vision OCR
- 식약처 COOKRCP01 API

## 프로젝트 구조

```text
src/main/java/com/example/allergy_server
├─ controller
│  ├─ MealController.java
│  ├─ OcrController.java
│  ├─ FoodImageController.java
│  ├─ MealHistoryController.java
│  └─ WeeklyMealPlanController.java
├─ entity
│  ├─ MealHistory.java
│  └─ WeeklyMealPlan.java
├─ repository
│  ├─ MealHistoryRepository.java
│  └─ WeeklyMealPlanRepository.java
├─ service
│  ├─ MealService.java
│  ├─ FoodApiService.java
│  ├─ CheckAllergyService.java
│  ├─ IngredientAliasService.java
│  ├─ MealHistoryService.java
│  └─ WeeklyMealPlanService.java
├─ parser
│  └─ MealTextParser.java
└─ external_ocr
   ├─ OcrPicture.java
   └─ ClovaOcrClient.java
```

## 실행 전 준비

### 1. MySQL DB 생성 및 SQL 실행

대상 DB:

```text
infant_meal_db
```

기존 SQL 파일 위치:

```text
C:\Users\손홍재\OneDrive\문서\Database.sql
C:\Users\손홍재\OneDrive\문서\AL_alysis.sql
C:\Users\손홍재\OneDrive\문서\Food_di.sql
C:\Users\손홍재\OneDrive\문서\Mealmage.sql
```

프로젝트 내부 추가 SQL:

```text
src/main/resources/sql/create_meal_history.sql
src/main/resources/sql/create_weekly_meal_plan.sql
```

권장 실행 순서:

```text
1. Database.sql
2. AL_alysis.sql
3. Food_di.sql
4. Mealmage.sql
```

`Mealmage.sql`에는 이미지 분석 기록용 `meal_history`와 주간 식단표용 `weekly_meal_plan` 생성 SQL이 포함되어 있습니다.

### 2. application.properties 설정

파일 위치:

```text
src/main/resources/application.properties
```

GitHub에는 실제 API 키와 DB 비밀번호가 들어간 `application.properties`를 올리지 않습니다. 새 환경에서는 아래 예시 파일을 복사해서 값을 채워 사용합니다.

```text
src/main/resources/application-example.properties
```

현재 주요 설정:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/infant_meal_db?serverTimezone=Asia/Seoul&useSSL=false
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

clova.ocr.url=...
clova.ocr.secret=...

server.port=8080
```

주의:

- `ddl-auto=validate`이므로 필요한 테이블이 DB에 먼저 생성되어 있어야 서버가 정상 실행됩니다.
- API 키, OCR secret, DB password는 제출/배포 시 환경변수 또는 별도 로컬 설정 파일로 분리하는 것이 좋습니다.

## 실행 방법

검증된 실행 방식:

```powershell
.\mvnw.cmd clean compile
.\mvnw.cmd -DskipTests package
java -jar target\Allergy_Server-0.0.1-SNAPSHOT.jar
```

서버 주소:

```text
http://localhost:8080
```

참고:

- 현재 환경에서는 `.\mvnw.cmd spring-boot:run` 실행 시 메인 클래스를 찾지 못하는 문제가 발생한 적이 있어, jar 실행 방식을 권장합니다.

## 전체 처리 흐름

### 텍스트 분석

```text
JSON 식단 입력
→ MealController
→ MealService.analyzeMeal()
→ 음식명 표준화
→ 식약처 API 조회
→ 재료/조리법/TIP 추출
→ 알러지 위험 분석
→ JSON 반환
```

### 이미지 분석

```text
이미지 업로드
→ OCR
→ 식단표 여부 판단
→ 식단표면 아침/점심/저녁/간식 분리
→ 단일 메뉴면 음식명 추출
→ 음식 분석
→ meal_history 저장
→ JSON 반환
```

### 주간 식단표 저장

```text
날짜 + 아침/점심/저녁/간식 입력
→ 식사별 분석
→ weekly_meal_plan 저장
→ 날짜/기간 기준 조회
```

## API 문서

상세 요청/응답 예시는 [docs/API_SPEC.md](docs/API_SPEC.md)를 참고하세요.

프론트 전달용 요약은 [docs/FRONTEND_HANDOFF.md](docs/FRONTEND_HANDOFF.md)를 참고하세요.

발표/제출용 백엔드 요약은 [docs/BACKEND_SUMMARY.md](docs/BACKEND_SUMMARY.md)를 참고하세요.

추후 확장 기능 메모는 [docs/FUTURE_WORK.md](docs/FUTURE_WORK.md)를 참고하세요.

## 주요 DB 테이블

| 테이블 | 역할 |
| --- | --- |
| `allergen_in` | 알러지 위험 기준 음식/재료 |
| `ingredient_alias` | OCR/입력 음식명 표준화 |
| `allergy_category` | 알러지 카테고리 |
| `food_dictionary` | 음식 검색명 매핑 |
| `meal_history` | 이미지 분석 기록 저장 |
| `weekly_meal_plan` | 날짜별 주간 식단표 저장 |

## 한계점 및 개선점

- 현재 일반 음식 이미지는 OCR 기반입니다. 텍스트가 없는 음식 사진은 인식 실패 가능성이 큽니다.
- 제품 패키지 이미지도 로고형 글자, 곡면, 저화질일 경우 OCR 실패 가능성이 있습니다.
- 식약처 API 검색 결과가 음식명과 정확히 매칭되지 않을 수 있어 fallback 검색어와 alias 데이터 보강이 필요합니다.
- 캐시는 현재 메모리 기반이라 서버 재시작 시 초기화됩니다.
- API timeout, 캐시 만료 정책, 에러 코드 세분화는 추가 개선 대상입니다.
- 분석 기록/주간 식단표는 현재 사용자 구분 없이 저장됩니다. 사용자별 기록은 로그인/JWT 설계 이후 확장하는 것이 좋습니다.
