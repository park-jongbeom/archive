# [Feature] 회원 탈퇴 실패/상태(Error/Retry) 처리 보강

## User Story
As a 사용자,
I want 탈퇴가 실패했을 때 이유를 알고 다시 시도할 수 있다,
So that 탈퇴 과정에서 앱이 멈추지 않는다.

---

## Acceptance Criteria
- [ ] 실패 시 사용자에게 이해 가능한 에러 메시지가 보인다.
- [ ] 실패 후 재시도 경로가 있다(버튼/다시 시도).
- [ ] 로딩 중에는 입력/버튼이 적절히 비활성화된다(중복 제출 방지).

---

## Flow (링크)
- (FLOW-WITHDRAW) `example/02_flow/flow_withdraw.md`

---

## How to Test
1. 네트워크 끊기(또는 실패 조건 만들기) → 탈퇴 확정 클릭
2. 에러 메시지 + 재시도 노출 확인
3. 재시도 수행 → 다시 요청되는지 확인
4. 연타 → 중복 제출이 막히는지 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p2

