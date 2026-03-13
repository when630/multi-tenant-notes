# Multi-Tenant Notes App - 개발 계획서

## 개발 순서 요약

```
Phase 1: 프로젝트 기반 세팅
    ↓
Phase 2: 인증 (Auth)
    ↓
Phase 3: 테넌트 + 멤버십 기초
    ↓
Phase 4: 초대 + 회원가입
    ↓
Phase 5: 권한 (RBAC)
    ↓
Phase 6: 노트 CRUD
    ↓
Phase 7: 노트 공유 + 댓글
    ↓
Phase 8: 알림
    ↓
Phase 9: 활동 이력
    ↓
Phase 10: 슈퍼어드민 UI
    ↓
Phase 11: 마감 작업
```

각 Phase는 이전 Phase에 의존합니다. 순서를 지켜야 합니다.
Phase 안에서는 백엔드를 먼저 하고, 프론트엔드를 붙이는 순서로 진행합니다.

---

## Phase 1: 프로젝트 기반 세팅

### 1-1. 백엔드 프로젝트 초기화

| 작업 | 상세 |
|------|------|
| 멀티 모듈 생성 | src (메인), notes-common (엔티티/DTO/인터페이스), notes-storage (스토리지 추상화) |
| build.gradle | Spring Boot 3.x, JPA, QueryDSL 5.1, JWT, Flyway, Lombok, PostgreSQL |
| application.yml | 프로필 분리: local, local-h2, dev, production |
| Docker Compose | PostgreSQL 컨테이너 |
| 프로젝트 구조 | config/, domain/, common/ 패키지 구성 |

산출물:
- multi-tenant-notes/backend/ 프로젝트
- docker-compose.yml
- 빌드 및 기동 확인

### 1-2. 프론트엔드 프로젝트 초기화

| 작업 | 상세 |
|------|------|
| Next.js 생성 | yarn create next-app (Pages Router, TypeScript) |
| Tailwind + shadcn/ui | 초기화 및 기본 컴포넌트 설치 |
| 폴더 구조 | pages/, components/, layouts/, stores/, utils/, interfaces/, provider/, messages/ |
| Zustand | 스토어 기본 틀 생성 |
| axios | tenantAPI.ts 인스턴스 생성 |
| next-intl | ko.json, en.json 기본 파일 |
| next-themes | 다크모드 기본 설정 |

산출물:
- multi-tenant-notes/frontend/ 프로젝트
- 개발 서버 기동 확인

---

## Phase 2: 인증 (Auth)

모든 기능의 기반. 로그인/로그아웃이 되어야 이후 개발과 테스트가 가능합니다.

### 2-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V1 | USERS, REFRESH_TOKENS 테이블 생성 |
| 엔티티 | User, RefreshToken |
| Repository | UserRepository, RefreshTokenRepository |
| JWT 유틸 | JwtTokenProvider (생성, 검증, 파싱) |
| AuthService | login, logout, refresh, getMe, updateMe |
| AuthController | POST login, POST refresh, POST logout, GET me, PUT me |
| SecurityConfig | JWT 인증 필터, 공개 URL 설정 |
| 시드 데이터 | 슈퍼어드민 계정 (Flyway 또는 CommandLineRunner) |

API 완성 목록:
- POST /api/auth/login
- POST /api/auth/refresh
- POST /api/auth/logout
- GET /api/auth/me
- PUT /api/auth/me

### 2-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| AuthLayout | 중앙 정렬 카드 + 하단 다크모드/언어 전환 |
| 로그인 페이지 | /login (이메일, 비밀번호, 로그인 버튼) |
| 스토어 | useTokenInfoStore, useAccountStore |
| tenantAPI | login, refresh, logout, getMe, updateMe 메서드 |
| AuthProvider | 401 인터셉터, authEventBus, 자동 토큰 갱신 |
| BrowserStorage | 토큰 저장/삭제 유틸 |

