# [Feature] 홈 목록 에러/재시도 처리

## User Story
As a 사용자,
I want 홈 목록이 실패했을 때 에러를 보고 재시도할 수 있다,
So that 네트워크가 불안정해도 앱을 계속 사용할 수 있다.

---

## Acceptance Criteria
- [ ] 실패 시 에러 메시지와 재시도 버튼(또는 동작)이 보인다(Error + Retry).
- [ ] 재시도 시 다시 로딩 상태가 노출된다(Loading).
- [ ] 연속 재시도(연타)로 요청이 폭주하지 않는다(중복 방지).

---

## Flow (링크)
- (FLOW-HOME) `example/02_flow/flow_home_list.md`

---

## How to Test
1. 네트워크 끊기 → 홈 진입 → Error + Retry 확인
2. Retry 탭 → 다시 로딩 후 요청 재시도 확인
3. Retry 연타 → 요청 폭주 방지 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p1

