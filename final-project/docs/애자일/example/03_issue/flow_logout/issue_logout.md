# [Feature] 로그아웃

## User Story
As a 사용자,
I want 로그아웃을 할 수 있다,
So that 내 계정을 안전하게 전환하거나 종료할 수 있다.

---

## Acceptance Criteria
- [ ] 설정/마이페이지 화면에 “로그아웃” 버튼(또는 메뉴)이 있다.
- [ ] 로그아웃 실행 시 로그인 상태(토큰/세션)가 제거된다.
- [ ] 로그아웃 성공 시 로그인(또는 온보딩) 화면으로 이동한다.
- [ ] (필요 시) 처리 중 로딩 UI가 보이고 중복 탭이 제한된다.
- [ ] (필요 시) 실패 시 에러 메시지와 재시도 경로가 있다.

---

## Flow (링크)
- (FLOW-LOGOUT) `example/02_flow/flow_logout.md`

---

## How to Test
1. 로그인 상태에서 설정/마이페이지 진입 → 로그아웃 버튼 노출 확인
2. 로그아웃 탭 → 로그인 화면 이동 및 재진입 시 로그인 필요 확인
3. (선택) 실패 조건에서 에러/재시도 노출 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p0
