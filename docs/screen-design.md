# Multi-Tenant Notes App - 화면 설계서 v2

## 페이지 목록 (16개)

| 구분 | 경로 | 페이지 | 권한 |
|------|------|--------|------|
| 공개 | /login | 로그인 | 없음 |
| 공개 | /invite/[token] | 초대 회원가입 | 없음 |
| 공개 | /public/notes/[shareToken] | 외부 공유 노트 | 없음 |
| 테넌트 | /select-tenant | 테넌트 선택 | 인증만 |
| 테넌트 | / | 대시보드 | O/A/M |
| 테넌트 | /notes | 노트 목록 | O/A/M |
| 테넌트 | /notes/new | 노트 작성 | O/A/M |
| 테넌트 | /notes/[noteId] | 노트 상세 | 접근 권한자 |
| 테넌트 | /notes/[noteId]/edit | 노트 편집 | 작성자/O/A |
| 테넌트 | /admin/members | 멤버 관리 | O/A |
| 테넌트 | /admin/invitations | 초대 관리 | O/A |
| 테넌트 | /admin/settings | 테넌트 설정 | O |
| 테넌트 | /admin/activities | 활동 이력 | O/A |
| 슈퍼어드민 | /super-admin | 어드민 대시보드 | SA |
| 슈퍼어드민 | /super-admin/tenants | 테넌트 관리 | SA |
| 슈퍼어드민 | /super-admin/tenants/[tenantId] | 테넌트 상세 | SA |

---

## 레이아웃

### AuthLayout

로그인, 회원가입, 테넌트 선택 페이지에 사용.

```
┌──────────────────────────────────────┐
│              로고 + 앱 이름            │
│                                      │
│         ┌──────────────────┐         │
│         │                  │         │
│         │    폼 카드 영역    │         │
│         │                  │         │
│         └──────────────────┘         │
│                                      │
│        다크모드 토글 / 언어 전환       │
└──────────────────────────────────────┘
```

- 중앙 정렬된 카드 형태
- 하단에 다크모드 토글 + 언어 전환 (로그인 전에도 사용 가능)

### MainLayout

인증 후 모든 테넌트 페이지에 사용.

```
┌─────────────────────┬──────────────────────────────────┐
│ 사이드바 (240px)     │ 헤더                              │
│                     │ ┌──────────────────────────────┐ │
│ ┌─────────────────┐ │ │ 대시보드 > 노트 > 회의록       │ │
│ │ 🏢 팀 알파       │ │ └──────────────────────────────┘ │
│ │                 │ ├──────────────────────────────────┤
│ │ 👤 홍길동    🔔3 │ │                                  │
│ └─────────────────┘ │                                  │
│                     │                                  │
│ 네비게이션           │         메인 콘텐츠 영역           │
│ ● 대시보드           │                                  │
│ ○ 노트              │                                  │
│ ─── 관리 ────       │                                  │
│ ○ 멤버 관리         │                                  │
│ ○ 초대 관리         │                                  │
│ ○ 활동 이력         │                                  │
│ ○ 설정              │                                  │
│                     │                                  │
│                     │                                  │
│ ┌─────────────────┐ │                                  │
│ │ 🌙/☀ │ KO/EN   │ │                                  │
│ │ ───── FREE ──── │ │                                  │
│ └─────────────────┘ │                                  │
└─────────────────────┴──────────────────────────────────┘
```

사이드바 상단:
- 테넌트 이름 (아이콘 또는 이니셜)
- 유저 이름 + 아바타 (클릭 시 드롭다운: 내 정보, 테넌트 전환, 로그아웃)
- 알림 벨 아이콘 + 미읽 카운트 뱃지 (클릭 시 알림 팝오버)

사이드바 중간:
- 네비게이션 메뉴
- "관리" 섹션 구분선 (OWNER/ADMIN만 표시)
- 현재 페이지 하이라이트

사이드바 하단:
- 다크모드 토글 (아이콘 버튼)
- 언어 전환 (KO/EN 토글)
- 플랜 배지 (FREE/PRO)

헤더:
- 브레드크럼 (페이지 뎁스 표시)
- 예: 대시보드 > 노트 > 회의록
- 각 뎁스는 클릭 가능한 링크

### SuperAdminLayout

슈퍼어드민 전용. MainLayout과 동일한 구조.

