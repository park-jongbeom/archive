# 1팀 Issue ↔ PR ↔ Branch ↔ PRD 추적 매트릭스

> **목적**: GitHub Issue, Pull Request, 브랜치, PRD Must 항목이 끝까지 정합한지 한 표로 추적.
> **갱신 주기**: 매일 일일 체크 시 갱신 ([`./daily_check_guide.md`](./daily_check_guide.md))
> **마지막 갱신**: 2026-05-13 (베이스라인)

---

## 1. 추적 매트릭스 (Issue 단위)

| Issue | 제목 (요약) | 작성자 | 생성일 | 상태 | PRD 매핑 | 관련 브랜치 | 관련 PR | Issue 닫힘? | 비고 |
|---|---|---|---|---|---|---|---|---|---|
| #7 | 음료 DB화 (A-2) | starlightfjh | 05-11 | Open | Must-2 | feature/firebase-setup | #2, #3, #11, #14, #17 | ❌ | 머지됐지만 미닫힘 |
| #8 | 카페인 저장 기능 (A-4) | juu124 | 05-11 | Open | Must-2 | data/repository/CaffeineRepository | #12, #16 | ❌ | 머지됐지만 미닫힘 |
| #9 | CaffeineEntity & Room (A-1) | sdfg7979-glitch (송성호?) | 05-11 | Open | Must-3 | data/repository/CaffeineRepository | #12 | ❌ | 머지됐지만 미닫힘 |
| #10 | 반감기 계산 로직 (A-5) | ljs990326-cloud | 05-11 | Open | Must-3 | domain/util/CaffeineCalculator | #6 | ❌ | 머지됐지만 미닫힘 |
| #19 | 음료 검색 로직 (A-4-2) | starlightfjh | 05-12 | Open | Must-2 | feature/Drink_Search | (미머지) | — | 진행 중 |
| #20 | 잔류량 게이지 UI (A-6) | ljs990326-cloud | 05-13 | Open | Must-3 | presentation/home | #18 | ❌ | 머지됐지만 미닫힘 |

---

## 2. PRD Must 항목 ↔ Issue ↔ FigJam 3중 정합성

PRD([Product Brief Scoffee ver 1.0](../product_brief/Product%20Brief_Scoffee_ver%201.0)) Must 7개 항목 × GitHub Issue × FigJam 시각화:

| Must # | 기능 | Issue 등록? | Issue 번호 | 코드 상태 | FigJam 시각화 |
|---|---|---|---|---|---|
| 1 | 온보딩 (수면/민감도/기상) | ❌ **미등록** | — | 🟢 5개 화면 | ✅ 와이어프레임 |
| 2 | 카페인 입력 | ✅ | #7, #8, #19 | 🟢 화면+VM | ✅ Flow-A + 와이어프레임 |
| 3 | 잔류량 실시간 계산 | ✅ | #9, #10, #20 | 🟢 완료 | ✅ MVP 우선순위 (공식) |
| 4 | Gemini 컷오프 산출 | ❌ **미등록** | — | 🟡 스텁 | ❌ **미시각화** |
| 5 | WearOS 알림 3종 | ❌ **미등록** | — | 🟡 화면 골격만 | ❌ **미시각화** |
| 6 | Health Services 수면 | ❌ **미등록** | — | 🟡 DataSource 스텁 | ✅ 와이어프레임 (수면 분기) |
| 7 | 주간 리포트 | ❌ **미등록** | — | 🟡 빈 UseCase | ✅ Flow-B + 와이어프레임 |

> 🚨 **3중 정렬이 모두 깨진 항목**: Must-4, Must-5 — Issue 없음 + FigJam 시각화 없음 + 코드 스텁만. **계획·디자인·구현 3 영역에서 가장 위험한 빈틈**.

> 🚨 **Must 7개 중 5개가 Issue 미등록**. Week 1 검토에서 권장한 mock-first 3건도 Issue로 분리되지 않아 추적이 어려움. 다음 회의 의제로 격상 권장.

---

## 3. PR 정합성 점검

13개 머지된 PR 중 "Closes #N" 명시 여부:

