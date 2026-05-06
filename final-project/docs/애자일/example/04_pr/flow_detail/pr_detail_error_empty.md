## Summary
- 상세 에러/빈 상태 처리 + 재시도 경로 제공

---

## Related Issue
- Closes #403 (예: 상세 에러/빈 상태 처리)

---

## What’s Done
- 실패 시 에러 메시지 + 재시도(Error + Retry)
- 데이터 없음(Empty) 안내 노출(정책에 따라 Error로 대체 가능)
- 재시도 연타 방지(요청 폭주 방지)

---

## How to Test
1. 네트워크 끊기 → 상세 진입 → Error + Retry 확인
2. 없는 ID(또는 빈 데이터) → Empty(또는 Error) 정책 확인
3. Retry 연타 → 요청 폭주가 없는지 확인

