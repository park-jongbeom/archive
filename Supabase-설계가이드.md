# Supabase 설계 가이드 for AOS 6기

SNS/커뮤니티 앱을 Supabase(PostgreSQL)로 설계할 때의 **테이블·컬럼 구조**, **정규화**, **트리거·Edge Functions 활용** 요약입니다.

---

## Supabase란?

**Supabase**는 오픈소스 **BaaS(Backend as a Service)** 로, **PostgreSQL**을 기반으로 인증(Auth), 실시간(Realtime), 스토리지(Storage), Edge Functions 등을 제공합니다. **관계형 DB(테이블·행·열)** 로 데이터를 다루며, 외래키와 JOIN으로 테이블 간 관계를 유지합니다.

- **실시간**: Realtime 구독으로 테이블 변경 시 클라이언트에 자동 반영되어, 채팅·피드에 활용할 수 있습니다.
- **SQL·JOIN**: 테이블 간 관계를 **외래키(FK)** 로 두고 JOIN으로 조회합니다. 작성자·사용자 정보는 **profiles와 JOIN**하여 항상 최신 값을 사용합니다.
- **RLS(Row Level Security)**: 행 단위 보안 정책으로 “본인 데이터만 수정” 등 제어가 가능합니다.
- **확장성**: 관리형 PostgreSQL이므로 서버 구축 부담이 적고, 트래픽에 맞게 확장할 수 있습니다.

이 가이드는 **정규화**를 전제로, SNS/커뮤니티 앱에서 **어떤 테이블을 두고, FK로 어떻게 참조할지**, **트리거·Edge Functions로 무엇을 처리할지**를 정리한 내용입니다.

---

## 1. Supabase(PostgreSQL) 기본 개념

| 구분 | 설명 |
|------|------|
| 구조 | **테이블** → **행(row)** → **열(column)** |
| 관계 | **외래키(FK)** 로 테이블 간 참조, **JOIN**으로 조회 |
| 정규화 | 한 가지 정보는 한 테이블에만 두고, 나머지는 FK로 참조. 단일 진실 소스 유지, 수정 시 한 곳만 갱신 |

설계 시 정하는 것: **어떤 테이블을 둘지**, **각 테이블에 어떤 컬럼을 넣을지**, **어떤 FK 관계를 둘지**.

---

## 2. 전체 구조 (정규화)

사용자 정보(이름, 프로필 이미지)는 **profiles**에만 두고, posts·comments·likes·follows는 **FK만** 갖습니다. 조회 시 **JOIN**으로 profiles와 연결해 작성자·대상 정보를 가져옵니다. 단일 진실 소스가 유지되고, 프로필 수정 시 별도 반영 없이 항상 최신이 조회됩니다.

| 용도 | 테이블 | 비고 |
|------|--------|------|
| 사용자 프로필 | `profiles` (또는 `users`) | Auth와 연동. **단일 진실 소스**. 이름·프로필 이미지는 여기만 보관 |
| 게시글 | `posts` | author_id FK → profiles. 작성자 정보는 JOIN으로 조회 |
| 댓글 | `comments` | post_id FK, user_id FK → profiles. 작성자 정보는 JOIN으로 조회 |
| 좋아요 | `likes` | post_id, user_id FK. 복합 PK로 중복 방지 |
| 팔로우 | `follows` | follower_id, following_id FK → profiles. 복합 PK로 중복 방지 |

알림이 필요하면 `notifications` 테이블을 추가해, 트리거 또는 Edge Functions에서 삽입·푸시와 연동할 수 있습니다.

**미디어 파일**: 이미지·영상은 **Supabase Storage**(또는 팀이 정한 객체 저장소)에 두고, 테이블에는 **URL** 또는 스토리지 **경로**만 두는 구성이 일반적이다. 버킷·RLS는 Supabase 콘솔에서 설정한다.

---

## 3. 테이블·컬럼 설계

주 키는 `id`(uuid) 사용을 권장하며, Supabase Auth와 연동할 때 `auth.uid()`와 매핑합니다. 시각 필드(created_at 등)는 **timestamptz** 타입 사용을 권장합니다.

