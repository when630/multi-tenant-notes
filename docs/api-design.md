# Multi-Tenant Notes App - API 설계서 v2

## 공통 사항

### Base URL
- 백엔드: `http://localhost:8080`
- 프론트: `http://{subdomain}.localhost:3000`

### 인증 방식
- JWT Bearer Token
- 헤더: `Authorization: Bearer {accessToken}`
- 테넌트 식별: `X-Tenant-Id: {tenantId}`

### 공통 응답 구조

```json
// 성공 (단건)
{
  "code": "SUCCESS",
  "data": { ... }
}

// 성공 (목록 + 페이징)
{
  "code": "SUCCESS",
  "data": {
    "content": [ ... ],
    "totalCount": 120,
    "page": 0,
    "size": 20
  }
}

// 실패
{
  "code": "TENANT_NOT_FOUND",
  "message": "테넌트를 찾을 수 없습니다."
}
```

### 공통 에러 코드

| HTTP 상태 | 코드 | 설명 |
|-----------|------|------|
| 400 | INVALID_REQUEST | 요청 파라미터 오류 |
| 401 | UNAUTHORIZED | 인증 필요 / 토큰 만료 |
| 403 | FORBIDDEN | 권한 없음 |
| 403 | PLAN_LIMIT_EXCEEDED | 플랜 제한 초과 |
| 404 | NOT_FOUND | 리소스 없음 |
| 404 | TENANT_NOT_FOUND | 테넌트 없음 |
| 409 | DUPLICATE | 중복 데이터 |
| 429 | RATE_LIMIT_EXCEEDED | 요청 제한 초과 |

### 공통 쿼리 파라미터 (목록 조회)

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| page | int | 0 | 페이지 번호 (0부터) |
| size | int | 20 | 페이지 크기 |
| sort | string | createdAt,desc | 정렬 기준 |

---

## 1. 인증 API

인증 관련은 테넌트 식별이 필요 없는 공개 API.

### POST /api/auth/login

로그인. Access Token + Refresh Token 발급.

- 권한: 없음 (공개)

```
Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "accessToken": "eyJhbG...",
  "refreshToken": "dGhpcyBp...",
  "expiresIn": 3600,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "isSuperAdmin": false,
    "tenants": [
      {
        "tenantId": 1,
        "tenantName": "팀 알파",
        "subdomain": "team-alpha",
        "role": "OWNER",
        "plan": "PRO"
      },
      {
        "tenantId": 2,
        "tenantName": "팀 베타",
        "subdomain": "team-beta",
        "role": "MEMBER",
        "plan": "FREE"
      }
    ]
  }
}
```

### POST /api/auth/refresh

Access Token 갱신.

- 권한: 없음 (Refresh Token 필요)

```
Request Body:
{
  "refreshToken": "dGhpcyBp..."
}

Response:
{
  "accessToken": "eyJhbG...",
  "expiresIn": 3600
}
```

### POST /api/auth/logout

로그아웃. Refresh Token 무효화.

- 권한: 인증 필요

```
Request Body:
{
  "refreshToken": "dGhpcyBp..."
}

Response:
{
  "code": "SUCCESS"
}
```

### POST /api/auth/signup

초대 링크를 통한 회원가입.

- 권한: 없음 (유효한 초대 토큰 필요)

```
Request Body:
{
  "inviteToken": "abc123...",
  "email": "newuser@example.com",
  "password": "password123",
  "name": "김철수"
}

Response:
{
  "accessToken": "eyJhbG...",
  "refreshToken": "dGhpcyBp...",
  "expiresIn": 3600,
  "user": { ... }
}
```

### GET /api/auth/invite/{token}

초대 링크 정보 조회.

- 권한: 없음 (공개)

```
Response:
{
  "tenantName": "팀 알파",
  "role": "MEMBER",
  "invitedBy": "홍길동",
  "expiresAt": "2026-04-01T00:00:00"
}
```

### GET /api/auth/me

현재 로그인한 사용자 정보 조회.

- 권한: 인증 필요

```
Response:
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "isSuperAdmin": false,
  "tenants": [
    {
      "tenantId": 1,
      "tenantName": "팀 알파",
      "subdomain": "team-alpha",
      "role": "OWNER",
      "plan": "PRO"
    }
  ]
}
```