검증:
- 로그인 → 토큰 발급 → me 조회 → 로그아웃 흐름 확인

---

## Phase 3: 테넌트 + 멤버십 기초

테넌트 구조가 서야 데이터 격리를 비롯한 모든 멀티 테넌트 기능이 동작합니다.

### 3-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V2 | TENANTS, TENANT_MEMBERS 테이블 생성 |
| 엔티티 | Tenant, TenantMember |
| TenantContext | ThreadLocal 기반 현재 테넌트 ID 보관 |
| TenantInterceptor | X-Tenant-Id 헤더 파싱 → TenantContext 설정 |
| TenantService | 테넌트 정보 조회, 사용량 조회 |
| TenantController | GET /api/tenant, PUT /api/tenant |
| SuperAdminController | 테넌트 CRUD (POST, GET, GET/{id}, PUT/{id}) |
| SuperAdmin 대시보드 API | GET /api/super-admin/dashboard |

API 완성 목록:
- GET /api/tenant
- PUT /api/tenant
- POST /api/super-admin/tenants
- GET /api/super-admin/tenants
- GET /api/super-admin/tenants/{tenantId}
- PUT /api/super-admin/tenants/{tenantId}
- GET /api/super-admin/dashboard

### 3-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| 테넌트 선택 페이지 | /select-tenant (TenantCard 목록) |
| MainLayout | 사이드바 + 헤더 기본 구조 |
| Sidebar | 상단(테넌트 이름, 유저, 알림 자리), 중간(네비), 하단(다크모드, 언어, 플랜) |
| Header | Breadcrumb 컴포넌트 |
| useTenantStore | 현재 테넌트 정보 + 역할 |
| PageSSR | getServerSideProps 래퍼 (인증 + 테넌트 식별) |
| 서브도메인 리졸빙 | req.headers.host에서 서브도메인 파싱 |
| ThemeToggle | 다크모드 전환 버튼 |
| LanguageToggle | KO/EN 전환 |
| PlanBadge | FREE/PRO 뱃지 |

검증:
- 슈퍼어드민으로 테넌트 생성 (Postman)
- 로그인 → 테넌트 선택 → {subdomain}.localhost:3000 접속
- MainLayout 렌더링 확인

---

## Phase 4: 초대 + 회원가입

멤버를 테넌트에 추가할 수 있어야 이후 권한/공유 테스트가 가능합니다.

### 4-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V3 | INVITATIONS 테이블 생성 |
| 엔티티 | Invitation |
| InvitationService | 생성, 목록, 토큰 검증, 취소, 만료 처리 |
| AuthService 확장 | signup (초대 토큰 → 유저 생성 → TENANT_MEMBERS 추가) |
| InvitationController | POST, GET, DELETE /api/invitations |
| AuthController 확장 | POST /api/auth/signup, GET /api/auth/invite/{token} |
| SuperAdmin 확장 | POST /api/super-admin/tenants/{id}/invitations |

API 완성 목록:
- POST /api/auth/signup
- GET /api/auth/invite/{token}
- POST /api/invitations
- GET /api/invitations
- DELETE /api/invitations/{invitationId}
- POST /api/super-admin/tenants/{tenantId}/invitations

### 4-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| 초대 회원가입 페이지 | /invite/[token] |
| 초대 관리 페이지 | /admin/invitations |
| InvitationCreateDialog | 역할 선택 → 링크 생성 → 복사 |
| InvitationTable | 초대 목록 (상태 뱃지, 취소) |
| StatusBadge | PENDING/ACCEPTED/EXPIRED |

검증:
- 초대 링크 생성 → 링크로 회원가입 → 테넌트 소속 확인
- 여러 테넌트에 같은 이메일로 가입 → 테넌트 선택 화면 확인

---

## Phase 5: 권한 (RBAC)

멤버가 있으니 역할별 접근 제어를 적용합니다.