### profiles

Auth 사용자와 1:1로 두는 프로필 테이블. Supabase에서는 `auth.users`와 연동해 `profiles.id = auth.uid()`로 둘 수 있습니다.

| 구분 | 컬럼 | 타입 | 설명 |
|------|------|------|------|
| 상위 | id | uuid, PK | Auth uid와 동일 |
| 상위 | display_name | text | 표시 이름 |
| 상위 | profile_image_url | text | 프로필 이미지 URL |
| 상위 | created_at | timestamptz | 가입 시각 |

```json
{
  "id": "uuid-123",
  "display_name": "홍길동",
  "profile_image_url": "https://...",
  "created_at": "2024-01-15T10:00:00Z"
}
```

### posts

작성자 이름·프로필 이미지는 저장하지 않고, **author_id** FK로만 참조. 조회 시 `posts` JOIN `profiles` 로 가져옵니다.

| 구분 | 컬럼 | 타입 | 설명 |
|------|------|------|------|
| 상위 | id | uuid, PK | 게시글 ID |
| 상위 | author_id | uuid, FK → profiles(id) | 작성자 (JOIN으로 이름·프로필 조회) |
| 상위 | content | text | 본문 텍스트 |
| 상위 | image_urls | text[] | 이미지 URL 배열 |
| 상위 | created_at | timestamptz | 작성 시각 |

like_count, comment_count는 **집계 시 서브쿼리 또는 VIEW**로 계산할 수 있습니다. 조회 빈도가 매우 높을 때만 선택적으로 컬럼을 두고 트리거로 유지할 수 있습니다.

```json
{
  "id": "uuid-post-1",
  "author_id": "uuid-123",
  "content": "본문 텍스트",
  "image_urls": ["https://..."],
  "created_at": "2024-01-15T10:00:00Z"
}
```

쿼리 예 (작성자 정보 포함): `select p.*, pr.display_name as author_name, pr.profile_image_url as author_profile_image_url from posts p join profiles pr on p.author_id = pr.id where p.author_id = $1`.

### comments

| 구분 | 컬럼 | 타입 | 설명 |
|------|------|------|------|
| 상위 | id | uuid, PK | 댓글 ID |
| 상위 | post_id | uuid, FK → posts(id) | 게시글 |
| 상위 | user_id | uuid, FK → profiles(id) | 댓글 작성자 (JOIN으로 이름·프로필 조회) |
| 상위 | content | text | 댓글 내용 |
| 상위 | created_at | timestamptz | 작성 시각 |

```json
{
  "id": "uuid-comment-1",
  "post_id": "uuid-post-1",
  "user_id": "uuid-456",
  "content": "댓글 내용",
  "created_at": "2024-01-15T11:00:00Z"
}
```

쿼리 예 (작성자 정보 포함): `select c.*, pr.display_name as user_name, pr.profile_image_url as user_profile_image_url from comments c join profiles pr on c.user_id = pr.id where c.post_id = $1`.

### likes

post_id + user_id 복합 PK로 한 사용자가 한 게시글에 한 번만 좋아요 가능. 사용자 정보는 JOIN으로 조회합니다.

| 구분 | 컬럼 | 타입 | 설명 |
|------|------|------|------|
| 상위 | post_id | uuid, FK → posts(id), PK | 게시글 |
| 상위 | user_id | uuid, FK → profiles(id), PK | 좋아요 누른 사람 (JOIN으로 조회) |
| 상위 | created_at | timestamptz | 좋아요 시각 (선택) |

```json
{
  "post_id": "uuid-post-1",
  "user_id": "uuid-456",
  "created_at": "2024-01-15T11:30:00Z"
}
```

### follows

follower_id + following_id 복합 PK로 중복 팔로우 방지. 팔로우 대상 정보는 JOIN으로 조회합니다.

