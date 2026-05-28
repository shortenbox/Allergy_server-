# API Specification

Base URL:

```text
http://localhost:8080
```

## API 목록

| Method | Endpoint | 설명 |
| --- | --- | --- |
| `POST` | `/api/meal/analyze/text` | 텍스트 기반 식단 분석 |
| `POST` | `/api/meal/analyze/image` | 식단표/메뉴 이미지 분석 |
| `POST` | `/api/food/analyze/image` | 일반 음식 이미지 분석 |
| `GET` | `/api/meal/history` | 최근 이미지 분석 기록 조회 |
| `GET` | `/api/meal/history/{id}` | 이미지 분석 기록 상세 조회 |
| `POST` | `/api/meal/weekly` | 날짜별 식단표 저장 |
| `GET` | `/api/meal/weekly` | 기간별 식단표 조회 |
| `GET` | `/api/meal/weekly/{id}` | 식단표 ID 상세 조회 |
| `GET` | `/api/meal/weekly/date/{date}` | 날짜별 식단표 조회 |

## 공통 분석 응답 필드

음식 분석 결과에는 보통 아래 필드가 포함됩니다.

```json
{
  "foodName": "오트밀",
  "carbohydrate": "51.9",
  "protein": "11.9",
  "fat": "7",
  "sodium": "53.4",
  "risk": "위험",
  "ingredients": ["귀리", "양파"],
  "hasIngredients": true,
  "recipeText": "귀리, 쌀, 양파",
  "recipeSteps": ["1. 재료를 준비한다.", "2. 끓인다."],
  "recipeTip": "나트륨을 줄이는 조리 TIP",
  "hasRecipe": true
}
```

데이터가 없으면 아래처럼 빈 배열/빈 문자열이 유지됩니다.

```json
{
  "ingredients": [],
  "hasIngredients": false,
  "recipeSteps": [],
  "recipeTip": "",
  "hasRecipe": false
}
```

## 1. 텍스트 식단 분석

```text
POST /api/meal/analyze/text
Content-Type: application/json
```

요청 예시:

```json
{
  "breakfast": "오트밀",
  "lunch": "묵은지 김치찌개",
  "dinner": "그릭요거트",
  "snack": "바나나"
}
```

응답 예시:

```json
{
  "breakfast": {
    "foodName": "오트밀",
    "risk": "위험",
    "ingredients": ["귀리", "양파"],
    "hasIngredients": true,
    "recipeSteps": ["1. 귀리는 깨끗이 씻는다."],
    "recipeTip": "귀리와 달걀을 넣어 고소한 맛을 더했다.",
    "hasRecipe": true
  },
  "lunch": {
    "foodName": "묵은지 김치찌개",
    "risk": "위험",
    "ingredients": ["김치", "두부"],
    "hasIngredients": true,
    "recipeSteps": ["1. 육수를 만든다."],
    "recipeTip": "육수를 사용해 소금간을 줄인다.",
    "hasRecipe": true
  }
}
```

## 2. 식단표/메뉴 이미지 분석

```text
POST /api/meal/analyze/image
Content-Type: multipart/form-data
```

요청:

```text
Body → form-data
key: image
type: File
```

동작:

- OCR 결과가 아침/점심/저녁/간식 구조면 식단표로 분석합니다.
- 식단 구분이 없으면 단일 메뉴 이미지처럼 분석합니다.
- 분석 결과는 `meal_history`에 저장됩니다.

응답 예시:

```json
{
  "status": "SUCCESS",
  "message": "이미지 분석 성공",
  "ocrText": "묵은지\n김치찌개\n8,900원",
  "foods": ["묵은지", "김치찌개"],
  "analysis": {
    "김치찌개": {
      "foodName": "김치찌개",
      "risk": "위험",
      "ingredients": ["김치", "두부"],
      "hasIngredients": true,
      "recipeSteps": ["1. 육수를 만든다."],
      "recipeTip": "나트륨 섭취를 줄일 수 있다.",
      "hasRecipe": true
    }
  }
}
```

## 3. 일반 음식 이미지 분석

```text
POST /api/food/analyze/image
Content-Type: multipart/form-data
```

요청:

```text
Body → form-data
key: image
type: File
```

응답은 `/api/meal/analyze/image`의 단일 메뉴 분석 응답과 동일한 구조입니다.

주의:

- 현재는 OCR 기반입니다.
- 텍스트가 없는 음식 실물 사진은 실패 가능성이 큽니다.

## 4. 이미지 분석 기록 조회

### 최근 기록 조회

