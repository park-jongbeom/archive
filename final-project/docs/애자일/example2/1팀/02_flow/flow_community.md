# User Flow — 커뮤니티 글 읽기·쓰기·상호작용 (FLOW-COMMUNITY)

## Flow ID

- `FLOW-COMMUNITY`

## 목표

- 사용자가 커뮤니티에서 게시글 목록을 보고, 글을 작성하며, 상세에서 댓글·좋아요 등으로 상호작용한다.

## 시작 조건

- 로그인된 상태.
- 하단 네비게이션에서 커뮤니티 탭 진입 가능(`F-community-01`).

## 주요 단계

1. 커뮤니티 목록 화면에서 게시글 목록을 불러온다(Loading → 목록/Empty).
2. 글쓰기 버튼으로 작성 화면에 진입해 제목·본문·태그 등을 입력하고 등록한다(`F-Community-03`).
3. 목록에서 게시글을 선택해 상세로 진입한다(`F-community-04` / `F-Community-Detail-Screen` — 명세 통합).
4. 상세에서 댓글을 작성·등록하고, 좋아요 등 상호작용을 수행한다(`F-Community-05`, 상세 통합 명세).
5. (선택) 본인 글 수정 흐름(`F-Community-06`).

## 종료 조건

- 성공: 작성 글이 목록/상세에 반영되고, 댓글 등 상호작용이 정상 동작.
- 실패: 네트워크/검증 오류 시 메시지 및 재시도.

## 예외/상태(최소)

- 목록 로딩 실패(Error + Retry).
- 게시글 없음(Empty View).
- 검색어 없음·검색 결과 없음(`F-Community-02` — Should).
- 댓글/본문 유효성(빈 입력, 글자 수 제한).
- 중복 제출 방지.

## 관련 기능 ID (명세 참조)

- `F-community-01`, `F-Community-02`, `F-Community-03`, `F-community-04`, `F-Community-05`, `F-Community-06`, `F-Community-Detail-Screen`

## 비고

- 상세 화면 관련 ID가 명세에 중복·분산되어 있으므로, Flow 문서·Issue에서는 **하나의 “상세 상호작용” 목표**로 묶고 AC에서 화면 요소를 나눈다.