| 구분 | 컬럼 | 타입 | 설명 |
|------|------|------|------|
| 상위 | follower_id | uuid, FK → profiles(id), PK | 팔로우하는 사람(나) |
| 상위 | following_id | uuid, FK → profiles(id), PK | 팔로우 대상 (JOIN으로 이름·프로필 조회) |
| 상위 | created_at | timestamptz | 팔로우 시각 (선택) |

```json
{
  "follower_id": "uuid-123",
  "following_id": "uuid-789",
  "created_at": "2024-01-15T12:00:00Z"
}
```

쿼리 예 (팔로우 대상 정보 포함): `select f.*, pr.display_name as following_name, pr.profile_image_url as following_profile_image_url from follows f join profiles pr on f.following_id = pr.id where f.follower_id = $1`.

---

## 4. 프로필 변경 시

**정규화된 설계**에서는 프로필(이름, 프로필 이미지)이 **profiles** 한 곳에만 있으므로, 프로필 수정 시 **profiles만 UPDATE**하면 됩니다. posts·comments·likes·follows에는 이름·이미지를 저장하지 않기 때문에 별도 반영이 필요 없고, 모든 조회가 **JOIN**으로 profiles와 연결되므로 **항상 최신 정보**가 반영됩니다.

---

## 5. 트리거·Edge Functions로 처리하기 좋은 기능

| 기능 | 권장 처리 | 이유 |
|------|------------|------|
| like_count / comment_count 유지 | DB 트리거 또는 VIEW | 집계는 VIEW(서브쿼리)로 조회 가능. 조회 부하가 클 때만 트리거로 posts에 count 컬럼 유지 |
| 알림 생성·푸시 발송 | 트리거 + Edge Functions | DB에서 알림 행 삽입, Edge에서 FCM 등 푸시 발송 |
| 글/댓글 삭제 전파 | CASCADE 또는 트리거 | FK ON DELETE CASCADE 또는 트리거로 정합성 유지 |
| 신고·스팸·콘텐츠 검증 | Edge Functions | 서버에서만 검증, 우회 어렵게 |
| 이메일 발송 | Edge Functions | 클라이언트에 메일 발송 권한 두지 않음 |
| 통계·스케줄 | pg_cron 또는 Edge Functions | 주기 집계, 오래된 데이터 정리 |
| 외부 API 연동 | Edge Functions | API 키·비밀을 서버에만 두기 위함 |
| 권한 반영 | RLS + 트리거 | Auth 역할 등 반영 |

---

## 6. 추가 고려사항

- **RLS(Row Level Security)**: profiles는 본인만, posts는 작성자(author_id)만 수정/삭제, comments/likes는 본인만 삭제, follows는 본인(follower_id)만 생성/삭제 등으로 정책 설정. 예: `create policy "posts_update_own" on posts for update using (auth.uid() = author_id);`
- **인덱스**: author_id, post_id, user_id, created_at 등 조회·정렬에 자주 쓰는 컬럼에 인덱스 추가. 복합 조건이면 복합 인덱스 고려.
- **페이지네이션**: `limit` + `offset` 또는 `where id > $last_id order by id limit N` 커서 기반.
- **Realtime**: Supabase Realtime으로 특정 테이블 변경 구독 시 실시간 UI 반영 가능.
- **VIEW**: 피드·댓글 목록처럼 “posts + author 정보”를 자주 쓰면 VIEW로 정의해 쿼리를 단순화할 수 있습니다.

---

## 7. 요약

- **구조**: profiles(단일 진실 소스), posts·comments·likes·follows는 FK만 두고 작성자/대상 정보는 **JOIN**으로 조회.
- **지향점**: 정규화로 데이터 중복 없이 유지, 프로필 수정 시 한 곳만 갱신.
- **프로필 변경**: profiles만 수정하면 되며, JOIN으로 항상 최신 반영.
- **추가**: RLS, 인덱스, 페이지네이션, Realtime, VIEW·트리거는 설계 시 함께 고려.

---

**작성자** 김갑석 · **작성일** 2026-03-16

---

*이 자료는 멋쟁이사자처럼 부트캠프 학습 목적으로 제공됩니다.*
