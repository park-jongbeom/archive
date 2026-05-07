# [Bug] 홈 새로고침 시 크래시 발생

## User Story
As a 사용자,
I want 홈에서 새로고침을 해도 앱이 종료되지 않길 원한다,
So that 안정적으로 목록을 갱신할 수 있다.

---

## 재현 단계 (Steps to Reproduce)
1. 앱 실행 → 홈 화면 진입
2. 네트워크가 불안정한 상태(또는 비행기 모드)로 만든다
3. 새로고침(스와이프/버튼)을 연타한다(3~5회)
4. 앱이 크래시(종료)한다

---

## Expected / Actual
- Expected: 크래시 없이 에러 UI를 보여주고 재시도할 수 있다.
- Actual: 일정 확률로 앱이 종료된다.

---

## Acceptance Criteria
- [ ] 새로고침 연타 시에도 앱이 크래시하지 않는다.
- [ ] 실패 시 Error 메시지와 Retry가 제공된다.
- [ ] 로딩 중 중복 요청이 제한되어 요청이 폭주하지 않는다(중복 방지).
- [ ] 수정 후에도 홈 목록/상세 데모 시나리오가 회귀 없이 통과한다.

---

## Related Flow / Demo
- Flow: `example/02_flow/flow_home_list.md`
- Demo: `example/05_demo/demo_home_list_detail.md` (시나리오 2/3 참고)

---

## Root Cause (작성 가능하면)
- (예) 네트워크 실패 시 null 값 처리 누락으로 NPE 발생

---

## Fix Strategy (한 줄)
- (예) 실패 응답을 UiState.Error로 매핑하고, 새로고침 요청을 1회만 허용하도록 가드 추가

---

## How to Test
1. 네트워크 OFF 상태에서 새로고침 연타 → 크래시 없이 Error + Retry 확인
2. 네트워크 ON 후 Retry → 목록 정상 표시 확인
3. 기존 데모 시나리오(홈 목록/상세) 전체 통과 확인

---

## Related
- PR: TBD
- Crash log: TBD

---

## Priority (Projects)
- priority: p0

