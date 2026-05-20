# 1팀 (Scoffee) Sprint 1 종료 + 데모일 체크리스트 — 2026-05-20

> **작성**: 2026-05-20 / 보조강사
> **대상**: 1팀 Sprint 1 종료(5/20) + 데모·보완(5/20~5/21) 시기 보조강사 점검 항목
> **베이스라인**:
> - [snapshots/260519_pm.md](../snapshots/260519_pm.md) (회의 채널 완전 회복 sentinel ⭐⭐⭐ + 손지희 온보딩 자발 인수 + Decision Log 8건)
> - [snapshots/260519_am.md](../snapshots/260519_am.md) (W3 최소 Done = Flow A 4상태 데모 / Flow B 이월 합의)
> - [mom/260519_pm_minutes.md](../mom/260519_pm_minutes.md)
> **양식 근거**: [teams-docs/2team/review/team_sprint_briefing_260520_am.md](../../2team/review/team_sprint_briefing_260520_am.md) (2팀 차주 스프린트 발화 브리프 — 데모시나리오·anti-pattern 산업 표준 인용)

---

## ⚠ 사전 주의

1. 본 문서는 **보조강사 점검 체크리스트**. 회의 발화안 ≠ 점검 항목. 발화 필요 항목은 §6에서 별도 표시
2. 1팀 현 상태: 3명 (손지희 신임 팀장 / 정섭 / 이제이) — 송성호 5/12 PO 중도포기
3. **회의 채널 완전 회복 sentinel ⭐⭐⭐** (5/19 AM 9'24" + PM 9'51") — 데모일에도 9분+ 유지 검증
4. **신호등 회고 7일 연속 미시행** → 5/20 AM 8일차 강제 시도 필수
5. 손지희 R-burn-1 회복 격려 누락 — 데모 전 1줄 인정 timing
6. **데모 환경 리스크**: 정섭 PC 32GB RAM 한계 → 시연 호스트는 손지희 권장

---

## 1. 5블록 체크 요약 (한눈에)

| # | 블록 | 핵심 체크 | 시점 |
|---|---|---|---|
| 1 | 데모 자체 (Flow A) | 4상태(로딩/에러/빈/예외) + 헬스 SDK 추위 분기 + 시연 호스트 + API 키 보안 | **데모 전 (5/20 PM 전)** |
| 2 | Sprint 1 종료 운영 | 회고 (Why not done) + Velocity baseline + Decision Log 정리 + 신호등 8일차 | **데모 직후** |
| 3 | 보완 작업 | API 키 제한 + 코드리뷰 정례화 + 테스트 시나리오 + R-W 룰 | **5/20 PM ~ 5/21** |
| 4 | 7회차 임박 6건 1:1 | PO/부팀장 + Must 잔여 + 이제이 R-quiet 인정 + 손지희 R-burn-1 인정 | **Sprint 경계 활용 (5/20~5/21)** |
| 5 | 리스크 모니터링 | R-mtg-mic / R-attend / R-환경 (PC) | **회의 진행 중 + 후속** |

---

## 2. 블록 1 — 데모 자체 (Flow A) ⭐⭐⭐

### 핵심 체크

| 체크 | 출처 / 근거 | 검증 방법 |
|---|---|---|
| **Flow A에 로딩/에러/빈/예외 4상태 포함** 시연 가능 여부 | 5/19 AM 명시 "이번 주 최소 Done" | 4상태 각각 화면 확인 |
| **헬스 SDK "추위 부분" 마감 상태** | 정섭 5/19 PM "추위 부분 빼고 다 했어" | 시연 시 분기/제외 결정 |
| **온보딩 영역 (Flow B 선행 조건) 진척** | 손지희 5/19 PM 자발 인수 | Flow A 데모 흐름에 온보딩 진입점 포함 여부 |
| **User journey 워크스루 형태** 시연 | Atlassian Sprint Demo / Scrum Alliance 표준 | 기능 나열 X, 화면 흐름 ✅ |
| **시연 호스트 결정** | 정섭 PC 32GB RAM 한계 + 캠/영상 동시 부하 sentinel (5/19 PM) | 손지희 호스트 권장 |
| **🚨 API 키 노출 보안 처리** | 7회차 임박 항목 — Firebase 콘솔 제한 | **데모 전 필수**. 시연 중 노출 위험 |

### 산업 표준 근거 (2팀 브리프 §2 인용)

| 출처 | 핵심 |
|---|---|
| [Atlassian Sprint Demo](https://www.atlassian.com/agile/project-management/sprint-demo) | 데모는 user journey 워크스루 형태가 권장 |
| [UAT Best Practices (Quellit)](https://www.quellit.ai/blog/top-10-uat-best-practices-for-sprint-teams) | 사용자 스토리 기반 end-user 관점 시나리오 |
| [Scrum Alliance — Sprint Demo](https://resources.scrumalliance.org/Article/sprint-demo) | end-user perspective 시연 + 작동하는 SW 검증 |

→ **Flow A 데모는 플로우 단위 user journey 형태로 시연**. 기능 나열식(예: "헬스 SDK 붙였습니다", "음료 검색 됩니다") 회피.

---

## 3. 블록 2 — Sprint 1 종료 운영 ⭐⭐⭐

### 데모 직후 체크

| 체크 | 비고 |
|---|---|
| **Sprint 1 회고 — *"왜 못 끝냈는가"* 학습 기회 보존** | Flow B 이월 합의(5/19 AM) → 회고에서 이월 사유 명시 |
| **Velocity baseline 기록** — Sprint 1 처리 플로우/포인트 수 | Sprint 2 계획 기준치. 안 하면 즉흥 운영 |
| **Decision Log 8건 (5/19 단일일 sentinel) 정리** | min SDK · 백로그 일괄 · 온보딩 인수 · 분담 자율 등 — Sprint 1 단위 묶기 |
| **백로그 일괄 등록 완료 여부** (5/19 PM 결정) | 손지희 owner. Flow B 관련 백로그 |
| **🚦 신호등 회고 8일차 강제 시도** | 7일 연속 미시행. 데모일은 더더욱 정착 필수 (보조강사 채팅 백업 즉시 시도) |

### Anti-pattern 차단 — 기간 가변 조정 금지

> **2팀 브리프 §6에서 가져온 핵심 원칙**

Flow B 이월은 **올바른 결정**(스프린트 기간 유지 + 플로우 분할). 회고에서 *"다음엔 기간을 늘릴까요?"* 반문 차단:

| 출처 | 인용 |
|---|---|
| [Scrum.org — 27 Sprint Anti-Patterns](https://www.scrum.org/resources/blog/27-sprint-anti-patterns) | *"스프린트 길이를 며칠 연장해 Sprint Goal을 맞추는 것은 agile이 아니라 책 조작(cooking the books)"* |
| [Age-of-Product](https://age-of-product.com/sprint-anti-patterns-2/) | *"이해관계자 참여를 망가뜨리고, 스크럼 이벤트의 정상 cadence를 무너뜨린다"* |
| [Agile Alliance](https://agilealliance.org/why-you-need-your-user-stories-to-fit-into-one-sprint/) | *"사용자 스토리는 반드시 하나의 스프린트에 들어가도록 설계되어야 한다. 안 들어가면 쪼개라, 기간을 늘리지 마라"* |

→ 1팀 Flow B 이월 = **이미 올바른 선택**. 회고에서 이 점 명시 격려 권장.

---

## 4. 블록 3 — 보완 작업 (5/20 PM ~ 5/21) ⭐⭐

| 체크 | 우선순위 | Owner |
|---|---|---|
| **🚨 노출 API 키 Firebase 콘솔 제한** | 7회차 임박 — **데모 전 처리 필수** (시연 중 노출 위험) | 손지희 |
| **R3 정섭 셀프 머지 #45/#47 — 코드 리뷰 정례화 결정** | 7회차 임박 — 다음 스프린트 시작 전 룰 확정 | 팀 + 강사 |
| **테스트 시나리오 문서화** | 5/18 강사 자발 push, 1주 내 권장 | 팀 |
| **R-W 신규 (온보딩 누락) — 플로우 작성 시 일괄 등록 룰** | 코호트 차원 도입 권장. 다음 플로우부터 적용 | 손지희 |

---

## 5. 블록 4 — 7회차 임박 6건 1:1 클로징 ⭐⭐

> Sprint 1 종료 = **자연스러운 1:1 클로징 timing**. 회의 채널 회복(5/19 AM+PM 9분+ ⭐⭐⭐)으로 압박은 완화됐으나 결정 자체는 필요.

| 항목 | 상태 | 비고 |
|---|---|---|
| 🚨 **PO 역할 분담 / 부팀장 자리** | 손지희 실질 진행자 견고화 ⭐⭐⭐ | 공식화 timing (Sprint 1 종료 활용) |
| 🚨 **Must 7→5~6 압축** | Flow B 이월로 부분 해소 ⭐ | 잔여 Must 확정 (Sprint 2 백로그) |
| 🚨 **이제이 R-quiet 인정** | 5/19 PM 복귀 ✅ + AM 자체 분담 발화 ⭐ | 데이터 견고화 후 격려 timing |
| 🟢 **손지희 R-burn-1 회복 명시 인정** | 5/18 PM~5/19 격려 0건 / 데이터 견고화 ⭐⭐⭐ | **데모 전 1줄 격려 권장** ⭐ |
| 🚨 **코드 리뷰 정례화** (R3 #45/#47) | 신규 셀프 머지 2건 | 블록 3과 통합 |
| 🚨 **노출 API 키 제한** | 보안 | 블록 3과 통합 |

---

## 6. 블록 5 — 리스크 모니터링

| 항목 | 5/19 PM 상태 | 5/20 데모일 검증 |
|---|---|---|
| **R-mtg-mic-1팀** | 🟢⭐⭐⭐ 사실상 해소 sentinel (AM 9'24" + PM 9'51") | 데모 회의도 9분+ 유지 확인 |
| **R-attend** | 🚨 4일째 / 이제이 PM 복귀 ✅ | 이제이 5/20 출석 / 손지희 데모일 결석 X / 정섭 R-stall X |
| **R-환경 (정섭 PC)** | ⚠️ 32GB RAM 한계 + 캠/영상 동시 부하 | 시연 호스트 결정 — 정섭 X, **손지희 호스트 권장** |
| **R-burn-1 (손지희)** | 🟢⭐⭐⭐ 회복 완전 사이클 | 데모일 R-burn 재발 X / 격려 1줄 timing |
| **R-W (1팀 신규)** | ⚠️ 온보딩 누락 즉시 해소 ⭐⭐⭐ | 다음 플로우 작성 시 일괄 등록 룰 적용 |
| **R3 (셀프 머지)** | 🚨 #45/#47 2건 신규 | 1:1 코드 리뷰 정례화 결정 |

---

## 7. 발화/체크 우선순위 압축

```
[데모 전 (5/20 PM 전까지)]
  Flow A 4상태 검증 + 헬스 SDK 추위 분기 결정 + 시연 호스트 결정 (손지희 권장)
  + 🚨 API 키 보안 처리 (시연 노출 위험) + 손지희 R-burn-1 회복 격려 1줄
        ↓
[데모 직후]
  Sprint 1 회고 (Why not done — Flow B 이월 사유 명시, 기간 늘리기 anti-pattern 차단)
  + Velocity baseline 기록 + Decision Log 8건 Sprint 1 단위 정리
  + 🚦 신호등 회고 8일차 강제 시도
        ↓
[5/20 PM ~ 5/21]
  7회차 임박 6건 1:1 클로징
  (PO/부팀장 공식화 + Must 잔여 확정 + 이제이 R-quiet 격려 + 코드리뷰 정례화)
        ↓
[Sprint 2 시작 전]
  R-W 룰 (플로우 작성 시 일괄 등록) + 테스트 시나리오 문서화 + 백로그 일괄 등록 완료 확인
```

---

## 8. 발화 톤 가이드

| 블록 | 톤 |
|---|---|
| 1 (데모 자체) | "체크 + 권유" — Flow A 4상태/시연 호스트는 강사 직접 지시보다 손지희 자율 합의 유도 |
| 2 (Sprint 1 종료) | "원칙 제시 + 격려" — Flow B 이월은 올바른 선택, anti-pattern 차단 한 줄 |
| 3 (보완 작업) | "보안 1순위 + 코드 리뷰 룰" — API 키만 데모 전 강제, 나머지는 1주 |
| 4 (7회차 1:1) | "Sprint 경계 활용 + 격려 우선" — 손지희 R-burn-1 인정 1줄 + 이제이 R-quiet 격려 |
| 5 (리스크) | "모니터링 only" — 회의 발화 X, 점검만 |

### 공통 톤 원칙

- 손지희 신임 팀장 자율 운영 존중 — 직접 지시 회피, 권유·근거 제공 형태
- **회의 채널 완전 회복 sentinel ⭐⭐⭐** 견고화 우선 — 데모일 9분+ 유지 검증
- 이제이 호명 시 *"분담 밀림"* 표현 회피 (5/19 PM 강사 우려 명시 후) → *"PM 복귀 후 분담 정상화"* 톤
- 정섭 PC 이슈는 데모 환경 결정 차원으로만 — 본인 책임 톤 회피

---

## 9. 데모 후 다음 회차 메모

> Sprint 2 시작 시점에 carry-over 예상 항목 (5/20 PM 이후 갱신 권장)

- Flow B 본격 진입 + 온보딩 영역 (손지희) 본격 구현
- 헬스 SDK "추위 부분" 마감 (정섭)
- 이제이 분담 정상화 (R-quiet 부분 회복 sentinel ⭐ 견고화)
- Decision Log 정착 (5/19 8건 sentinel → Sprint 2도 cadence 유지)
- R-W 룰 적용 (플로우 작성 시 일괄 등록)
- 코드 리뷰 정례화 룰 (R3 해소)
- 손지희 PO/부팀장 공식화 (R-PO 7회차 임박 해소)

---

## 10. 관련 메모리·스냅샷 컨텍스트

### 1팀 현 상태 (5/19 PM 기준)

- **인원**: 3명 (손지희 / 정섭 / 이제이) — 송성호 5/12 PO 중도포기
- **운영**: 회의 채널 완전 회복 sentinel (AM 9'24" + PM 9'51" ⭐⭐⭐) + Decision Log 8건 단일일 sentinel ⭐⭐⭐
- **회복 신호**:
  - 손지희 R-burn-1 회복 완전 사이클 ⭐⭐⭐ (5/18 PM 본격 진입 → 5/19 PM 온보딩 자발 인수)
  - 이제이 R-quiet 부분 회복 ⭐ (5/19 AM 자체 분담 발화 + PM 복귀)
- **리스크**:
  - 신호등 회고 7일 연속 미시행
  - 7회차 임박 6건 1:1 잔여 (회의 채널 회복으로 압박 완화)
  - R3 정섭 셀프 머지 #45/#47 신규
  - R-환경 (정섭 PC 32GB RAM 한계) 신규
  - R-W 1팀 발현 (온보딩 누락) — 즉시 해소 ⭐⭐⭐

### 메모리 참조

- [team1_personnel_change_260512.md](../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_personnel_change_260512.md) — 1팀 4→3명, 손지희 신임 팀장
- [team1_burn_attend_260514_am.md](../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_burn_attend_260514_am.md) — R-burn-1 발현
- [team1_mtg_mic_6day_threshold_260518.md](../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_mtg_mic_6day_threshold_260518.md) — 6일차 임계 돌파
- [team1_pm_partial_recovery_260518.md](../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_pm_partial_recovery_260518.md) — PM 부분 회복 + 손지희 코드 본격 진입
- [team1_mtg_full_recovery_260519.md](../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_mtg_full_recovery_260519.md) — 양 회의 9분+ 완전 회복 sentinel

---

## 11. 참조

### 스냅샷·운영 (1팀)

- [snapshots/260519_am.md](../snapshots/260519_am.md)
- [snapshots/260519_pm.md](../snapshots/260519_pm.md)
- [mom/260519_am_minutes.md](../mom/260519_am_minutes.md)
- [mom/260519_pm_minutes.md](../mom/260519_pm_minutes.md)
- [review/team_specific_checks.md](./team_specific_checks.md)
- [review/daily_check_guide.md](./daily_check_guide.md)

### 2팀 브리프 (양식 근거)

- [teams-docs/2team/review/team_sprint_briefing_260520_am.md](../../2team/review/team_sprint_briefing_260520_am.md) — 데모시나리오·anti-pattern·산업 표준 인용

### 외부 자료 (산업 표준 검증 출처 — 2팀 브리프와 공통)

- [Sprint Anti-Patterns — Scrum.org](https://www.scrum.org/resources/blog/27-sprint-anti-patterns)
- [Sprint Anti-Patterns: 29 Examples — Age-of-Product](https://age-of-product.com/sprint-anti-patterns-2/)
- [Should your user stories fit into one sprint? — Agile Alliance](https://agilealliance.org/why-you-need-your-user-stories-to-fit-into-one-sprint/)
- [How to conduct an effective sprint demo — Atlassian](https://www.atlassian.com/agile/project-management/sprint-demo)
- [What Is a Sprint Demo — Scrum Alliance](https://resources.scrumalliance.org/Article/sprint-demo)
- [Top 10 UAT Best Practices — Quellit](https://www.quellit.ai/blog/top-10-uat-best-practices-for-sprint-teams)
- [애자일 실무 가이드(4): 스프린트 리뷰 — 오픈소스컨설팅](https://tech.osci.kr/%EC%95%A0%EC%9E%90%EC%9D%BC-%EC%8B%A4%EB%AC%B4-%EA%B0%80%EC%9D%B4%EB%93%9C4-%EC%8A%A4%ED%94%84%EB%A6%B0%ED%8A%B8-%EB%A6%AC%EB%B7%B0sprint-review/)
