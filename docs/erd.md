# Multi-Tenant Notes App - ERD v2

## 테이블 목록 (13개)

| 구분 | 테이블 | 설명 |
|------|--------|------|
| 핵심 | TENANTS | 테넌트(조직) 정보 |
| 핵심 | USERS | 사용자 계정 |
| 핵심 | TENANT_MEMBERS | 테넌트-사용자 매핑 (N:M) |
| 도메인 | NOTES | 노트 |
| 도메인 | TAGS | 태그 |
| 도메인 | NOTE_TAGS | 노트-태그 매핑 (N:M) |
| 도메인 | COMMENTS | 댓글 |
| 공유 | NOTE_SHARES | 테넌트 내 멤버 간 노트 공유 |
| 공유 | NOTE_PUBLIC_LINKS | 외부 공유 링크 (Pro 플랜) |
| 알림 | NOTIFICATIONS | 사용자 알림 |
| 인증 | INVITATIONS | 초대 링크 |
| 인증 | REFRESH_TOKENS | 리프레시 토큰 |
| 이력 | ACTIVITY_HISTORY | 변경 이력 추적 |

---

## 테이블 상세

### TENANTS

테넌트(조직) 정보. 슈퍼어드민만 생성 가능.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| name | varchar(100) | NOT NULL | 테넌트 이름 |
| subdomain | varchar(50) | UNIQUE, NOT NULL | 서브도메인 (URL 식별자) |
| plan | enum | NOT NULL | FREE / PRO |
| status | enum | NOT NULL | ACTIVE / INACTIVE |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

### USERS

사용자 계정. 테넌트와 독립적으로 존재하며, TENANT_MEMBERS를 통해 테넌트에 소속.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| email | varchar(255) | UNIQUE, NOT NULL | 로그인 이메일 |
| password | varchar(255) | NOT NULL | bcrypt 해시 |
| name | varchar(50) | NOT NULL | 표시 이름 |
| is_super_admin | boolean | NOT NULL, default false | 슈퍼어드민 여부 |
| status | enum | NOT NULL | ACTIVE / INACTIVE |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

### TENANT_MEMBERS

사용자-테넌트 소속 관계. 한 사용자가 여러 테넌트에 소속 가능하며, 테넌트마다 역할이 다를 수 있음.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | |
| user_id | bigint | FK → USERS.id, NOT NULL | |
| role | enum | NOT NULL | OWNER / ADMIN / MEMBER |
| status | enum | NOT NULL | ACTIVE / INACTIVE |
| joined_at | timestamp | NOT NULL | 소속 일시 |

- UNIQUE 제약: (tenant_id, user_id)
- OWNER는 테넌트당 1명만 가능 (애플리케이션 레벨 제어)

### NOTES

노트. 테넌트 격리 대상.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | 데이터 격리용 |
| author_id | bigint | FK → USERS.id, NOT NULL | 작성자 |
| title | varchar(200) | NOT NULL | 제목 |
| content | text | | 본문 |
| is_deleted | boolean | NOT NULL, default false | soft delete |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

- INDEX: (tenant_id, is_deleted, created_at DESC)

### TAGS

태그. 테넌트 내에서 고유.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | |
| name | varchar(50) | NOT NULL | 태그명 |
| created_at | timestamp | NOT NULL | |

- UNIQUE 제약: (tenant_id, name)

### NOTE_TAGS

노트-태그 다대다 매핑. tenant_id 없음 (부모 조인으로 격리).

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| note_id | bigint | FK → NOTES.id, NOT NULL | |
| tag_id | bigint | FK → TAGS.id, NOT NULL | |

- PK: (note_id, tag_id) 복합키

### NOTE_SHARES

테넌트 내 멤버 간 노트 공유 (읽기 전용).

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| note_id | bigint | FK → NOTES.id, NOT NULL | |
| shared_with_id | bigint | FK → USERS.id, NOT NULL | 공유 대상 |
| created_at | timestamp | NOT NULL | |

- UNIQUE 제약: (note_id, shared_with_id)

### NOTE_PUBLIC_LINKS

외부 공유 링크. Pro 플랜 전용.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| note_id | bigint | FK → NOTES.id, NOT NULL | |
| share_token | varchar(64) | UNIQUE, NOT NULL | UUID 기반 토큰 |
| is_active | boolean | NOT NULL, default true | 활성 여부 |
| expires_at | timestamp | | 만료 일시 (null이면 무기한) |
| created_at | timestamp | NOT NULL | |

### COMMENTS

노트 댓글.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| note_id | bigint | FK → NOTES.id, NOT NULL | |
| author_id | bigint | FK → USERS.id, NOT NULL | 작성자 |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | 데이터 격리용 |
| content | text | NOT NULL | 댓글 내용 |
| created_at | timestamp | NOT NULL | |
| updated_at | timestamp | NOT NULL | |

### NOTIFICATIONS

