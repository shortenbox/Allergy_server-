# Frontend Handoff

Android 팀 연동용 백엔드 API 요약입니다. 상세 예시는 `docs/API_SPEC.md`를 기준으로 보면 됩니다.

## Base URL

```text
http://localhost:8080
```

실기기 Android에서 로컬 PC 서버에 접근할 경우 같은 네트워크의 PC IP 주소로 바꿔야 합니다.

```text
http://PC_IP:8080
```

## 핵심 API

| 기능 | Method | Endpoint | Body |
| --- | --- | --- | --- |
| 텍스트 식단 분석 | `POST` | `/api/meal/analyze/text` | JSON |
| 식단표/메뉴 이미지 분석 | `POST` | `/api/meal/analyze/image` | multipart `image` |
| 일반 음식 이미지 분석 | `POST` | `/api/food/analyze/image` | multipart `image` |
| 이미지 분석 기록 목록 | `GET` | `/api/meal/history` | 없음 |
| 이미지 분석 기록 상세 | `GET` | `/api/meal/history/{id}` | 없음 |
| 날짜별 식단표 저장 | `POST` | `/api/meal/weekly` | JSON |
| 기간별 식단표 조회 | `GET` | `/api/meal/weekly?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | 없음 |
| 날짜별 식단표 조회 | `GET` | `/api/meal/weekly/date/{date}` | 없음 |

## Android 화면에서 주로 사용할 필드

음식 상세 화면:

```text
foodName
risk
ingredients
hasIngredients
recipeSteps
recipeTip
hasRecipe
carbohydrate
protein
fat
sodium
```

이미지 분석 결과 화면:

```text
status
message
ocrText
foods
analysis
```

분석 기록 화면:

```text
id
imagePath
ocrText
analysis
createdAt
```

주간 식단표 화면:

```text
id
date
breakfast
lunch
dinner
snack
analysis
createdAt
updatedAt
```

## 요청 예시

텍스트 분석:

```json
{
  "breakfast": "오트밀",
  "lunch": "묵은지 김치찌개",
  "dinner": "그릭요거트",
  "snack": "바나나"
}
```

주간 식단 저장:

```json
{
  "date": "2026-05-28",
  "breakfast": "오트밀",
  "lunch": "묵은지 김치찌개",
  "dinner": "그릭요거트",
  "snack": "바나나"
}
```

이미지 분석:

```text
multipart/form-data
key: image
type: File
```

## 에러 처리 기준

| status | 의미 | 프론트 처리 |
| --- | --- | --- |
| `NO_IMAGE_FILE` | 이미지 파일 없음 | 파일 선택 요청 |
| `OCR_FAILED` | OCR 텍스트 인식 실패 | 다른 이미지 요청 또는 직접 입력 유도 |
| `NO_FOOD_DETECTED` | OCR은 됐지만 음식명 추출 실패 | 직접 입력 유도 |
| `ERROR` | 서버 처리 오류 | 재시도 안내 |
| `NOT_FOUND` | 조회 대상 없음 | 빈 화면 또는 안내 문구 표시 |

## 주의사항

- 일반 음식 사진 인식은 현재 OCR 기반입니다.
- 텍스트가 없는 음식 실물 사진은 실패할 수 있습니다.
- PowerShell 출력에서는 한글이 깨져 보일 수 있지만, Postman/Android JSON 응답은 UTF-8 기준으로 확인하면 됩니다.
- 현재 기록 저장은 사용자 구분 없이 전체 공용 기록으로 저장됩니다.