### 5-1. 백엔드

| 작업 | 상세 |
|------|------|
| @RoleRequired | 커스텀 어노테이션 (허용 역할 지정) |
| RoleCheckInterceptor | TenantContext + 현재 유저 → TENANT_MEMBERS에서 역할 조회 → 권한 체크 |
| MemberService | 목록 조회, 역할 변경, 비활성화 |
| MemberController | GET /api/members, PUT role, PUT status |

API 완성 목록:
- GET /api/members
- PUT /api/members/{memberId}/role
- PUT /api/members/{memberId}/status

### 5-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| 멤버 관리 페이지 | /admin/members |
| MemberSearchPanel | 키워드, 역할 필터 |
| MemberSearchTable | 멤버 목록, 액션 메뉴 |
| RoleChangeDialog | 역할 변경 다이얼로그 |
| RoleBadge | OWNER/ADMIN/MEMBER 색상 구분 |
| 사이드바 메뉴 | 역할별 표시/숨김 적용 |
| UserDropdown | 내 정보, 테넌트 전환, 로그아웃 |

검증:
- OWNER: 모든 메뉴 접근, 역할 변경 가능
- ADMIN: 관리 메뉴 접근, 역할 변경 불가
- MEMBER: 관리 메뉴 숨김, 403 응답 확인

---

## Phase 6: 노트 CRUD

핵심 도메인. 데이터 격리가 제대로 동작하는지 검증하는 가장 중요한 단계입니다.

### 6-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V4 | NOTES, TAGS, NOTE_TAGS 테이블 생성 |
| 엔티티 | Note, Tag, NoteTag |
| TenantBaseEntity | tenantId 자동 주입 (@PrePersist) |
| NoteService | CRUD, 소프트 삭제, 플랜 제한 체크 (Free 50개) |
| NoteRepository | QueryDSL 동적 검색 (keyword, tagId, authorId, shared) |
| TagService | 태그 목록, 자동 생성 (노트 저장 시) |
| NoteController | POST, GET, GET/{id}, PUT/{id}, DELETE/{id} |
| TagController | GET /api/tags |
| Hibernate @Filter | tenant_id 자동 필터링 (또는 Repository 레벨) |

API 완성 목록:
- POST /api/notes
- GET /api/notes
- GET /api/notes/{noteId}
- PUT /api/notes/{noteId}
- DELETE /api/notes/{noteId}
- GET /api/tags

### 6-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| 대시보드 | / (통계 카드, 최근 노트) |
| 노트 목록 | /notes (NoteSearchPanel, NoteSearchTable) |
| 노트 작성 | /notes/new (NoteEditor) |
| 노트 상세 | /notes/[noteId] (NoteDisplay) |
| 노트 편집 | /notes/[noteId]/edit (NoteEditor 재사용) |
| NoteLink | 노트 링크 컴포넌트 |

검증:
- 테넌트A에서 노트 생성 → 테넌트B에서 안 보이는지 확인 (데이터 격리)
- Free 플랜 50개 초과 시 에러 확인
- MEMBER는 본인 노트만 수정/삭제, OWNER/ADMIN은 전체 가능

---

## Phase 7: 노트 공유 + 댓글

노트에 협업 기능을 올립니다.

### 7-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V5 | NOTE_SHARES, NOTE_PUBLIC_LINKS, COMMENTS 테이블 생성 |
| 엔티티 | NoteShare, NotePublicLink, Comment |
| NoteShareService | 멤버 공유, 공유 해제 |
| NotePublicLinkService | 외부 링크 생성/비활성화, Pro 플랜 체크 |
| CommentService | CRUD |
| PublicNoteController | GET /api/public/notes/{shareToken} |
| NoteShareController | POST, DELETE |
| CommentController | POST, GET, PUT, DELETE |

