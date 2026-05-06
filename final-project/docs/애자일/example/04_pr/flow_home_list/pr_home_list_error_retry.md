## Summary
- 홈 목록 에러 처리 + 재시도 + 중복 요청 방지 보강

---

## Related Issue
- Closes #303 (예: 홈 목록 에러/재시도 처리)

---

## What’s Done
- 실패 시 에러 메시지 + 재시도 제공(Error + Retry)
- 재시도 시 로딩 상태 노출(Loading)
- 재시도 연타 방지(요청 폭주 방지)

---

## How to Test
1. 네트워크 끊기 → 홈 진입 → Error + Retry 확인
2. Retry 탭 → 다시 요청되는지 확인
3. Retry 연타 → 요청이 과도하게 발생하지 않는지 확인

