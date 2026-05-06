## Summary
- 홈 새로고침 연타 시 크래시 방지 및 Error/Retry 상태 처리 보강

---

## Related Issue
- Closes #999 (예: 홈 새로고침 크래시 버그)

---

## What’s Done
- 실패 응답/예외를 UiState.Error로 매핑해 크래시 방지
- 새로고침 중복 요청 제한(연타 가드) 추가
- Error UI + Retry 동작 연결

---

## How to Test
1. 네트워크 OFF 상태에서 새로고침 연타 → 크래시 없이 Error + Retry 확인
2. 네트워크 ON 후 Retry → 목록 정상 표시 확인
3. `example/05_demo/demo_home_list_detail.md` 시나리오 재실행 → 회귀 없음 확인

---

## Notes
- 추후 동일 패턴의 새로고침/재시도 로직은 공통 유틸로 리팩터링 가능

