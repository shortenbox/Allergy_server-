# Future Work

추후 확장 기능 및 개선 메모입니다.

## 1. 로그인/JWT

- 사용자 계정 기반 서비스로 확장
- Spring Security + JWT 적용
- 로그인 사용자 기준으로 분석 기록과 식단표 분리

## 2. 사용자별 분석 기록

현재 `meal_history`와 `weekly_meal_plan`은 사용자 구분 없이 저장됩니다.

확장 방향:

```text
users
→ meal_history.user_id
→ weekly_meal_plan.user_id
```

필요 API:

```text
GET /api/users/{userId}/meal/history
GET /api/users/{userId}/meal/weekly
```

## 3. 비전 모델 기반 음식 인식

현재 일반 음식 사진은 OCR 기반입니다. 텍스트 없는 음식 사진을 인식하려면 이미지 분류/비전 모델이 필요합니다.

예상 흐름:

```text
음식 사진
→ Vision AI / 음식 분류 모델
→ 음식명 추론
→ 기존 MealService 분석
→ JSON 반환
```

## 4. API 안정화

- 외부 API timeout 처리
- `FOOD_NOT_FOUND`, `API_ERROR` 등 에러 코드 세분화
- OCR 실패와 음식 분석 실패 구분 강화
- 로그 레벨 정리

## 5. 검색 정확도 개선

- fallback 검색어 추가
- `ingredient_alias` 데이터 보강
- `food_dictionary`와 `FoodApiService` 연동 강화
- OCR 오인식 보정 규칙 추가

## 6. 캐시 정책 개선

현재 식약처 API 응답은 메모리 캐시입니다.

개선 방향:

- TTL 기반 캐시 만료
- Redis 도입
- 자주 검색되는 음식 DB 캐싱

## 7. 운영/배포 준비

- API 키 환경변수 분리
- DB 비밀번호 환경변수 분리
- 서버 프로필 분리: local/dev/prod
- 배포용 Dockerfile 작성
- README에 배포 절차 추가