```
사이드바 상단:
- "Super Admin" 표시
- 유저 이름 + 아바타

사이드바 메뉴:
- 대시보드
- 테넌트 관리

사이드바 하단:
- 다크모드 토글
- 언어 전환
```

---

## 공통 컴포넌트 상세

### 알림 팝오버

사이드바의 벨 아이콘 클릭 시 표시.

```
┌────────────────────────────┐
│ 알림           [전체 읽음]  │
│────────────────────────────│
│ 🔵 홍길동님이 '회의록'      │
│    노트를 공유했습니다       │
│    3분 전                  │
│────────────────────────────│
│ 🔵 김철수님이 '회의록'에     │
│    댓글을 남겼습니다         │
│    1시간 전                │
│────────────────────────────│
│    역할이 MEMBER에서        │
│    ADMIN으로 변경되었습니다   │
│    어제                    │
│────────────────────────────│
│       전체 알림 보기 →      │
└────────────────────────────┘
```

| 요소 | shadcn 컴포넌트 | 설명 |
|------|----------------|------|
| 팝오버 컨테이너 | Popover | 벨 아이콘 클릭 시 |
| 전체 읽음 버튼 | Button (ghost) | PUT /api/notifications/read-all |
| 알림 항목 | 커스텀 | 미읽: 파란 점 표시, 클릭 시 읽음 처리 + 해당 페이지 이동 |
| 전체 알림 보기 | 링크 | 별도 알림 전체 페이지 대신 팝오버에서 스크롤 |

API 호출:
- GET /api/notifications/unread-count (사이드바 뱃지, 폴링 또는 페이지 전환 시)
- GET /api/notifications?size=10 (팝오버 열 때)
- PUT /api/notifications/{id}/read (알림 클릭 시)
- PUT /api/notifications/read-all (전체 읽음)

### 유저 드롭다운

사이드바 상단의 유저 이름/아바타 클릭 시 표시.

```
┌──────────────────┐
│ 👤 홍길동         │
│ owner@example.com│
│──────────────────│
│ 내 정보 수정      │
│ 테넌트 전환       │
│──────────────────│
│ 로그아웃          │
└──────────────────┘
```

| 요소 | shadcn 컴포넌트 | 설명 |
|------|----------------|------|
| 드롭다운 | DropdownMenu | |
| 내 정보 수정 | DropdownMenuItem | Dialog 열기 |
| 테넌트 전환 | DropdownMenuItem | /select-tenant 이동 (2개 이상 소속 시만 표시) |
| 로그아웃 | DropdownMenuItem | POST /api/auth/logout |

### 브레드크럼

헤더 영역. 현재 페이지 위치를 계층적으로 표시.

```
대시보드                        ← / 페이지
대시보드 > 노트                  ← /notes
대시보드 > 노트 > 회의록          ← /notes/[noteId]
대시보드 > 노트 > 새 노트         ← /notes/new
관리 > 멤버 관리                  ← /admin/members
관리 > 설정                      ← /admin/settings
```

| 요소 | shadcn 컴포넌트 | 설명 |
|------|----------------|------|
| 브레드크럼 | Breadcrumb (커스텀) | 각 뎁스 클릭 시 해당 페이지 이동 |
| 구분자 | > 또는 / | |
| 현재 페이지 | 텍스트 (비활성) | 마지막 항목은 링크 아님 |

---

## 페이지 상세

### 1. 로그인 (/login)

레이아웃: AuthLayout

```
┌─────────────────────────┐
│        로그인             │
│                         │
│  이메일    [           ]  │
│  비밀번호  [           ]  │
│                         │
│  [      로그인 버튼     ]  │
│                         │
└─────────────────────────┘
```

| 요소 | shadcn 컴포넌트 |
|------|----------------|
| 이메일 입력 | Input |
| 비밀번호 입력 | Input |
| 로그인 버튼 | Button |
| 에러 메시지 | Alert |

API: POST /api/auth/login → 성공 시 /select-tenant 이동 (1개 테넌트면 바로 해당 테넌트)

### 2. 테넌트 선택 (/select-tenant)

레이아웃: AuthLayout

```
┌─────────────────────────────┐
│      테넌트를 선택하세요       │
│                             │
│  ┌───────────────────────┐  │
│  │ 팀 알파       OWNER   │  │
│  │ team-alpha    PRO     │  │
│  └───────────────────────┘  │
│                             │
│  ┌───────────────────────┐  │
│  │ 팀 베타       MEMBER  │  │
│  │ team-beta     FREE    │  │
│  └───────────────────────┘  │
└─────────────────────────────┘
```