API 완성 목록:
- POST /api/notes/{noteId}/shares
- DELETE /api/notes/{noteId}/shares/{userId}
- POST /api/notes/{noteId}/public-link
- DELETE /api/notes/{noteId}/public-link
- GET /api/public/notes/{shareToken}
- POST /api/notes/{noteId}/comments
- GET /api/notes/{noteId}/comments
- PUT /api/notes/{noteId}/comments/{commentId}
- DELETE /api/notes/{noteId}/comments/{commentId}

### 7-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| NoteSharePanel | 멤버 공유 목록 + 외부 링크 표시 |
| MemberShareDialog | 멤버 선택 → 공유 |
| PublicLinkDialog | 외부 링크 생성 (만료일 선택) |
| CommentList | 댓글 목록 (수정/삭제 드롭다운) |
| CommentEditor | 댓글 입력/수정 |
| 외부 공유 페이지 | /public/notes/[shareToken] (독립 페이지) |

검증:
- 멤버 공유 → 대상자 노트 목록에 표시
- Free 플랜 외부 링크 시도 → PLAN_LIMIT_EXCEEDED
- 외부 링크로 비회원 접속 확인
- 댓글 CRUD + 권한 확인

---

## Phase 8: 알림

공유, 댓글, 역할 변경 등 이벤트가 갖춰진 후에 알림을 추가합니다.

### 8-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V6 | NOTIFICATIONS 테이블 생성 |
| 엔티티 | Notification |
| NotificationService | 생성, 목록, 미읽 카운트, 읽음 처리, 삭제 |
| NotificationController | API 5개 |
| 알림 트리거 연동 | NoteShareService → NOTE_SHARED 알림 생성 |
| | CommentService → COMMENT_ADDED 알림 생성 |
| | MemberService → ROLE_CHANGED 알림 생성 |
| | AuthService (signup) → MEMBER_JOINED 알림 생성 |

API 완성 목록:
- GET /api/notifications
- GET /api/notifications/unread-count
- PUT /api/notifications/{notificationId}/read
- PUT /api/notifications/read-all
- DELETE /api/notifications/{notificationId}

### 8-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| NotificationPopover | 벨 아이콘 클릭 → 팝오버 (최근 10개) |
| NotificationItem | 개별 알림 (미읽 표시, 클릭 시 이동) |
| useNotificationStore | 미읽 카운트 관리 |
| 사이드바 벨 아이콘 | 미읽 카운트 뱃지 |
| 알림 폴링 | 페이지 전환마다 unread-count 조회 |

검증:
- 노트 공유 → 대상자에게 알림
- 댓글 작성 → 노트 작성자 + 공유받은 멤버에게 알림
- 역할 변경 → 대상자에게 알림
- 새 멤버 가입 → OWNER/ADMIN에게 알림
- 알림 클릭 → 해당 노트로 이동

---

## Phase 9: 활동 이력

핵심 기능이 모두 완성된 상태에서 이력 추적을 추가합니다.

### 9-1. 백엔드

| 작업 | 상세 |
|------|------|
| Flyway V7 | ACTIVITY_HISTORY 테이블 생성 |
| 엔티티 | ActivityHistory |
| ActivityHistoryService | 이력 저장, 조회 |
| ActivityHistoryController | GET /api/activities |
| @ApiCallLog AOP | API 코드별 실행 시간 기록 |
| 서비스 연동 | NoteService, CommentService, MemberService, NoteShareService에 이력 기록 추가 |

API 완성 목록:
- GET /api/activities

### 9-2. 프론트엔드

| 작업 | 상세 |
|------|------|
| 활동 이력 페이지 | /admin/activities |
| 필터 | entityType, action Select |
| 이력 테이블 | Table + ActionBadge |
| 대시보드 확장 | 최근 활동 섹션 추가 (OWNER/ADMIN만) |

검증:
- 노트 생성/수정/삭제 → 이력 기록 확인
- 댓글, 공유, 멤버 변경 → 이력 기록 확인
- 필터링 동작 확인

