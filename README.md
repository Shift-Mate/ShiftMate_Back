# ShiftMate Backend

ShiftMate 백엔드는 매장 단위의 근무표 운영을 위해 인증, 매장/멤버 관리, 근무 템플릿/자동 스케줄 생성, 출퇴근, 대타 요청/승인, 급여 집계를 제공합니다.

## 1. Tech Stack

- Java 21
- Spring Boot 4.0.2
- Spring Data JPA, Spring Validation, Spring Security
- JWT (jjwt 0.12.5)
- MySQL
- Redis (리프레시 토큰 저장)
- Spring Mail (비밀번호 재설정 메일)
- Gradle
- Docker (멀티 스테이지 빌드)

## 2. Core Features

- 인증/인가
- 회원가입, 로그인, 토큰 재발급, 로그아웃
- 비밀번호 재설정(토큰 발급 + 메일 발송 + 토큰 만료 정리 스케줄러)
- 매장/매장 멤버 관리
- 매장 CRUD 및 사업자번호(Bizno API) 검증
- 매장 멤버 초대/조회/수정/삭제(소프트 삭제)
- 근무 템플릿/선호도/자동 스케줄
- 템플릿 생성(COSTSAVER/HIGHSERVICE), 필요 인원 설정, 타입 선택
- 직원별 선호도 등록/조회/수정/삭제
- 월요일 시작 기준 주간 자동 스케줄 생성
- 출퇴근
- PIN 기반 출근/퇴근 처리
- 일별/주별 근태 조회, 개인 주간 근태 조회
- 대타
- 대타 요청/지원/취소/승인/거절/관리자 취소
- 승인 시 실제 배정 멤버를 지원자로 교체
- 급여 집계
- 월별 매장 단위 근무시간/예상 급여 집계
- 지각 시 급여 시작 시각 보정(30분 단위 라운딩)

## 3. Project Structure

```text
src/main/java/com/example/shiftmate
├── domain
│   ├── auth
│   ├── user
│   ├── store
│   ├── storeMember
│   ├── shiftTemplate
│   ├── employeePreference
│   ├── shiftAssignment
│   ├── attendance
│   └── substitute
└── global
    ├── config
    ├── security
    ├── exception
    └── common
```

## 4. Environment Variables

`src/main/resources/application.properties`에서 `.env`를 import 하도록 설정되어 있습니다.

필수/권장 변수:

- `JWT_SECRET` (32자 이상 권장)
- `JWT_ACCESS_EXPIRATION` (ms, 기본 3600000)
- `JWT_REFRESH_EXPIRATION` (ms, 기본 604800000)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME` (예: `com.mysql.cj.jdbc.Driver`)
- `REDIS_HOST` (기본 `localhost`)
- `REDIS_PORT` (기본 `6379`)
- `REDIS_TIMEOUT` (기본 `2s`)
- `BIZNO_API_KEY`
- `MAIL_HOST` (기본 `smtp.gmail.com`)
- `MAIL_PORT` (기본 `587`)
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM` (미설정 시 `MAIL_USERNAME`)
- `FRONTEND_BASE_URL` (기본 `http://localhost:3000`)

## 5. Local Run

### 5.1 Prerequisites

- JDK 21
- MySQL
- Redis

### 5.2 Run

```bash
./gradlew bootRun
```

기본 포트: `8080`

현재 설정:

- `spring.jpa.hibernate.ddl-auto=create`
- `spring.sql.init.mode=always`
- `spring.jpa.defer-datasource-initialization=true`

즉, 애플리케이션 시작 시 스키마 생성 후 `src/main/resources/data.sql`이 실행됩니다.

## 6. Docker

```bash
docker build -t shiftmate-back .
docker run -p 8080:8080 --env-file .env shiftmate-back
```

## 7. API Overview

### Auth (`/auth`)

- `POST /auth/signup`
- `POST /auth/login`
- `POST /auth/reissue`
- `POST /auth/logout`
- `POST /auth/password-reset/request`
- `POST /auth/password-reset/confirm`

### User (`/users`)

- `GET /users/admin/user-info`
- `GET /users/me`
- `GET /users/me/salary/months`
- `GET /users/me/salary/monthly`
- `PATCH /users/me/password`
- `PATCH /users/me`

### Store (`/stores`)