| 요소 | shadcn 컴포넌트 |
|------|----------------|
| 테넌트 카드 | Card |
| 역할/플랜 | Badge |

API: 로그인 응답의 tenants 배열 사용 → 선택 시 {subdomain}.localhost:3000 리다이렉트

### 3. 초대 회원가입 (/invite/[token])

레이아웃: AuthLayout

```
┌─────────────────────────────┐
│   "팀 알파"에 초대되었습니다    │
│   초대자: 홍길동 / 역할: MEMBER │
│                             │
│  이메일    [              ]   │
│  이름     [              ]   │
│  비밀번호  [              ]   │
│  비밀번호 확인 [           ]   │
│                             │
│  [       가입하기           ]  │
│                             │
│  이미 계정이 있나요? 로그인     │
└─────────────────────────────┘
```

API:
- SSR: GET /api/auth/invite/{token}
- POST /api/auth/signup → 자동 로그인 → 테넌트 이동

### 4. 대시보드 (/)

레이아웃: MainLayout
브레드크럼: 대시보드

```
┌─────────────────────────────────────┐
│  ┌─────────┐ ┌─────────┐ ┌────────┐ │
│  │ 내 노트  │ │ 공유받은 │ │ 멤버 수 │ │
│  │   12개   │ │   5개   │ │  4명   │ │
│  └─────────┘ └─────────┘ └────────┘ │
│                                     │
│  최근 노트                           │
│  ┌─────────────────────────────────┐ │
│  │ 회의록           3분 전    회의  │ │
│  │ 프로젝트 계획서   1시간 전  기획  │ │
│  │ API 스펙 정리     어제     개발  │ │
│  └─────────────────────────────────┘ │
│                                     │
│  최근 활동                (O/A만)    │
│  ┌─────────────────────────────────┐ │
│  │ 홍길동이 "회의록" 노트를 수정함   │ │
│  │ 김철수가 "API 스펙" 에 댓글 작성  │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

API:
- GET /api/notes?size=5&sort=updatedAt,desc
- GET /api/activities?size=5 (O/A만)
- GET /api/tenant

### 5. 노트 목록 (/notes)

레이아웃: MainLayout
브레드크럼: 대시보드 > 노트

```
┌─────────────────────────────────────────┐
│  노트                     [+ 새 노트]   │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 키워드 [          ]  태그 [▼ 전체]   │ │
│  │ 작성자 [▼ 전체]      [검색] [초기화]  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  검색 결과 (45건)                        │
│  ┌─────────────────────────────────────┐ │
│  │ 제목      작성자   태그   작성일     │ │
│  │─────────────────────────────────────│ │
│  │ 회의록    홍길동   회의   03-13     │ │
│  │ API 스펙  김철수   개발   03-12     │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  < 1 2 3 4 5 >                          │
└─────────────────────────────────────────┘
```

| 요소 | shadcn 컴포넌트 |
|------|----------------|
| 검색 키워드 | Input |
| 태그/작성자 필터 | Select |
| 결과 테이블 | Table |
| 태그 | Badge |
| 페이징 | 커스텀 |

API:
- SSR: GET /api/tags, GET /api/members
- GET /api/notes?keyword=&tagId=&authorId=&page=0&size=20

컴포넌트: NoteSearchPanel, NoteSearchTable

### 6. 노트 작성 (/notes/new)

레이아웃: MainLayout
브레드크럼: 대시보드 > 노트 > 새 노트

```
┌─────────────────────────────────────┐
│  새 노트                [저장] [취소] │
│                                     │
│  제목                               │
│  [                               ]  │
│                                     │
│  태그                               │
│  [회의 ×] [개발 ×] [+ 태그 추가   ]  │
│                                     │
│  내용                               │
│  ┌─────────────────────────────────┐ │
│  │                                 │ │
│  │      텍스트 에디터 영역           │ │
│  │                                 │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

API:
- GET /api/tags
- POST /api/notes → /notes/{noteId} 이동

컴포넌트: NoteEditor

### 7. 노트 상세 (/notes/[noteId])

레이아웃: MainLayout
브레드크럼: 대시보드 > 노트 > {노트 제목}