### PUT /api/auth/me

내 정보 수정.

- 권한: 인증 필요

```
Request Body:
{
  "name": "홍길동(수정)",
  "currentPassword": "oldpass",
  "newPassword": "newpass123"
}

Response:
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동(수정)"
}
```

---

## 2. 노트 API

모든 노트 API는 X-Tenant-Id 헤더 필수.

### POST /api/notes

노트 생성.

- 권한: OWNER, ADMIN, MEMBER
- 플랜 제한: Free 플랜은 50개까지

```
Request Body (NoteInfoCreate):
{
  "title": "회의록",
  "content": "오늘 회의에서 논의한 내용...",
  "tagNames": ["회의", "프로젝트A"]
}

Response (NoteInfo):
{
  "id": 1,
  "title": "회의록",
  "content": "오늘 회의에서 논의한 내용...",
  "author": {
    "id": 1,
    "name": "홍길동"
  },
  "tags": [
    { "id": 1, "name": "회의" },
    { "id": 2, "name": "프로젝트A" }
  ],
  "isShared": false,
  "hasPublicLink": false,
  "commentCount": 0,
  "createdAt": "2026-03-13T10:00:00",
  "updatedAt": "2026-03-13T10:00:00"
}
```

### GET /api/notes

노트 목록 조회.

- 권한: OWNER, ADMIN, MEMBER
- OWNER/ADMIN은 테넌트 전체 노트 조회 가능

```
Query Parameters (NoteInfoSearch):
  page, size, sort
  keyword: string
  tagId: bigint
  authorId: bigint
  shared: boolean

Response (NoteInfoList):
{
  "content": [
    {
      "id": 1,
      "title": "회의록",
      "contentPreview": "오늘 회의에서 논의한...",
      "author": { "id": 1, "name": "홍길동" },
      "tags": [ { "id": 1, "name": "회의" } ],
      "isShared": false,
      "commentCount": 3,
      "createdAt": "2026-03-13T10:00:00",
      "updatedAt": "2026-03-13T10:00:00"
    }
  ],
  "totalCount": 45,
  "page": 0,
  "size": 20
}
```

### GET /api/notes/{noteId}

노트 상세 조회.

- 권한: 작성자 본인, 공유받은 멤버, OWNER, ADMIN

```
Response (NoteInfo):
{
  "id": 1,
  "title": "회의록",
  "content": "오늘 회의에서 논의한 내용...",
  "author": { "id": 1, "name": "홍길동" },
  "tags": [ { "id": 1, "name": "회의" }, { "id": 2, "name": "프로젝트A" } ],
  "shares": [
    { "userId": 2, "userName": "김철수", "sharedAt": "2026-03-13T11:00:00" }
  ],
  "publicLink": {
    "shareToken": "abc123...",
    "isActive": true,
    "expiresAt": null
  },
  "commentCount": 3,
  "createdAt": "2026-03-13T10:00:00",
  "updatedAt": "2026-03-13T10:00:00"
}
```

### PUT /api/notes/{noteId}

노트 수정.

- 권한: 작성자 본인, OWNER, ADMIN

```
Request Body (NoteInfoUpdate):
{
  "title": "회의록 (수정)",
  "content": "수정된 내용...",
  "tagNames": ["회의", "프로젝트B"]
}

Response (NoteInfo): { ... }
```

### DELETE /api/notes/{noteId}

노트 삭제 (soft delete).

- 권한: 작성자 본인, OWNER, ADMIN

```
Response:
{
  "code": "SUCCESS"
}
```

---

## 3. 노트 공유 API

### POST /api/notes/{noteId}/shares

테넌트 내 멤버에게 노트 공유.
공유 시 대상 멤버에게 알림 자동 생성 (NOTE_SHARED).

- 권한: 노트 작성자, OWNER, ADMIN

```
Request Body:
{
  "userIds": [2, 3]
}

Response:
{
  "shares": [
    { "userId": 2, "userName": "김철수", "sharedAt": "2026-03-13T11:00:00" },
    { "userId": 3, "userName": "이영희", "sharedAt": "2026-03-13T11:00:00" }
  ]
}
```

### DELETE /api/notes/{noteId}/shares/{userId}

공유 해제.

- 권한: 노트 작성자, OWNER, ADMIN