```text
GET /api/meal/history
```

응답 예시:

```json
[
  {
    "id": 2,
    "imagePath": "C:\\Users\\...\\uploads\\meal-history\\uuid.jpg",
    "ocrText": "묵은지\n김치찌개\n8,900원",
    "analysis": {
      "status": "SUCCESS",
      "foods": ["묵은지", "김치찌개"]
    },
    "createdAt": "2026-05-28T13:46:13"
  }
]
```

### 상세 기록 조회

```text
GET /api/meal/history/{id}
```

응답 예시:

```json
{
  "id": 2,
  "imagePath": "C:\\Users\\...\\uploads\\meal-history\\uuid.jpg",
  "ocrText": "묵은지\n김치찌개\n8,900원",
  "analysis": {
    "status": "SUCCESS",
    "analysis": {}
  },
  "createdAt": "2026-05-28T13:46:13"
}
```

조회 실패 예시:

```json
{
  "status": "NOT_FOUND",
  "message": "분석 기록을 찾을 수 없습니다."
}
```

## 5. 주간 식단표 저장

```text
POST /api/meal/weekly
Content-Type: application/json
```

요청 예시:

```json
{
  "date": "2026-05-28",
  "breakfast": "오트밀",
  "lunch": "묵은지 김치찌개",
  "dinner": "그릭요거트",
  "snack": "바나나"
}
```

응답 예시:

```json
{
  "id": 1,
  "date": "2026-05-28",
  "breakfast": "오트밀",
  "lunch": "묵은지 김치찌개",
  "dinner": "그릭요거트",
  "snack": "바나나",
  "analysis": {
    "breakfast": {
      "foodName": "오트밀",
      "risk": "위험",
      "hasRecipe": true
    },
    "lunch": {
      "foodName": "묵은지 김치찌개",
      "risk": "위험",
      "hasRecipe": true
    }
  },
  "createdAt": "2026-05-28T13:57:21",
  "updatedAt": "2026-05-28T13:57:21"
}
```

## 6. 주간 식단표 조회

### 기간 조회

```text
GET /api/meal/weekly?startDate=2026-05-26&endDate=2026-06-01
```

응답 예시:

```json
[
  {
    "id": 1,
    "date": "2026-05-28",
    "breakfast": "오트밀",
    "lunch": "묵은지 김치찌개",
    "dinner": "그릭요거트",
    "snack": "바나나",
    "analysis": {}
  }
]
```

### ID 조회

```text
GET /api/meal/weekly/{id}
```

### 날짜 조회

```text
GET /api/meal/weekly/date/2026-05-28
```

조회 실패 예시:

```json
{
  "status": "NOT_FOUND",
  "message": "해당 날짜의 식단표를 찾을 수 없습니다."
}
```

## 에러 응답 예시

### 이미지 파일 없음

```json
{
  "status": "NO_IMAGE_FILE",
  "message": "이미지 파일이 없습니다.",
  "ocrText": "",
  "foods": [],
  "analysis": {}
}
```

### OCR 실패

```json
{
  "status": "OCR_FAILED",
  "message": "이미지에서 텍스트를 인식하지 못했습니다.",
  "ocrText": "",
  "foods": [],
  "analysis": {}
}
```

### 음식명 추출 실패

```json
{
  "status": "NO_FOOD_DETECTED",
  "message": "OCR 텍스트는 인식했지만 음식명을 찾지 못했습니다.",
  "ocrText": "인식된 텍스트",
  "foods": [],
  "analysis": {}
}
```

### 서버 오류

```json
{
  "status": "ERROR",
  "message": "이미지 분석 중 서버 오류가 발생했습니다.",
  "ocrText": "",
  "foods": [],
  "analysis": {}
}
```

## Postman 테스트 순서

1. MySQL SQL 실행
2. 서버 실행
3. 텍스트 분석 테스트
4. 이미지 분석 테스트
5. `GET /api/meal/history`로 기록 저장 확인
6. `POST /api/meal/weekly`로 주간 식단 저장
7. `GET /api/meal/weekly?startDate=...&endDate=...`로 기간 조회 확인

## 한계점

- 일반 음식 사진 인식은 OCR 기반이라 텍스트가 없으면 실패할 수 있습니다.
- OCR 결과가 줄바꿈/특수문자/로고형 글자에 따라 흔들릴 수 있습니다.
- 식약처 API 검색 결과가 정확하지 않으면 fallback 검색어 보강이 필요합니다.
- 현재 기록 저장은 사용자 구분 없이 전체 공용 기록으로 저장됩니다.