```
┌─────────────────────────────────────────┐
│  회의록                 [편집] [삭제]    │
│  홍길동 · 2026-03-13 10:00              │
│  [회의] [프로젝트A]                      │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │         노트 본문 내용               │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  공유                                    │
│  ┌─────────────────────────────────────┐ │
│  │ 멤버 공유: 김철수, 이영희  [+ 공유]   │ │
│  │ 외부 링크: https://...  [복사] [해제] │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  댓글 (3)                                │
│  ┌─────────────────────────────────────┐ │
│  │ 김철수 · 1시간 전         [수정][삭제]│ │
│  │ 좋은 내용이네요!                     │ │
│  │─────────────────────────────────────│ │
│  │ 이영희 · 30분 전                     │ │
│  │ 다음 회의 때 참고하겠습니다            │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  [댓글 입력                    ] [작성]   │
└─────────────────────────────────────────┘
```

| 요소 | shadcn 컴포넌트 |
|------|----------------|
| 편집/삭제 | Button |
| 삭제 확인 | AlertDialog |
| 공유 추가 | Dialog |
| 외부 링크 | Dialog |
| 댓글 액션 | DropdownMenu |
| 댓글 입력 | Textarea + Button |

API:
- SSR: GET /api/notes/{noteId}
- GET /api/notes/{noteId}/comments
- POST /api/notes/{noteId}/comments
- PUT/DELETE /api/notes/{noteId}/comments/{commentId}
- POST /api/notes/{noteId}/shares
- DELETE /api/notes/{noteId}/shares/{userId}
- POST/DELETE /api/notes/{noteId}/public-link
- DELETE /api/notes/{noteId}

컴포넌트: NoteDisplay, NoteSharePanel, CommentList, CommentEditor, MemberShareDialog, PublicLinkDialog

### 8. 노트 편집 (/notes/[noteId]/edit)

레이아웃: MainLayout
브레드크럼: 대시보드 > 노트 > {노트 제목} > 편집
NoteEditor 컴포넌트 재사용, 기존 데이터 로드.

API:
- SSR: GET /api/notes/{noteId}
- GET /api/tags
- PUT /api/notes/{noteId} → /notes/{noteId} 이동

### 9. 외부 공유 노트 (/public/notes/[shareToken])

레이아웃: 없음 (독립 페이지, 하단에 다크모드/언어 전환)

```
┌─────────────────────────────────────┐
│  [앱 로고]                           │
│                                     │
│  회의록                              │
│  팀 알파 · 홍길동 · 2026-03-13       │
│                                     │
│  ┌─────────────────────────────────┐ │
│  │         노트 본문 내용           │ │
│  └─────────────────────────────────┘ │
│                                     │
│  이 노트는 "팀 알파"에서 공유되었습니다 │
│                                     │
│        🌙/☀  │  KO/EN              │
└─────────────────────────────────────┘
```

API: SSR: GET /api/public/notes/{shareToken}

### 10. 멤버 관리 (/admin/members)

레이아웃: MainLayout
브레드크럼: 관리 > 멤버 관리

