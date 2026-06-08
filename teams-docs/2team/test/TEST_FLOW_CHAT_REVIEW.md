# TEST_FLOW_CHAT 조치 추천

> 대상 문서: [TEST_FLOW_CHAT.md](TEST_FLOW_CHAT.md) (사본: [../repo/docs/Demo/TestSheet/TEST_FLOW_CHAT.md](../repo/docs/Demo/TestSheet/TEST_FLOW_CHAT.md))
> 작성 시점: 2026-05-29
> 검토 관점: 테스트 시트 작성 표준(ISTQB / IEEE 829 / ISO/IEC/IEEE 29119-3) 부합도

---

## 1. 배경

현 시트는 개발자가 직접 작성한 것으로, 다음 두 가지 사용자 인지가 있다.

- (a) 전반적으로 과다하게 작성되어 있다.
- (b) 사전 준비의 "네트워크 연결 정상"은 **검증 대상**이지 **사전 가정**으로 두면 안 된다.

본 문서는 위 인지를 업계 표준에 비추어 검증하고, 구체적 조치를 우선순위별로 제시한다. **본 문서는 추천이며 시트 자체는 수정하지 않았다.**

---

## 2. 적용 표준 (요약)

| 항목 | 표준 정의 | 핵심 원칙 |
|---|---|---|
| Precondition | 테스트 시작 전 이미 충족되어 있어야 하는 **상태(state)** | "what already exists" — 액션이 아니라 상태. **검증 대상은 precondition 아님** |
| Test Step | 테스터가 직접 수행하는 액션 | 모호함 없는 구체 동작 |
| Expected Result | 단일·검증 가능한 결과 | judgment 없이 평가 가능 |
| Brevity | 최소 충분 정보 | 공통 setup은 상위로, row 중복 금지 |
| Independence | 테스트 케이스 간 독립 | 이전 케이스 진행 상태를 사전 조건으로 두지 않음 |

핵심 룰: **"검증 대상(object of testing)이 precondition에 포함되면 그 항목은 그 시트에서 영구 검증 불가."**

참고 출처:
- ISTQB Glossary — Test Case: https://istqb-glossary.page/test-case/
- Lead With Skills — ISTQB Test Design Process: https://www.leadwithskills.com/blogs/test-conditions-test-cases-istqb-design-process
- QATestLab — How to correctly write preconditions: https://en.training.qatestlab.com/blog/course-materials/how-to-correctly-write-preconditions-in-test-cases/
- Testsigma — Precondition in a Test Case: https://testsigma.com/blog/precondition-in-test-case/
- IEEE 829 개요: https://www.professionalqa.com/ieee-standard-829-1998
- TestLodge — Negative Test Cases (네트워크 단절 등): https://www.testlodge.com/resources/learning_center/test_cases/negative_test_cases

---

## 3. 식별된 이슈 및 조치 추천

### [CRITICAL-1] "네트워크 연결 정상"이 사전 준비에 박힘

**위치**: TEST_FLOW_CHAT.md L25, `## 사전 준비 → ### Real 검증` 6번째 항목

**문제**:
- Firebase Live API 기반 앱에서 네트워크 가용성은 **검증 대상**이며, 표준상 검증 대상은 precondition으로 둘 수 없다.
- 본 시트는 장애/재연결(CHAT-006) AC 6개를 모두 Mock(`AIEvent` 기반)으로만 커버한다. **실제 네트워크 단절(비행기 모드, Wi-Fi 토글 등) 시나리오는 Real/Mock 어디서도 검증되지 않는다.**
- 판정 기준 L136에 "장애 preset은 실제 네트워크 장애를 만들지 않고 `AIEvent` 기반 ViewModel/UI 반응을 검증한다"고 명시되어 있으나, **왜** 실제 네트워크 단절을 검증하지 않는지(스코프 사유)는 빠져 있다.

**조치 추천** (택1):

(A) **권장 — 사전 준비에서 제거 + Real 부정 케이스 추가**
- 사전 준비 L25 "네트워크 연결 정상" 삭제
- Real 시트에 다음 행 1건 추가:

  | Test ID | 구분 | 시나리오 | 연결 AC | 사전 조건 | 수행 절차 | 기대 결과 |
  |---|---|---|---|---|---|---|
  | TC-CH-XX | Real | 통신 중 네트워크 단절 | CHAT-006 AC1·2·3 | `devDebug`, Chat READY, 정상 네트워크 | PTT press 후 발화 중 비행기 모드 ON → 10초 후 OFF | reconnecting 상태 표시 → 자동 복구 시 READY/IDLE 복귀, 복구 실패 시 retry 가능 error UI |