- `POST /stores`
- `GET /stores`
- `GET /stores/{storeId}`
- `PUT /stores/{storeId}`
- `DELETE /stores/{storeId}`
- `POST /stores/verify-bizno`

### Store Member (`/stores/{storeId}/store-members`)

- `POST /stores/{storeId}/store-members/{userId}`
- `GET /stores/{storeId}/store-members`
- `GET /stores/{storeId}/store-members/{id}`
- `PUT /stores/{storeId}/store-members/{id}`
- `DELETE /stores/{storeId}/store-members/{id}`

### Shift Template (`/stores/{storeId}/shift-template`)

- `POST /stores/{storeId}/shift-template`
- `GET /stores/{storeId}/shift-template`
- `GET /stores/{storeId}/shift-template/type`
- `PUT /stores/{storeId}/shift-template`
- `PUT /stores/{storeId}/shift-template/{templateId}`
- `DELETE /stores/{storeId}/shift-template`
- `DELETE /stores/{storeId}/shift-template/type`

### Employee Preference (`/stores/{storeId}/members/{memberId}/preferences`)

- `POST /stores/{storeId}/members/{memberId}/preferences`
- `GET /stores/{storeId}/members/{memberId}/preferences`
- `PUT /stores/{storeId}/members/{memberId}/preferences/{preferenceId}`
- `DELETE /stores/{storeId}/members/{memberId}/preferences`

### Shift Assignment (`/stores/{storeId}/schedules`)

- `POST /stores/{storeId}/schedules/auto-generate`
- `GET /stores/{storeId}/schedules`
- `GET /stores/{storeId}/schedules/me`
- `DELETE /stores/{storeId}/schedules`

### Attendance (`/stores/{storeId}/attendance`)

- `POST /stores/{storeId}/attendance/clock`
- `GET /stores/{storeId}/attendance/daily`
- `GET /stores/{storeId}/attendance/weekly`
- `GET /stores/{storeId}/attendance/weekly/my`

### Substitute (`/stores/{storeId}/substitute-requests`)

- `POST /stores/{storeId}/substitute-requests`
- `GET /stores/{storeId}/substitute-requests/others`
- `GET /stores/{storeId}/substitute-requests/my`
- `GET /stores/{storeId}/substitute-requests/all`
- `DELETE /stores/{storeId}/substitute-requests/{requestId}`
- `POST /stores/{storeId}/substitute-requests/{requestId}/apply`
- `GET /stores/{storeId}/substitute-requests/applications/my`
- `DELETE /stores/{storeId}/substitute-requests/applications/{applicationId}`
- `GET /stores/{storeId}/substitute-requests/{requestId}/applications`
- `PATCH /stores/{storeId}/substitute-requests/{requestId}/applications/{applicationId}/approve`
- `PATCH /stores/{storeId}/substitute-requests/{requestId}/applications/{applicationId}/reject`
- `DELETE /stores/{storeId}/substitute-requests/{requestId}/manager-cancel`

## 8. Response Format

모든 응답은 공통 래퍼를 사용합니다.

성공:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

실패:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "오류 메시지",
    "details": []
  }
}
```

## 9. Domain Rules (Important)

- 스케줄 자동 생성은 매장 `MANAGER`만 가능하며, 시작일은 월요일이어야 합니다.
- 동일 주차 스케줄이 이미 있으면 중복 생성이 불가합니다.
- 출퇴근 처리는 매장 관리자 화면에서 수행하며, 근무자 PIN 검증이 필요합니다.
- 출근 후 5분 이내에는 퇴근 처리할 수 없습니다.
- 대타 요청은 본인 스케줄만 가능하고 근무 시작 24시간 전까지만 허용됩니다.
- 대타 지원은 동일 부서만 가능하며, 기존 스케줄 시간 충돌 시 지원할 수 없습니다.
- 대타 승인 시 해당 근무 배정 담당자가 실제로 변경됩니다.

## 10. Notes

- 보안 컨텍스트는 JWT 필터 기반이며 `Authorization: Bearer <accessToken>` 형식을 사용합니다.
- 일부 권한 검증은 서비스 레이어(매장 멤버/랭크 검증)에서 강제됩니다.
- 비밀번호 재설정 토큰은 10분마다 스케줄러가 만료 데이터를 정리합니다.