```
Response:
{
  "code": "SUCCESS"
}
```

### POST /api/notes/{noteId}/public-link

외부 공유 링크 생성.

- 권한: OWNER, ADMIN (Pro 플랜만)

```
Request Body:
{
  "expiresAt": "2026-04-13T00:00:00"
}

Response:
{
  "shareToken": "abc123...",
  "url": "http://team-alpha.localhost:3000/public/notes/abc123...",
  "isActive": true,
  "expiresAt": "2026-04-13T00:00:00"
}
```

### DELETE /api/notes/{noteId}/public-link

외부 공유 링크 비활성화.

- 권한: OWNER, ADMIN

```
Response:
{
  "code": "SUCCESS"
}
```

### GET /api/public/notes/{shareToken}

외부 공유 노트 열람. 인증 불필요.

- 권한: 없음 (공개, 유효한 토큰 필요)

```
Response:
{
  "title": "회의록",
  "content": "오늘 회의에서 논의한 내용...",
  "author": { "name": "홍길동" },
  "tenantName": "팀 알파",
  "createdAt": "2026-03-13T10:00:00"
}
```

---

## 4. 댓글 API

댓글 작성 시 노트 작성자 및 공유받은 멤버에게 알림 자동 생성 (COMMENT_ADDED).

### POST /api/notes/{noteId}/comments

댓글 작성.

- 권한: 노트에 접근 가능한 모든 멤버

```
Request Body (CommentInfoCreate):
{
  "content": "좋은 내용이네요!"
}

Response (CommentInfo):
{
  "id": 1,
  "content": "좋은 내용이네요!",
  "author": { "id": 2, "name": "김철수" },
  "createdAt": "2026-03-13T12:00:00",
  "updatedAt": "2026-03-13T12:00:00"
}
```

### GET /api/notes/{noteId}/comments

댓글 목록 조회.

- 권한: 노트에 접근 가능한 모든 멤버

```
Query Parameters:
  page, size

Response (CommentInfoList):
{
  "content": [
    {
      "id": 1,
      "content": "좋은 내용이네요!",
      "author": { "id": 2, "name": "김철수" },
      "createdAt": "2026-03-13T12:00:00",
      "updatedAt": "2026-03-13T12:00:00"
    }
  ],
  "totalCount": 5,
  "page": 0,
  "size": 20
}
```

### PUT /api/notes/{noteId}/comments/{commentId}

댓글 수정.

- 권한: 작성자 본인만

```
Request Body (CommentInfoUpdate):
{
  "content": "수정된 댓글입니다."
}

Response (CommentInfo): { ... }
```

### DELETE /api/notes/{noteId}/comments/{commentId}

댓글 삭제.

- 권한: 작성자 본인, OWNER, ADMIN

```
Response:
{
  "code": "SUCCESS"
}
```

---

## 5. 태그 API

### GET /api/tags

테넌트 내 태그 목록 조회.

- 권한: OWNER, ADMIN, MEMBER

```
Response:
{
  "tags": [
    { "id": 1, "name": "회의", "noteCount": 12 },
    { "id": 2, "name": "프로젝트A", "noteCount": 8 }
  ]
}
```

---

## 6. 멤버 관리 API

역할 변경 시 대상 멤버에게 알림 자동 생성 (ROLE_CHANGED).

### GET /api/members

테넌트 내 멤버 목록 조회.

- 권한: OWNER, ADMIN, MEMBER

```
Query Parameters (MemberInfoSearch):
  page, size
  keyword: string
  role: enum

Response (MemberInfoList):
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "email": "owner@example.com",
      "name": "홍길동",
      "role": "OWNER",
      "status": "ACTIVE",
      "joinedAt": "2026-01-01T00:00:00"
    }
  ],
  "totalCount": 4,
  "page": 0,
  "size": 20
}
```

### PUT /api/members/{memberId}/role

멤버 역할 변경.

- 권한: OWNER만

```
Request Body:
{
  "role": "ADMIN"
}

Response:
{
  "id": 1,
  "userId": 1,
  "name": "김철수",
  "role": "ADMIN"
}
```

### PUT /api/members/{memberId}/status

멤버 비활성화/활성화.

- 권한: OWNER, ADMIN

