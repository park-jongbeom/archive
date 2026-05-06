# [Feature] 회원 탈퇴 진입/확인 UI

## User Story
As a 사용자,
I want 설정 화면에서 회원 탈퇴를 선택하고 최종 확인을 할 수 있다,
So that 실수로 탈퇴하지 않도록 확인한 뒤 진행할 수 있다.

---

## Acceptance Criteria
- [ ] 설정(계정) 화면에 “회원 탈퇴” 진입 버튼이 있다.
- [ ] 탈퇴 안내/주의사항이 보인다.
- [ ] 탈퇴 확인 UI(다이얼로그/바텀시트/화면)가 있으며, 취소할 수 있다.
- [ ] “탈퇴 확정”을 탭하면 탈퇴 요청 이벤트가 발생한다(실연동은 다른 Issue).

---

## Flow (링크)
- (FLOW-WITHDRAW) `example/02_flow/flow_withdraw.md`

---

## Edge Cases
- 뒤로 가기/취소 동작
- 중복 탭(연타)

---

## How to Test
1. 설정 화면 진입 → 회원 탈퇴 메뉴 표시 확인
2. 탈퇴 진입 → 안내/확인 UI 표시 확인
3. 취소/뒤로가기 → 원래 화면으로 복귀 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p1