---

## Phase 10: 슈퍼어드민 UI

백엔드 API는 Phase 3에서 완성. 여기서는 전용 프론트엔드를 구축합니다.

### 10-1. 프론트엔드

| 작업 | 상세 |
|------|------|
| SuperAdminLayout | 슈퍼어드민 전용 사이드바 |
| 어드민 대시보드 | /super-admin (통계 카드, 최근 테넌트) |
| 테넌트 관리 | /super-admin/tenants (검색 + 테이블 + 생성 다이얼로그) |
| 테넌트 상세 | /super-admin/tenants/[tenantId] (정보, 사용량, 초대 발급) |
| 테넌트 생성 다이얼로그 | 테넌트 정보 + OWNER 정보 입력 |

검증:
- 슈퍼어드민 로그인 → 어드민 대시보드 접근
- 테넌트 생성 → 목록에 표시
- 플랜/상태 변경 → 해당 테넌트에 반영
- 일반 사용자 접근 시 403

---

## Phase 11: 마감 작업

### 11-1. UI/UX 마무리

| 작업 | 상세 |
|------|------|
| 테넌트 설정 페이지 | /admin/settings (이름 변경, 사용량 표시) |
| 다크모드 | 전체 페이지 다크모드 검수 |
| i18n | ko.json, en.json 번역 완성 |
| 반응형 | 사이드바 모바일 대응 (접기/펼치기) |
| 에러 페이지 | 404, 403, 500 에러 페이지 |
| 로딩 상태 | 스켈레톤 UI 또는 스피너 |
| Toast 알림 | 성공/실패 피드백 |

### 11-2. 백엔드 마무리

| 작업 | 상세 |
|------|------|
| Rate limiting | 테넌트 단위 API 요청 제한 |
| PostgreSQL RLS | 테넌트 격리 DB 레벨 안전망 |
| 에러 핸들링 | GlobalExceptionHandler 정리 |
| API 문서 | Swagger/SpringDoc 설정 |
| CORS | 서브도메인 허용 설정 |

### 11-3. 테스트/문서화

| 작업 | 상세 |
|------|------|
| 통합 테스트 | 인증, 테넌트 격리, 권한 체크 |
| /etc/hosts 가이드 | 로컬 서브도메인 설정 방법 |
| README | 프로젝트 소개, 기술 스택, 실행 방법 |
| 시드 데이터 | 개발용 샘플 데이터 (테넌트 2개, 멤버 각 3명, 노트 10개) |

---

## Flyway 마이그레이션 계획

| 버전 | Phase | 테이블 |
|------|-------|--------|
| V1 | Phase 2 | USERS, REFRESH_TOKENS |
| V2 | Phase 3 | TENANTS, TENANT_MEMBERS |
| V3 | Phase 4 | INVITATIONS |
| V4 | Phase 6 | NOTES, TAGS, NOTE_TAGS |
| V5 | Phase 7 | NOTE_SHARES, NOTE_PUBLIC_LINKS, COMMENTS |
| V6 | Phase 8 | NOTIFICATIONS |
| V7 | Phase 9 | ACTIVITY_HISTORY |

---

## Phase별 완성 API 수

| Phase | 누적 API 수 | 추가 API |
|-------|------------|----------|
| Phase 2 | 5 | 인증 5 |
| Phase 3 | 12 | 테넌트 2 + 슈퍼어드민 5 |
| Phase 4 | 18 | 초대 3 + 회원가입 2 + 슈퍼어드민 초대 1 |
| Phase 5 | 21 | 멤버 3 |
| Phase 6 | 27 | 노트 5 + 태그 1 |
| Phase 7 | 36 | 공유 5 + 댓글 4 |
| Phase 8 | 41 | 알림 5 |
| Phase 9 | 42 | 이력 1 |

최종: 42개 엔드포인트 (v2 기준 40 + Phase 진행 중 추가분)