```
Request Body:
{
  "status": "INACTIVE"
}

Response:
{
  "id": 1,
  "userId": 1,
  "name": "김철수",
  "status": "INACTIVE"
}
```

---

## 7. 알림 API

### GET /api/notifications

알림 목록 조회.

- 권한: OWNER, ADMIN, MEMBER (본인 알림만)

```
Query Parameters:
  page, size
  isRead: boolean    ← 읽음/안읽음 필터 (생략하면 전체)

Response (NotificationInfoList):
{
  "content": [
    {
      "id": 1,
      "type": "NOTE_SHARED",
      "title": "노트 공유",
      "message": "홍길동님이 '회의록' 노트를 공유했습니다",
      "sender": { "id": 1, "name": "홍길동" },
      "entityType": "NOTE",
      "entityId": 1,
      "isRead": false,
      "createdAt": "2026-03-13T11:00:00"
    },
    {
      "id": 2,
      "type": "COMMENT_ADDED",
      "title": "새 댓글",
      "message": "김철수님이 '회의록'에 댓글을 남겼습니다",
      "sender": { "id": 2, "name": "김철수" },
      "entityType": "NOTE",
      "entityId": 1,
      "isRead": true,
      "createdAt": "2026-03-13T12:00:00"
    }
  ],
  "totalCount": 15,
  "page": 0,
  "size": 20
}
```

### GET /api/notifications/unread-count

읽지 않은 알림 수. 사이드바 뱃지 표시용.

- 권한: OWNER, ADMIN, MEMBER

```
Response:
{
  "count": 3
}
```

### PUT /api/notifications/{notificationId}/read

개별 알림 읽음 처리.

- 권한: 본인 알림만

```
Response:
{
  "code": "SUCCESS"
}
```

### PUT /api/notifications/read-all

전체 알림 읽음 처리.

- 권한: 본인 알림만

```
Response:
{
  "code": "SUCCESS"
}
```

### DELETE /api/notifications/{notificationId}

알림 삭제.

- 권한: 본인 알림만

```
Response:
{
  "code": "SUCCESS"
}
```

---

## 8. 초대 API

새 멤버 가입 시 OWNER/ADMIN에게 알림 자동 생성 (MEMBER_JOINED).

### POST /api/invitations

초대 링크 생성.

- 권한: OWNER, ADMIN
- 플랜 제한: Free 플랜은 멤버 5명까지

```
Request Body:
{
  "role": "MEMBER"
}

Response:
{
  "id": 1,
  "token": "abc123...",
  "inviteUrl": "http://localhost:3000/invite/abc123...",
  "role": "MEMBER",
  "status": "PENDING",
  "expiresAt": "2026-03-20T00:00:00"
}
```

### GET /api/invitations

테넌트의 초대 목록 조회.

- 권한: OWNER, ADMIN

```
Response:
{
  "content": [
    {
      "id": 1,
      "token": "abc123...",
      "role": "MEMBER",
      "status": "PENDING",
      "invitedBy": { "id": 1, "name": "홍길동" },
      "expiresAt": "2026-03-20T00:00:00",
      "createdAt": "2026-03-13T10:00:00"
    }
  ],
  "totalCount": 3,
  "page": 0,
  "size": 20
}
```

### DELETE /api/invitations/{invitationId}

초대 취소.

- 권한: OWNER, ADMIN

```
Response:
{
  "code": "SUCCESS"
}
```

---

## 9. 테넌트 설정 API

### GET /api/tenant

현재 테넌트 정보 조회.

- 권한: OWNER, ADMIN, MEMBER

```
Response:
{
  "id": 1,
  "name": "팀 알파",
  "subdomain": "team-alpha",
  "plan": "PRO",
  "status": "ACTIVE",
  "usage": {
    "noteCount": 45,
    "noteLimit": null,
    "memberCount": 4,
    "memberLimit": 50
  },
  "createdAt": "2026-01-01T00:00:00"
}
```

### PUT /api/tenant

테넌트 이름 변경.

- 권한: OWNER만

```
Request Body:
{
  "name": "팀 알파 (리뉴얼)"
}

Response:
{
  "id": 1,
  "name": "팀 알파 (리뉴얼)",
  "subdomain": "team-alpha",
  "plan": "PRO"
}
```

---

## 10. 활동 이력 API

### GET /api/activities

