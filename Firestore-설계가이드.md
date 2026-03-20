# Firestore 설계 가이드 for AOS 6기

SNS/커뮤니티 앱을 Firestore로 설계할 때의 **컬렉션·문서 구조**, **비정규화**, **Functions 활용** 요약입니다.

---

## Firestore란?

**Cloud Firestore**는 Google(Firebase)에서 제공하는 **NoSQL 문서형 데이터베이스**입니다. 테이블과 행으로 데이터를 다루는 관계형 DB(RDB)와 달리, **컬렉션 → 문서 → (선택) 서브컬렉션** 구조로 데이터를 저장합니다. 각 문서는 JSON과 비슷한 **필드(key-value)** 로 이루어져 있습니다.

- **실시간 동기화**: 데이터 변경 시 구독 중인 클라이언트에 자동으로 반영되어, 채팅·피드처럼 실시간 업데이트가 필요한 앱에 적합합니다.
- **오프라인 지원**: 로컬 캐시를 사용해 오프라인에서도 읽기·쓰기가 가능하고, 복구 시 서버와 자동 동기화됩니다.
- **확장성**: 서버 구축 없이 사용할 수 있고, 트래픽에 따라 자동으로 확장됩니다.
- **비용**: 읽기·쓰기·삭제 횟수에 따라 과금되므로, 설계 시 **읽기 횟수를 줄이는 것**이 중요합니다.

이 가이드는 Firestore의 구조를 전제로, SNS/커뮤니티 앱에서 **어떤 컬렉션과 문서를 두고, 어떻게 비정규화할지**를 정리한 내용입니다.

---

## 1. Firestore 기본 개념

| 구분 | RDB | Firestore |
|------|-----|-----------|
| 구조 | 테이블 + JOIN | **컬렉션** → **문서** → (선택) **서브컬렉션** |
| 단위 | 행(row) | 문서 = 필드(key-value) |
| 관계 | 외래키, JOIN | 경로 직접 접근 또는 비정규화로 복사 저장 |

설계 시 정하는 것: **어떤 컬렉션을 둘지**, **각 문서에 어떤 필드를 넣을지**, **서브컬렉션으로 둘 대상**.

---

## 2. 전체 구조 (비정규화 지향)

문서 DB에서는 **읽기 패턴에 맞춘 비정규화**가 지향점입니다. 한 번 읽은 문서만으로 화면을 채울 수 있게 구성합니다.

| 용도 | 구조 | 비고 |
|------|------|------|
| 사용자 프로필 | `users` | 원본. 글/댓글/좋아요/팔로우에는 여기 값을 **복사** |
| 게시글 | `posts` | 작성자 이름·프로필 이미지 비정규화 |
| 댓글 | `posts/{postId}/comments` | 작성자 이름·프로필 이미지 비정규화 |
| 좋아요 | `posts/{postId}/likes` | 문서 ID = userId, 작성자 정보 비정규화 |
| 팔로우 | `follows` | 팔로우 대상 이름·프로필 이미지 비정규화 |

**컬렉션 vs 서브컬렉션**: "이 문서에만 속하는 자식"이면 서브컬렉션(예: 댓글). 여러 부모에 걸쳐 조회·다양한 조건 쿼리가 필요하면 최상위 컬렉션(예: posts). 알림이 필요하면 `users/{uid}/notifications` 서브컬렉션 또는 `notifications` 컬렉션을 추가해 Functions에서 생성·푸시와 연동할 수 있습니다.

**이미지·영상 등 바이너리**: 용량이 큰 파일은 **Firebase Storage**에 두고, Firestore 문서에는 **다운로드 URL** 또는 Storage **경로**만 저장하는 패턴이 흔하다. Storage Rules·앱 연동은 [개발 가이드](./개발-가이드.md) §3.2를 참고한다.

---

## 3. 문서 필드 설계

문서 ID는 경로에 포함되며, 리스트·상세에서 참조하기 편하도록 문서 내에 `id` 필드로 동일 값을 저장해 둘 수 있습니다. 시각 필드(createdAt 등)는 Firestore **Timestamp** 타입 사용을 권장합니다.

### users (원본만)

| 구분 | 필드 | 설명 |
|------|------|------|
| 상위 | userId | 문서 ID (Auth uid) |
| 상위 | displayName | 표시 이름 |
| 상위 | profileImageUrl | 프로필 이미지 URL |
| 상위 | createdAt | 가입 시각 |

```json
{
  "userId": "uid123",
  "displayName": "홍길동",
  "profileImageUrl": "https://...",
  "createdAt": "2024-01-15T10:00:00Z"
}
```

### posts

| 구분 | 필드 | 설명 |
|------|------|------|
| 상위 | author | 작성자 정보 (users에서 복사, 중첩 객체) |
| 하위 | author.id | 작성자 uid |
| 하위 | author.name | 작성자 표시 이름 |
| 하위 | author.profileImageUrl | 작성자 프로필 이미지 |
| 상위 | content | 본문 텍스트 |
| 상위 | imageUrls | 이미지 URL 배열 |
| 상위 | likeCount | 좋아요 수 (집계용 비정규화) |
| 상위 | commentCount | 댓글 수 (집계용 비정규화) |
| 상위 | createdAt | 작성 시각 |

```json
{
  "author": {
    "id": "uid123",
    "name": "홍길동",
    "profileImageUrl": "https://..."
  },
  "content": "본문 텍스트",
  "imageUrls": ["https://..."],
  "likeCount": 10,
  "commentCount": 3,
  "createdAt": "2024-01-15T10:00:00Z"
}
```

쿼리 예: `where("author.id", "==", uid)`.

### posts/{postId}/comments

