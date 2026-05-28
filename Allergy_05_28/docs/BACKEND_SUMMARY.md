# Backend Summary

발표/제출용 백엔드 기능 요약입니다.

## 프로젝트 개요

Allergy Server는 OCR 기반 식단 분석 서버입니다. 사용자가 텍스트 식단, 식단표 이미지, 메뉴 이미지 등을 업로드하면 음식명을 추출하고 영양 정보, 알러지 위험, 재료, 조리법, 저감 TIP을 분석해 JSON으로 반환합니다.

## 핵심 기능

- 텍스트 식단 분석
- 식단표 이미지 OCR 분석
- 단일 메뉴/제품 이미지 OCR 분석
- 식약처 COOKRCP01 API 연동
- 음식별 영양 정보 반환
- 재료 `ingredients` 추출
- 알러지 위험 `risk` 분석
- 조리 순서 `recipeSteps` 반환
- 저감 조리법 TIP `recipeTip` 반환
- 이미지 분석 기록 저장/조회
- 날짜별 주간 식단표 저장/조회

## 백엔드 처리 흐름

```text
입력
→ Controller
→ OCR 또는 텍스트 파싱
→ MealService 분석
→ FoodApiService 식약처 API 조회
→ IngredientAliasService 음식명 표준화
→ CheckAllergyService 알러지 위험 분석
→ DB 저장
→ JSON 응답
```

## 이미지 분석 흐름

```text
이미지 업로드
→ Clova/Google OCR
→ 식단표 구조 여부 판단
→ 식단표면 아침/점심/저녁/간식 분리
→ 단일 메뉴면 OCR 텍스트에서 음식명 추출
→ 음식별 분석
→ meal_history 저장
→ 분석 JSON 반환
```

## DB 테이블 설명

| 테이블 | 설명 |
| --- | --- |
| `allergen_in` | 알러지 위험 재료 기준 데이터 |
| `ingredient_alias` | OCR/입력 음식명 표준화 데이터 |
| `allergy_category` | 알러지 카테고리 |
| `food_dictionary` | 음식 검색명 매핑 |
| `meal_history` | 이미지 분석 기록 저장 |
| `weekly_meal_plan` | 날짜별 식단표 저장 |

## 주요 API

```text
POST /api/meal/analyze/text
POST /api/meal/analyze/image
POST /api/food/analyze/image
GET  /api/meal/history
GET  /api/meal/history/{id}
POST /api/meal/weekly
GET  /api/meal/weekly
GET  /api/meal/weekly/{id}
GET  /api/meal/weekly/date/{date}
```

## 검증 완료 항목

- Maven compile 성공
- Maven package 성공
- jar 실행 방식으로 서버 구동 확인
- 텍스트 분석 API 확인
- 이미지 분석 API 확인
- OCR 실패 케이스 확인
- `meal_history` DB 저장 확인
- `weekly_meal_plan` DB 저장 확인
- 기록 조회 API 확인
- 주간 식단표 조회 API 확인

## 한계점

- 현재 일반 음식 사진 인식은 OCR 기반입니다.
- 텍스트가 없는 음식 실물 사진은 `OCR_FAILED`가 발생할 수 있습니다.
- 제품 패키지의 로고형 글자, 곡면, 저화질 이미지는 OCR 정확도가 떨어질 수 있습니다.
- 사용자별 기록 저장은 아직 없습니다.
- 로그인/JWT 인증은 아직 없습니다.

## 발표 시 강조 포인트

- OCR, 외부 식품 API, 로컬 알러지 DB를 조합한 분석 파이프라인
- 단순 분석뿐 아니라 기록 저장과 주간 식단표 저장까지 확장
- Android 연동을 고려해 JSON 응답 필드와 boolean 필드 제공
- 실패 케이스를 `status` 코드로 분리해 프론트 처리 가능