| PR | 브랜치 | 머지일 | Issue 연결? | 조치 |
|---|---|---|---|---|
| #2 | firebase-setup | 05-08 | ❌ | (구 작업, 무시) |
| #3 | firebase 수정 | 05-08 | ❌ | (구 작업, 무시) |
| #4 | retrofit 추가 | 05-08 | ❌ | (구 작업, 무시) |
| #5 | hilt 구조 | 05-11 | ❌ | (구 작업, 무시) |
| #6 | 반감기 계산 | 05-11 | ❌ | **#10 닫혀야 했음** |
| #11 | firebase setup | 05-11 | ❌ | (#7과 관련) |
| #12 | CaffeineRepository | 05-12 | ❌ | **#8, #9 닫혀야 했음** |
| #13 | 디자인 시스템 | 05-12 | ❌ | (Must-1 부속) |
| #14 | firebase setup | 05-12 | ❌ | (#7과 관련) |
| #15 | drink_dto 수정 | 05-12 | ❌ | (#7 부속) |
| #16 | 네비게이션 | 05-12 | ❌ | (#8 부속) |
| #17 | DB 연결 테스트 | 05-12 | ❌ | **#7 닫혀야 했음** |
| #18 | presentation/home | 05-12 | ❌ | **#20 닫혀야 했음** |

**`Closes #N` 명시 비율: 0/13 = 0%**

---

## 4. 추적 단절 패턴

다음 패턴이 반복되고 있어 즉시 개선 필요:

### 패턴 A: Issue 등록 후 머지까지 진행, 그러나 Issue 미닫힘
- 발생 빈도: 4건 (#7, #8, #9, #10, #20)
- 영향: 진척 가시화 불가, 회고 시 작업 이력 추적 어려움
- 해결: PR description 또는 머지 commit message에 `Closes #N` 추가

### 패턴 B: 머지 후 Issue 작성 (역순)
- 발생 빈도: #20은 2026-05-13 작성, 관련 PR #18은 2026-05-12 머지 → **시간 역전**
- 의미: Issue가 "작업 시작 전 계획" 역할이 아닌 "완료 후 기록" 역할로 변질
- 해결: 작업 시작 전 Issue 먼저 등록 가이드

### 패턴 C: PRD ↔ Issue 불일치
- Must 7개 중 5개 미등록 (위 § 2 참조)
- 의미: 어떤 Must가 어디까지 진행됐는지 GitHub에서 시각화 불가
- 해결: Must-4~7 Issue 일괄 등록 (1주차 백로그)

---

## 5. 권장 GitHub 워크플로우 (1팀에 제안할 안)

### 5-1. Issue 템플릿 정착

각 Must 항목당 Issue 1개 + 하위 작업은 체크박스로:

```markdown
## 📋 Must-N: {기능명}

### 목표
{무엇을 만드는지}

### 완료 조건 (DoD)
- [ ] 단위 1
- [ ] 단위 2

### 관련 PR
- #X
- #Y
```

### 5-2. PR description 컨벤션

모든 PR이 다음 한 줄을 반드시 포함:

```markdown
Closes #N
```

→ GitHub가 PR 머지 시 Issue를 자동으로 닫음. 수동 작업 불필요.

### 5-3. 라벨 도입

현재 기본 9개 라벨만 있음. 다음 추가 권장:

| 라벨 | 색 | 용도 |
|---|---|---|
| `Must` | 🔴 | 필수 기능 |
| `Should` | 🟡 | 권장 기능 |
| `Won't` | ⚪ | 범위 외 (논의용) |
| `mock-first` | 🟣 | Week 1 합의된 mock 단계 |
| `wear` | ⌚ | WearOS 관련 |
| `health-services` | 🌙 | Samsung Health/Health Connect |
| `gemini` | 🤖 | Gemini API |
| `blocked` | 🚫 | 외부 의존으로 막힘 |

### 5-4. Milestone 도입

Week 2, Week 3, …, Week 6 Milestone 생성 → 각 Issue를 해당 주차에 배정 → "Week 3 잔여 Issue" 한 눈에 파악 가능.

---

## 6. 매트릭스 갱신 규칙

매일 일일 체크 시 다음 순서로 이 문서 갱신:

1. § 1 매트릭스에 **신규 Issue / PR 행 추가**
2. § 1에서 **상태 변경 반영** (Open → Closed, 머지 등)
3. § 2 PRD 매핑에서 **Issue 등록 상태 갱신**
4. § 3 PR 정합성에서 **신규 PR의 Closes #N 여부 기록**
5. § 4 추적 단절 패턴에 **신규 사례 누적**
6. 변경 사항 1줄 요약을 § 7 변경 로그에 기록

---

## 7. 변경 로그

| 날짜 | 변경 내용 |
|---|---|
| 2026-05-13 | 베이스라인 매트릭스 생성 (Issue 6건, PR 13건) |