| 구분 | 필드 | 설명 |
|------|------|------|
| 상위 | user | 댓글 작성자 정보 (users에서 복사, 중첩 객체) |
| 하위 | user.id | 댓글 작성자 uid |
| 하위 | user.name | 댓글 작성자 표시 이름 |
| 하위 | user.profileImageUrl | 댓글 작성자 프로필 이미지 |
| 상위 | content | 댓글 내용 |
| 상위 | createdAt | 작성 시각 |

```json
{
  "user": {
    "id": "uid456",
    "name": "김철수",
    "profileImageUrl": "https://..."
  },
  "content": "댓글 내용",
  "createdAt": "2024-01-15T11:00:00Z"
}
```

쿼리 예: `where("user.id", "==", uid)`.

### posts/{postId}/likes

문서 ID = userId (중복 좋아요 방지).

| 구분 | 필드 | 설명 |
|------|------|------|
| 상위 | user | 좋아요 누른 사람 정보 (users에서 복사, 중첩 객체) |
| 하위 | user.id | 좋아요 누른 사람 uid |
| 하위 | user.name | 좋아요 누른 사람 표시 이름 |
| 하위 | user.profileImageUrl | 좋아요 누른 사람 프로필 이미지 |
| 상위 | createdAt | 좋아요 시각 (선택) |

```json
{
  "user": {
    "id": "uid456",
    "name": "김철수",
    "profileImageUrl": "https://..."
  },
  "createdAt": "2024-01-15T11:30:00Z"
}
```

### follows

문서 ID 예: `{followerId}_{followingId}`.

| 구분 | 필드 | 설명 |
|------|------|------|
| 상위 | followerId | 팔로우하는 사람(나) uid |
| 상위 | following | 팔로우 대상 정보 (users에서 복사, 중첩 객체) |
| 하위 | following.id | 팔로우 대상 uid |
| 하위 | following.name | 팔로우 대상 표시 이름 |
| 하위 | following.profileImageUrl | 팔로우 대상 프로필 이미지 |
| 상위 | createdAt | 팔로우 시각 (선택) |

```json
{
  "followerId": "uid123",
  "following": {
    "id": "uid789",
    "name": "이영희",
    "profileImageUrl": "https://..."
  },
  "createdAt": "2024-01-15T12:00:00Z"
}
```

쿼리 예: `where("following.id", "==", uid)`.

---

## 4. 프로필 변경 시 반영

**갱신 범위**: posts(작성자), comments(댓글 작성자), likes(좋아요 누른 사람), follows(following.id가 해당 유저인 문서)에 복사해 둔 이름·프로필 이미지를 일괄 업데이트.

**권장**: 클라이언트가 아닌 **Functions**에서 처리. `users/{userId}` 문서 업데이트 트리거 → collection group 쿼리로 해당 유저가 관여한 문서 조회 → 500개 단위 배치로 갱신. 서브컬렉션 문서도 경로로 직접 접근하므로 성능 부담은 최상위 문서와 동일 수준.

---

## 5. Functions로 처리하기 좋은 기능

| 기능 | 이유 |
|------|------|
| 프로필 변경 시 비정규화 반영 | 끊김 없이 끝까지 실행, 클라이언트 비의존 |
| likeCount / commentCount 갱신 | 트랜잭션으로 일관성 유지, 동시 요청 시 어긋남 방지 |
| 알림 생성·푸시 발송 | 앱 꺼져 있어도 발송 |
| 글/댓글 삭제 전파 | 일괄 삭제·정합성 유지 |
| 신고·스팸·콘텐츠 검증 | 서버에서만 검증, 우회 어렵게 |
| 이메일 발송 | 클라이언트에 메일 발송 권한 두지 않음 |
| 통계·스케줄 | 주기 집계, 오래된 데이터 정리 등 |
| 외부 API 연동 | API 키·비밀을 서버에만 두기 위함 |
| 권한 반영 | Auth/Admin SDK는 서버에서만 사용 |

---

## 6. 추가 고려사항

- **보안 규칙**: users는 본인만, posts는 작성자만 수정/삭제, comments/likes는 해당 경로·본인만 삭제, follows는 본인 follower 문서만 생성/삭제 등으로 제한. 중첩 필드는 `resource.data.author.id == request.auth.uid` 처럼 점 표기로 검사할 수 있습니다.
- **인덱스**: 복합 쿼리 사용 시 콘솔/에러 링크로 복합 인덱스 추가. collection group 쿼리(예: `collectionGroup("comments").where("user.id", "==", uid)`)를 쓰면 해당 컬렉션 그룹 + 필드 조합에 대한 컬렉션 그룹 인덱스가 필요하며, 첫 실행 시 에러 링크로 생성할 수 있습니다.
- **페이지네이션**: `limit` + `startAfter(lastDocumentSnapshot)` 커서 기반.
- **오프라인**: Android 기본 오프라인 지속성 활용, 낙관적 UI와 함께 사용 가능.
- **비용**: 비정규화로 읽기 횟수 감소 → 비용 절감. 프로필 변경 시 쓰기는 늘지만 빈도가 낮아 읽기 감소 효과가 더 큼. 배치 한도 500개는 chunk로 나눠 처리.

---

## 7. 요약

- **구조**: users(원본), posts·comments·likes·follows에 작성자/대상 정보 비정규화.
- **지향점**: 한 번 읽은 문서만으로 화면 구성 → 읽기·비용 최소화.
- **프로필 변경**: users만 수정, 나머지는 Functions에서 collection group + 배치로 반영.
- **추가**: 보안 규칙, 인덱스, 페이지네이션, 오프라인, count 갱신은 설계 시 함께 고려.

---

**작성자** 김갑석 · **작성일** 2026-03-16

---

*이 자료는 멋쟁이사자처럼 부트캠프 학습 목적으로 제공됩니다.*