테넌트 내 활동 이력 조회.

- 권한: OWNER, ADMIN

```
Query Parameters:
  page, size
  entityType: string
  action: string
  userId: bigint

Response:
{
  "content": [
    {
      "id": 1,
      "user": { "id": 1, "name": "홍길동" },
      "entityType": "NOTE",
      "entityId": 1,
      "action": "CREATE",
      "detail": { "title": "회의록" },
      "createdAt": "2026-03-13T10:00:00"
    }
  ],
  "totalCount": 150,
  "page": 0,
  "size": 20
}
```

---

## 11. 슈퍼어드민 API

슈퍼어드민 전용. X-Tenant-Id 불필요.

### POST /api/super-admin/tenants

테넌트 생성.

- 권한: SUPER_ADMIN만

```
Request Body:
{
  "name": "팀 알파",
  "subdomain": "team-alpha",
  "plan": "FREE",
  "ownerEmail": "owner@example.com",
  "ownerName": "홍길동",
  "ownerPassword": "password123"
}

Response:
{
  "id": 1,
  "name": "팀 알파",
  "subdomain": "team-alpha",
  "plan": "FREE",
  "status": "ACTIVE",
  "owner": {
    "id": 1,
    "email": "owner@example.com",
    "name": "홍길동"
  },
  "createdAt": "2026-03-13T10:00:00"
}
```

### GET /api/super-admin/tenants

전체 테넌트 목록 조회.

- 권한: SUPER_ADMIN만

```
Query Parameters:
  page, size
  keyword: string
  plan: enum
  status: enum

Response:
{
  "content": [
    {
      "id": 1,
      "name": "팀 알파",
      "subdomain": "team-alpha",
      "plan": "FREE",
      "status": "ACTIVE",
      "memberCount": 4,
      "noteCount": 45,
      "createdAt": "2026-03-13T10:00:00"
    }
  ],
  "totalCount": 10,
  "page": 0,
  "size": 20
}
```

### GET /api/super-admin/tenants/{tenantId}

테넌트 상세 조회.

- 권한: SUPER_ADMIN만

```
Response:
{
  "id": 1,
  "name": "팀 알파",
  "subdomain": "team-alpha",
  "plan": "FREE",
  "status": "ACTIVE",
  "owner": { "id": 1, "email": "owner@example.com", "name": "홍길동" },
  "memberCount": 4,
  "noteCount": 45,
  "usage": {
    "noteCount": 45,
    "noteLimit": 50,
    "memberCount": 4,
    "memberLimit": 5
  },
  "createdAt": "2026-03-13T10:00:00",
  "updatedAt": "2026-03-13T10:00:00"
}
```

### PUT /api/super-admin/tenants/{tenantId}

테넌트 설정 변경 (플랜, 상태).

- 권한: SUPER_ADMIN만

```
Request Body:
{
  "plan": "PRO",
  "status": "ACTIVE"
}

Response:
{
  "id": 1,
  "name": "팀 알파",
  "plan": "PRO",
  "status": "ACTIVE"
}
```

### POST /api/super-admin/tenants/{tenantId}/invitations

슈퍼어드민이 특정 테넌트에 초대 링크 발급.

- 권한: SUPER_ADMIN만

```
Request Body:
{
  "role": "ADMIN"
}

Response:
{
  "token": "abc123...",
  "inviteUrl": "http://localhost:3000/invite/abc123...",
  "role": "ADMIN",
  "expiresAt": "2026-03-20T00:00:00"
}
```

### GET /api/super-admin/dashboard

슈퍼어드민 대시보드 통계.

- 권한: SUPER_ADMIN만

```
Response:
{
  "totalTenants": 10,
  "activeTenants": 8,
  "totalUsers": 45,
  "totalNotes": 320,
  "planDistribution": {
    "FREE": 6,
    "PRO": 4
  },
  "recentTenants": [
    { "id": 10, "name": "팀 오메가", "createdAt": "2026-03-12T10:00:00" }
  ]
}
```

---

## API 요약