(B) **차선 — 사전 준비 약화 + 스코프 사유 명시**
- "네트워크 연결 정상" → "Real happy path 기준 네트워크 가용 환경"로 표현 약화
- 판정 기준에 한 줄 추가: "실제 네트워크 단절(비행기 모드 등) 검증은 본 스프린트 스코프 밖. 사유: <사유 기재> — 추후 시트에서 보강."

**근거**: TestLodge "Negative Test Cases" — 네트워크 단절은 명시적 부정 시나리오로 다뤄야 하며 precondition으로 가정하면 안 됨.

---

### [CRITICAL-2] "마이크 권한 허용 가능 상태"가 사전 준비에 있음 — 모순

**위치**: TEST_FLOW_CHAT.md L24, `## 사전 준비 → ### Real 검증` 4번째 항목

**문제**:
- TC-CH-04 사전 조건: 마이크 권한 **미허용** 상태
- TC-CH-05 사전 조건: 마이크 권한 **미허용** 상태
- TC-CH-06 사전 조건: 마이크 권한 **영구 거부** 상태
- TC-CH-07 사전 조건: 마이크 권한 **허용**

마이크 권한 상태는 케이스별로 4가지로 갈리는 **변수**이다. 표준상 공통 사전 준비에 들어갈 항목이 아니라 row 단위 `사전 조건` 컬럼의 영역이다. 사전 준비의 "허용 가능 상태"는 실질적 의미가 없고 혼란만 유발한다.

**조치 추천**:
- 사전 준비 L24 "마이크 권한 허용 가능 상태" 삭제
- row의 `사전 조건` 컬럼에서만 명시 (현재 이미 명시되어 있음 — 사전 준비 항목만 제거하면 됨)

**근거**: ISTQB — precondition은 "what already exists" 즉 단일 상태여야 함.

---

### [HIGH-1] "조용한 환경 또는 이어폰 권장"은 precondition이 아님

**위치**: TEST_FLOW_CHAT.md L26

**문제**:
- "권장(recommended)"은 Pass/Fail 판정에 영향을 주지 않는다 → precondition의 정의에 부합하지 않음
- 표준상 이런 항목은 **테스터 가이드라인**으로, 별도 섹션에 배치되어야 함

**조치 추천**:
- 사전 준비에서 제거
- 상단 사용법 주석(L9 이하 `>` 블록)에 한 줄 추가:
  > 테스터 노트: 발화 검증 시 조용한 환경 또는 이어폰 사용을 권장.

---

### [HIGH-2] 사전 준비 ↔ row 사전 조건 중복

**위치**: TEST_FLOW_CHAT.md L106-L119 (Real row 14건)

**문제**:
- 사전 준비에 `devDebug`, `로그인 완료`가 명시되어 있는데, Real 14개 row 사전 조건에 매번 `devDebug` 반복 명시
- ISTQB "Brevity" 원칙 위반 — 노이즈만 늘림

**조치 추천**:
- Real row의 `사전 조건` 컬럼에서 `devDebug` 일괄 삭제 (사전 준비 섹션이 이미 보장)
- `로그인 완료`는 TC-CH-01에만 남기고 후속 row에서 삭제 (선행 row가 보장)
- Mock row의 `mockDebug`는 Real과 명확히 구분되므로 유지

**예시 (Before → After)**:
- Before: `` `devDebug`, 로그인 완료, Dashboard 진입 상태 ``
- After: `Dashboard 진입 상태`

---

### [HIGH-3] TC-CH-08 사전 조건 "TC-CH-07 진행 중" — 테스트 독립성 위반

**위치**: TEST_FLOW_CHAT.md L113

**문제**:
- 표준상 테스트 케이스는 **independent**해야 함 — 다른 케이스 진행 상태를 사전 조건으로 두면 안 됨
- TC-CH-07이 실패하면 TC-CH-08은 실행조차 불가 → blocker 연쇄
- "PTT 입력 종료"는 그 자체로 독립 검증 가능한 상태("녹음 진행 중")로 시작 가능

**조치 추천**:
- 사전 조건을 자기완결화:
  - Before: `TC-CH-07 진행 중`
  - After: `` `devDebug`, 마이크 권한 허용, Chat READY, PTT press로 녹음 진행 중 상태 ``

---

### [MEDIUM-1] AC 체크리스트 전체 `[x]` — 정보 가치 낮음

**위치**: TEST_FLOW_CHAT.md L45-L95

**문제**:
- 51줄 분량의 AC 체크리스트가 모두 `[x]` → 사실상 정보 가치 0 (전부 포함된다는 메시지만 남김)
- AC ↔ 테스트 추적은 row의 `연결 AC` 컬럼에서 이미 수행 → 중복

