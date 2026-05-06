# [Feature] 로그인 API 연동

## User Story
As a 사용자,
I want 로그인 버튼을 누르면 서버/인증 시스템에 로그인 요청을 보내고 결과를 받고 싶다,
So that 실제로 로그인할 수 있다.

---

## Acceptance Criteria
- [ ] 로그인 요청 중 버튼 로딩(또는 로딩 UI)이 보인다(Loading).
- [ ] 요청 중 중복 제출이 막힌다(연타 방지).
- [ ] 성공 시 로그인 상태가 저장되고 홈 화면으로 이동한다(Success).
- [ ] 실패 시 에러 메시지가 보이고 재시도할 수 있다(Error + Retry).

---

## Flow (링크)
- (FLOW-LOGIN) `example/02_flow/flow_login.md`

---

## Details (필요 시만)
> 원칙: 결정된 정책 2~5줄만(긴 화면명세 금지)

- 인증 방식: (예: Firebase Auth / 자체 로그인)
- 로그인 상태 저장 위치: (예: DataStore / EncryptedSharedPreferences 등 팀 합의)

---

## Edge Cases
- 네트워크 오류
- 서버 오류(5xx)
- 잘못된 자격증명(이메일/비밀번호 불일치)

---

## How to Test
1. 정상 계정으로 로그인 시도 → 로딩 노출 후 홈 이동 확인
2. 실패 계정(잘못된 비밀번호) → 에러 메시지 + 재시도 확인
3. 로그인 버튼 연타 → 중복 요청/중복 이동이 없는지 확인

---

## Related
- PR: TBD
- API: TBD
- Design(Figma): TBD (Frame 링크 권장)

---

## Priority (Projects)
- priority: p0