| 도메인 | 메서드 | 엔드포인트 | 권한 |
|--------|--------|------------|------|
| 인증 | POST | /api/auth/login | 공개 |
| 인증 | POST | /api/auth/refresh | 공개 (토큰) |
| 인증 | POST | /api/auth/logout | 인증 |
| 인증 | POST | /api/auth/signup | 공개 (초대 토큰) |
| 인증 | GET | /api/auth/invite/{token} | 공개 |
| 인증 | GET | /api/auth/me | 인증 |
| 인증 | PUT | /api/auth/me | 인증 |
| 노트 | POST | /api/notes | O/A/M |
| 노트 | GET | /api/notes | O/A/M |
| 노트 | GET | /api/notes/{noteId} | 접근 권한자 |
| 노트 | PUT | /api/notes/{noteId} | 작성자/O/A |
| 노트 | DELETE | /api/notes/{noteId} | 작성자/O/A |
| 공유 | POST | /api/notes/{noteId}/shares | 작성자/O/A |
| 공유 | DELETE | /api/notes/{noteId}/shares/{userId} | 작성자/O/A |
| 공유 | POST | /api/notes/{noteId}/public-link | O/A (Pro) |
| 공유 | DELETE | /api/notes/{noteId}/public-link | O/A |
| 공유 | GET | /api/public/notes/{shareToken} | 공개 |
| 댓글 | POST | /api/notes/{noteId}/comments | 접근 권한자 |
| 댓글 | GET | /api/notes/{noteId}/comments | 접근 권한자 |
| 댓글 | PUT | /api/notes/{noteId}/comments/{commentId} | 작성자 |
| 댓글 | DELETE | /api/notes/{noteId}/comments/{commentId} | 작성자/O/A |
| 태그 | GET | /api/tags | O/A/M |
| 멤버 | GET | /api/members | O/A/M |
| 멤버 | PUT | /api/members/{memberId}/role | O |
| 멤버 | PUT | /api/members/{memberId}/status | O/A |
| 알림 | GET | /api/notifications | O/A/M |
| 알림 | GET | /api/notifications/unread-count | O/A/M |
| 알림 | PUT | /api/notifications/{notificationId}/read | O/A/M |
| 알림 | PUT | /api/notifications/read-all | O/A/M |
| 알림 | DELETE | /api/notifications/{notificationId} | O/A/M |
| 초대 | POST | /api/invitations | O/A |
| 초대 | GET | /api/invitations | O/A |
| 초대 | DELETE | /api/invitations/{invitationId} | O/A |
| 테넌트 | GET | /api/tenant | O/A/M |
| 테넌트 | PUT | /api/tenant | O |
| 이력 | GET | /api/activities | O/A |
| 어드민 | POST | /api/super-admin/tenants | SA |
| 어드민 | GET | /api/super-admin/tenants | SA |
| 어드민 | GET | /api/super-admin/tenants/{tenantId} | SA |
| 어드민 | PUT | /api/super-admin/tenants/{tenantId} | SA |
| 어드민 | POST | /api/super-admin/tenants/{tenantId}/invitations | SA |
| 어드민 | GET | /api/super-admin/dashboard | SA |

총 40개 엔드포인트. 권한 범례: SA=슈퍼어드민, O=OWNER, A=ADMIN, M=MEMBER

---

## 알림 생성 트리거 (서비스 레이어에서 처리)

| 트리거 이벤트 | 알림 type | 수신자 | 관련 API |
|--------------|-----------|--------|----------|
| 노트 공유 | NOTE_SHARED | 공유 대상 멤버 | POST /api/notes/{id}/shares |
| 댓글 작성 | COMMENT_ADDED | 노트 작성자 + 공유받은 멤버 (댓글 작성자 제외) | POST /api/notes/{id}/comments |
| 역할 변경 | ROLE_CHANGED | 대상 멤버 | PUT /api/members/{id}/role |
| 새 멤버 참가 | MEMBER_JOINED | OWNER + ADMIN 전원 | POST /api/auth/signup |

---

## DTO 네이밍 컨벤션

| 용도 | 접미사 | 예시 |
|------|--------|------|
| 생성 요청 | InfoCreate | NoteInfoCreate, CommentInfoCreate |
| 수정 요청 | InfoUpdate | NoteInfoUpdate, CommentInfoUpdate |
| 검색 조건 | InfoSearch | NoteInfoSearch, MemberInfoSearch |
| 단건 응답 | Info | NoteInfo, NotificationInfo |
| 목록 응답 | InfoList | NoteInfoList, NotificationInfoList |