**참고 — 1팀 ONBOARDING 시트 패턴**:
- [TEST_FLOW_ONBOARDING.md L23-L71](../repo/docs/Demo/TestSheet/TEST_FLOW_ONBOARDING.md) 은 `[ ]` (누락) AC를 사유와 함께 명시 — 이 형태가 표준에 더 가깝다.

**조치 추천**:
- 전부 `[x]`인 그룹은 한 줄 요약으로 압축 (예: "CHAT-001: AC1~5 전체 커버 — TC-CH-01~03, TC-CH-22")
- 누락/위임 AC만 사유와 함께 별도 명시 (현재 시트에는 누락 AC가 없으나, 향후 발생 시 이 형태로 기록)

---

### [LOW-1] "Firestore 사용자 초기 설정 완료 또는 신규 설정 가능 상태" — 의미 분기

**위치**: TEST_FLOW_CHAT.md L23

**문제**:
- "완료 또는 신규 가능"은 사실상 모든 상태를 허용 → precondition으로서의 변별력 없음
- 어떤 시나리오에서 어느 쪽이 필요한지 row에서만 알 수 있음

**조치 추천**:
- 사전 준비에서는 "Google 로그인 가능한 테스트 계정"만 남기고 Firestore 상태 명시는 삭제
- TC-CH-03(관심 주제 다이얼로그)처럼 신규 상태가 필요한 row의 `사전 조건`에서만 명시 (현재 이미 명시되어 있음)

---

## 4. 정정 후 사전 준비 권장안

```markdown
## 사전 준비

### Real 검증
- Android Studio Build Variant: `devDebug`
- Google 로그인 가능한 테스트 계정

> 테스터 노트:
> - 발화 검증 시 조용한 환경 또는 이어폰 사용을 권장.
> - 마이크 권한 / 네트워크 상태 / Firestore 초기 상태는 **각 row의 사전 조건에서 명시** — 사전 준비에서 가정하지 않는다.

### Mock 검증
- Android Studio Build Variant: `mockDebug`
- preset 변경 위치:
  ... (현행 유지)
- preset 변경 후 앱 재빌드/재실행
- logcat 필터: `ChatMockPreset`
```

---

## 5. 조치 우선순위

| 우선순위 | 조치 | 영향 | 작업량 |
|---|---|---|---|
| CRITICAL-1 | 네트워크 사전 가정 제거 + Real 부정 케이스 추가 (또는 스코프 사유 명시) | 검증 커버리지 실질 확대 | 30분 |
| CRITICAL-2 | 마이크 권한 사전 준비에서 제거 | 모순 해소 | 5분 |
| HIGH-1 | "조용한 환경" → 테스터 노트로 이동 | 표준 정렬 | 5분 |
| HIGH-2 | row 사전 조건의 `devDebug` 일괄 삭제 | 가독성 | 15분 |
| HIGH-3 | TC-CH-08 자기완결화 | 독립성 회복 | 5분 |
| MEDIUM-1 | AC 체크리스트 압축 | 가독성 | 20분 |
| LOW-1 | Firestore 상태 사전 준비 정리 | 표준 정렬 | 5분 |

**전체 권장 적용 시 작업량**: 약 1시간 30분 (네트워크 부정 케이스 실제 실행 검증 시간 별도)

---

## 6. 의사결정이 필요한 사항

다음 두 항목은 2팀 내부 합의가 필요하다 — 보조강사 권한 밖이므로 결정만 받아 반영한다.

1. **네트워크 단절 Real 검증 — 본 스프린트에 포함할지 여부**
   - 포함 시: CRITICAL-1 (A) 적용 (Real row 추가)
   - 미포함 시: CRITICAL-1 (B) 적용 (사유 명시 후 보강 항목으로 기록)

2. **시트 두 곳(`test/` vs `repo/docs/Demo/TestSheet/`) 동기화 정책**
   - 두 위치에 거의 동일한 시트가 존재 — 한쪽을 single source of truth로 정하고 다른 쪽은 링크 처리하는 것이 향후 drift 방지에 유리하다.

---

## 7. 참고 — 본 시트의 잘된 점

표준 부합도가 높은 항목도 함께 기록한다(향후 다른 시트에서 유지할 패턴):

- **Real / Mock 구분 명시** + 각 빌드 variant의 검증 범위 분리 → 매우 명확
- **AC ↔ Test ID 양방향 추적** (체크리스트 + row `연결 AC` 컬럼)
- **판정 기준** 별도 섹션으로 분리 → 어떤 항목이 Pass/Fail 판정 대상이고 어떤 항목이 아닌지 명시
- **Mock preset 변경 위치 코드 경로 명시** + logcat 필터 태그 안내 → 재현성 확보
