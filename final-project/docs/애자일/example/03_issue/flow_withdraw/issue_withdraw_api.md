# [Feature] 회원 탈퇴 요청 연동 + 로그아웃 처리

## User Story
As a 사용자,
I want 탈퇴를 확정하면 서버/인증 시스템에 탈퇴 요청이 처리되고 로그아웃되길 원한다,
So that 내 계정이 삭제되고 앱을 안전하게 나갈 수 있다.

---

## Acceptance Criteria
- [ ] 탈퇴 요청 중 로딩 UI가 보인다(Loading).
- [ ] 요청 중 중복 제출이 막힌다(연타 방지).
- [ ] 성공 시 로그인 상태가 제거되고 로그인(또는 온보딩) 화면으로 이동한다(Success).
- [ ] 실패 시 에러 메시지와 재시도 경로가 제공된다(Error + Retry).

---

## Flow (링크)
- (FLOW-WITHDRAW) `example/02_flow/flow_withdraw.md`

---

## Details (필요 시만)
> 원칙: 결정된 정책 2~5줄만(긴 화면명세 금지)

- 탈퇴 정책: (예: 즉시 삭제 / 유예 기간 / 일부 데이터 유지 여부)

---

## Edge Cases
- 인증 만료(재로그인 유도)
- 네트워크 오류/서버 오류

---

## How to Test
1. 탈퇴 확정 → 로딩 노출 확인
2. 성공 시 로그인(또는 온보딩) 화면으로 이동하는지 확인
3. 네트워크 끊기 → 에러 메시지 + 재시도 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p1

