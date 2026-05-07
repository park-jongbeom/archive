# [Feature] 로그인 실패/상태(Loading/Error) 처리 보강

## User Story
As a 사용자,
I want 로그인 실패/로딩 상태를 명확히 확인하고 다시 시도할 수 있다,
So that 로그인이 실패해도 앱이 멈추지 않는다.

---

## Acceptance Criteria
- [ ] 로딩 중에는 입력/버튼이 적절히 비활성화된다(중복 제출 방지).
- [ ] 실패 시 사용자에게 이해 가능한 에러 메시지가 보인다.
- [ ] 실패 후 재시도 경로가 있다(버튼/다시 시도).
- [ ] (권장) 에러 메시지/문구는 팀 규칙으로 통일한다.

---

## Flow (링크)
- (FLOW-LOGIN) `example/02_flow/flow_login.md`

---

## Edge Cases
- 오프라인(네트워크 없음)
- 타임아웃/일시적 오류

---

## How to Test
1. 로그인 요청 중 버튼 연타 → 입력/버튼 비활성 및 중복 제출 방지 확인
2. 실패 상황에서 에러 메시지 노출 확인
3. 재시도 동작 수행 → 다시 로그인 시도되는지 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p1

