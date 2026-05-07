# [Feature] 상세 에러/빈 상태 처리

## User Story
As a 사용자,
I want 상세 데이터가 없거나 실패했을 때 안내를 보고 재시도할 수 있다,
So that 화면이 멈추지 않는다.

---

## Acceptance Criteria
- [ ] 실패 시 에러 메시지와 재시도 경로가 있다(Error + Retry).
- [ ] 데이터가 없을 때 빈 상태 안내가 보인다(Empty, 정책에 따라 Error로 대체 가능).
- [ ] 재시도 연타로 요청이 폭주하지 않는다(중복 방지).

---

## Flow (링크)
- (FLOW-DETAIL) `example/02_flow/flow_detail.md`

---

## How to Test
1. 네트워크 끊기 → 상세 진입 → Error + Retry 확인
2. 없는 ID(또는 빈 데이터) → Empty(또는 Error) 정책 확인
3. Retry 연타 → 요청 폭주가 없는지 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p1