```
┌─────────────────────────────────────────┐
│  멤버 관리                   [+ 초대]    │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 키워드 [        ]  역할 [▼ 전체]     │ │
│  │                    [검색] [초기화]    │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 이름     이메일      역할   상태  ⋮  │ │
│  │──────────────────────────────────── │ │
│  │ 홍길동  owner@...   OWNER  활성     │ │
│  │ 김철수  admin@...   ADMIN  활성  ⋮  │ │
│  │ 이영희  member@...  MEMBER 활성  ⋮  │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

API:
- GET /api/members?keyword=&role=&page=0&size=20
- PUT /api/members/{memberId}/role
- PUT /api/members/{memberId}/status

컴포넌트: MemberSearchPanel, MemberSearchTable, RoleChangeDialog

### 11. 초대 관리 (/admin/invitations)

레이아웃: MainLayout
브레드크럼: 관리 > 초대 관리

```
┌─────────────────────────────────────────┐
│  초대 관리               [+ 초대 생성]   │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 역할    상태     초대자   만료일  ⋮   │ │
│  │──────────────────────────────────── │ │
│  │ MEMBER  대기중   홍길동   03-20   ⋮  │ │
│  │ ADMIN   수락됨   홍길동   03-18      │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  초대 생성 다이얼로그                      │
│  ┌─────────────────────────┐             │
│  │ 역할 선택: ADMIN / MEMBER│             │
│  │ [생성]  [취소]           │             │
│  │                         │             │
│  │ 생성된 링크:             │             │
│  │ [http://...      ] [복사]│             │
│  └─────────────────────────┘             │
└─────────────────────────────────────────┘
```

API:
- GET /api/invitations
- POST /api/invitations
- DELETE /api/invitations/{invitationId}

컴포넌트: InvitationTable, InvitationCreateDialog

### 12. 테넌트 설정 (/admin/settings)

레이아웃: MainLayout
브레드크럼: 관리 > 설정

```
┌─────────────────────────────────────┐
│  테넌트 설정                         │
│                                     │
│  기본 정보                           │
│  ┌─────────────────────────────────┐ │
│  │ 테넌트 이름  [팀 알파        ]    │ │
│  │ 서브도메인   team-alpha (변경불가) │ │
│  │ 플랜        PRO (슈퍼어드민 관리) │ │
│  │                        [저장]    │ │
│  └─────────────────────────────────┘ │
│                                     │
│  사용량                              │
│  ┌─────────────────────────────────┐ │
│  │ 노트    45 / 무제한              │ │
│  │ ████████████████░░░░  80%       │ │
│  │ 멤버    4 / 50명                 │ │
│  │ ████░░░░░░░░░░░░░░░  8%        │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

API:
- SSR: GET /api/tenant
- PUT /api/tenant

### 13. 활동 이력 (/admin/activities)

레이아웃: MainLayout
브레드크럼: 관리 > 활동 이력

```
┌─────────────────────────────────────────┐
│  활동 이력                               │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 유형 [▼ 전체]  액션 [▼ 전체]  [검색]  │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 홍길동  NOTE 생성    "회의록"  10:00  │ │
│  │ 김철수  COMMENT 생성  "API.."  09:30  │ │
│  │ 홍길동  NOTE 수정    "계획서"  09:00  │ │
│  └─────────────────────────────────────┘ │
│  < 1 2 3 >                              │
└─────────────────────────────────────────┘
```

API: GET /api/activities?entityType=&action=&page=0&size=20

### 14. 슈퍼어드민 대시보드 (/super-admin)

레이아웃: SuperAdminLayout
브레드크럼: 대시보드

```
┌─────────────────────────────────────────┐
│  ┌────────┐ ┌────────┐ ┌──────────────┐ │
│  │ 테넌트  │ │ 사용자  │ │  플랜 분포    │ │
│  │  10개   │ │  45명   │ │ FREE:6 PRO:4 │ │
│  └────────┘ └────────┘ └──────────────┘ │
│                                         │
│  최근 생성된 테넌트                       │
│  ┌─────────────────────────────────────┐ │
│  │ 팀 오메가    PRO    5명    03-12    │ │
│  │ 팀 감마      FREE   2명    03-10    │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

API: GET /api/super-admin/dashboard

### 15. 테넌트 관리 (/super-admin/tenants)

레이아웃: SuperAdminLayout
브레드크럼: 대시보드 > 테넌트 관리

```
┌─────────────────────────────────────────┐
│  테넌트 관리              [+ 테넌트 생성] │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 키워드 [      ] 플랜 [▼] 상태 [▼]   │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 이름    서브도메인  플랜  멤버  노트  │ │
│  │──────────────────────────────────── │ │
│  │ 팀 알파 team-alpha PRO   4    45   │ │
│  │ 팀 베타 team-beta  FREE  2    10   │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

API:
- GET /api/super-admin/tenants?keyword=&plan=&status=&page=0&size=20
- POST /api/super-admin/tenants

### 16. 테넌트 상세 (/super-admin/tenants/[tenantId])

레이아웃: SuperAdminLayout
브레드크럼: 대시보드 > 테넌트 관리 > {테넌트 이름}

```
┌─────────────────────────────────────────┐
│  팀 알파                                 │
│                                         │
│  ┌─────────────────────────────────────┐ │
│  │ 서브도메인: team-alpha               │ │
│  │ 플랜: PRO [변경]                     │ │
│  │ 상태: ACTIVE [비활성화]               │ │
│  │ 생성일: 2026-01-01                   │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  사용량                                  │
│  ┌─────────────────────────────────────┐ │
│  │ 노트: 45 / 무제한                    │ │
│  │ 멤버: 4 / 50명                       │ │
│  └─────────────────────────────────────┘ │
│                                         │
│  OWNER: 홍길동 (owner@example.com)       │
│                                         │
│  초대 링크 발급              [+ 초대]     │
│  ┌─────────────────────────────────────┐ │
│  │ (이 테넌트의 초대 목록)               │ │
│  └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

API:
- SSR: GET /api/super-admin/tenants/{tenantId}
- PUT /api/super-admin/tenants/{tenantId}
- POST /api/super-admin/tenants/{tenantId}/invitations

---

## 컴포넌트 목록

### 레이아웃 (layouts/)

| 컴포넌트 | 설명 |
|----------|------|
| MainLayout | 사이드바 + 헤더 |
| AuthLayout | 중앙 정렬 카드 |
| SuperAdminLayout | 슈퍼어드민용 |
| Sidebar | 테넌트 정보, 유저 메뉴, 알림, 네비, 다크모드/언어/플랜 |
| Header | 브레드크럼 |
| NotificationPopover | 알림 팝오버 |
| UserDropdown | 유저 드롭다운 메뉴 |
| Breadcrumb | 페이지 뎁스 표시 |
| ThemeToggle | 다크모드 전환 |
| LanguageToggle | 언어 전환 |

### 도메인 컴포넌트 (components/)

| 컴포넌트 | 폴더 | 사용 페이지 |
|----------|------|------------|
| NoteSearchPanel | search/ | /notes |
| NoteSearchTable | search/ | /notes |
| NoteEditor | editor/ | /notes/new, /notes/[id]/edit |
| NoteDisplay | display/ | /notes/[id] |
| NoteSharePanel | display/ | /notes/[id] |
| CommentList | display/ | /notes/[id] |
| CommentEditor | editor/ | /notes/[id] |
| MemberShareDialog | editor/ | /notes/[id] |
| PublicLinkDialog | editor/ | /notes/[id] |
| MemberSearchPanel | search/ | /admin/members |
| MemberSearchTable | search/ | /admin/members |
| RoleChangeDialog | editor/ | /admin/members |
| InvitationTable | search/ | /admin/invitations |
| InvitationCreateDialog | editor/ | /admin/invitations |
| NotificationItem | display/ | NotificationPopover |
| PlanBadge | badge/ | 사이드바, 여러 페이지 |
| RoleBadge | badge/ | 여러 페이지 |
| StatusBadge | badge/ | 여러 페이지 |
| ActionBadge | badge/ | /admin/activities |
| NoteLink | link/ | 여러 페이지 |
| TenantCard | display/ | /select-tenant |

### Zustand 스토어 (stores/)

| 스토어 | 용도 |
|--------|------|
| useTokenInfoStore | JWT 토큰 정보 |
| useAccountStore | 현재 사용자 계정 |
| useTenantStore | 현재 테넌트 정보 + 역할 |
| useNotificationStore | 미읽 알림 카운트 |
| useSideMenuStore | 사이드 메뉴 열림/닫힘 |

---

## 페이지-API 매핑 요약

| 페이지 | SSR API | 클라이언트 API |
|--------|---------|---------------|
| /login | - | auth/login |
| /select-tenant | - | - |
| /invite/[token] | auth/invite/{token} | auth/signup |
| /public/notes/[token] | public/notes/{token} | - |
| / | tenant, notes, activities | - |
| /notes | tags, members | notes |
| /notes/new | tags | notes (생성) |
| /notes/[id] | notes/{id} | comments, shares, public-link |
| /notes/[id]/edit | notes/{id}, tags | notes (수정) |
| /admin/members | members | members (역할/상태) |
| /admin/invitations | invitations | invitations (생성/취소) |
| /admin/settings | tenant | tenant (수정) |
| /admin/activities | activities | activities (필터) |
| /super-admin | super-admin/dashboard | - |
| /super-admin/tenants | super-admin/tenants | super-admin/tenants (생성) |
| /super-admin/tenants/[id] | super-admin/tenants/{id} | super-admin (수정/초대) |

공통 (모든 테넌트 페이지):
- GET /api/notifications/unread-count (사이드바 뱃지, 페이지 전환마다)
- GET /api/notifications (팝오버 열 때)
- PUT /api/notifications/{id}/read (알림 클릭)
- PUT /api/notifications/read-all (전체 읽음)