사용자 알림. 이벤트 기반으로 자동 생성.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | 데이터 격리용 |
| recipient_id | bigint | FK → USERS.id, NOT NULL | 알림 수신자 |
| sender_id | bigint | FK → USERS.id | 알림 발생시킨 사용자 (시스템 알림은 null) |
| type | enum | NOT NULL | NOTE_SHARED / COMMENT_ADDED / ROLE_CHANGED / MEMBER_JOINED |
| title | varchar(200) | NOT NULL | 알림 제목 |
| message | varchar(500) | NOT NULL | 알림 내용 |
| entity_type | varchar(30) | | NOTE / COMMENT / MEMBER |
| entity_id | bigint | | 관련 엔티티 ID (클릭 시 이동용) |
| is_read | boolean | NOT NULL, default false | 읽음 여부 |
| created_at | timestamp | NOT NULL | |

- INDEX: (tenant_id, recipient_id, is_read, created_at DESC)
- INDEX: (recipient_id, is_read)

### INVITATIONS

초대 링크. 슈퍼어드민 또는 OWNER/ADMIN이 생성.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | 초대 대상 테넌트 |
| invited_by_id | bigint | FK → USERS.id, NOT NULL | 초대한 사람 |
| token | varchar(64) | UNIQUE, NOT NULL | 초대 토큰 |
| role | enum | NOT NULL | ADMIN / MEMBER (OWNER는 초대 불가) |
| status | enum | NOT NULL | PENDING / ACCEPTED / EXPIRED |
| expires_at | timestamp | NOT NULL | 만료 일시 |
| created_at | timestamp | NOT NULL | |

### REFRESH_TOKENS

JWT 리프레시 토큰.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| user_id | bigint | FK → USERS.id, NOT NULL | |
| token | varchar(255) | UNIQUE, NOT NULL | |
| expires_at | timestamp | NOT NULL | |
| created_at | timestamp | NOT NULL | |

### ACTIVITY_HISTORY

변경 이력 추적. 비즈니스 레벨 감사 로그.

| 컬럼 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | bigint | PK, auto_increment | |
| tenant_id | bigint | FK → TENANTS.id, NOT NULL | |
| user_id | bigint | FK → USERS.id, NOT NULL | 수행한 사용자 |
| entity_type | varchar(30) | NOT NULL | NOTE / COMMENT / MEMBER 등 |
| entity_id | bigint | NOT NULL | 대상 엔티티 ID |
| action | enum | NOT NULL | CREATE / UPDATE / DELETE / SHARE |
| detail | text | | JSON 형태의 변경 상세 |
| created_at | timestamp | NOT NULL | |

- INDEX: (tenant_id, entity_type, entity_id)
- INDEX: (tenant_id, created_at DESC)

---

## 관계 (Relationships)

```
TENANTS ||--o{ TENANT_MEMBERS    : has
TENANTS ||--o{ NOTES             : contains
TENANTS ||--o{ TAGS              : owns
TENANTS ||--o{ COMMENTS          : contains
TENANTS ||--o{ NOTIFICATIONS     : contains
TENANTS ||--o{ INVITATIONS       : issues
TENANTS ||--o{ ACTIVITY_HISTORY  : logs

USERS ||--o{ TENANT_MEMBERS      : belongs_to
USERS ||--o{ NOTES               : writes
USERS ||--o{ COMMENTS            : writes
USERS ||--o{ NOTE_SHARES         : receives
USERS ||--o{ NOTIFICATIONS       : receives
USERS ||--o{ INVITATIONS         : creates
USERS ||--o{ REFRESH_TOKENS      : has
USERS ||--o{ ACTIVITY_HISTORY    : performs

NOTES ||--o{ NOTE_TAGS           : has
NOTES ||--o{ NOTE_SHARES         : shared_via
NOTES ||--o{ NOTE_PUBLIC_LINKS   : has
NOTES ||--o{ COMMENTS            : has

TAGS ||--o{ NOTE_TAGS            : tagged_in
```

---

## 데이터 격리 전략

- tenant_id가 직접 존재하는 테이블: TENANT_MEMBERS, NOTES, TAGS, COMMENTS, NOTIFICATIONS, INVITATIONS, ACTIVITY_HISTORY
- 부모 조인으로 격리하는 테이블: NOTE_TAGS, NOTE_SHARES, NOTE_PUBLIC_LINKS (NOTES.tenant_id를 통해 격리)
- 테넌트 독립 테이블: USERS, REFRESH_TOKENS (테넌트와 무관하게 존재)

---

## 설계 포인트

1. 멀티 테넌트 소속: TENANT_MEMBERS로 N:M 관계를 구현하여 한 사용자가 여러 테넌트에 소속 가능
2. 역할 분리: USERS.is_super_admin은 시스템 레벨, TENANT_MEMBERS.role은 테넌트 레벨 권한
3. Soft delete: NOTES만 적용 (is_deleted 플래그). 멤버는 status로 비활성화
4. 외부 공유: NOTE_PUBLIC_LINKS를 별도 테이블로 분리하여 토큰 기반 공개 접근 지원
5. 알림: NOTIFICATIONS는 이벤트 기반으로 서비스 레이어에서 생성. entity_type + entity_id로 클릭 시 해당 페이지 이동 지원
6. 감사 추적: ACTIVITY_HISTORY에 entity_type + entity_id 패턴으로 범용 이력 저장
