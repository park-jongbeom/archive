# v2 양식 즉시 적용 가이드 — 오늘 PM(2026-05-14 16:00) 부터

> **목적**: v2 정식 정착(2026-05-15 AM)까지 시간 부족 시 오늘 PM에 **최소한의 핵심 변경 3건만** 적용하기 위한 압축 가이드.
> **상세 양식**: [meeting_prep_template_v2.md](../../teams-docs/.shared/meeting_prep_template_v2.md) (487줄)

---

## 1. 오늘 PM 즉시 적용 변경 3건 (5분 내 추가 가능)

### 변경 1 — 🚨 강사 사전 통지 의제 3건 (보조강사 마이크 SPOF 백업) ⭐⭐⭐

**언제**: PM 회의(16:00) **5분 전 = 15:55**
**어디**: 강사 1:1 채팅 또는 카톡
**무엇**: 다음 양식 1줄

```
[<팀번호>팀 PM 사전 통지]
1. <1순위, 회의 첫 안건 권유> — {예: R13 google-services 36h+ 정체, 첫 안건 격상 권유}
2. <2순위> — {예: carry-over 2회+ 이월 항목}
3. <3순위> — {예: 인물별 신호 / 좋은 점 격려}
(마이크 이슈 시 회의 중 추가 push 불가)
```

**팀별 오늘 PM 사전 통지 예시**:

#### 1팀 (carry-over 누적 11건 — 가장 critical)
```
[1팀 PM 사전 통지]
1. (회의 첫 안건) 🚨 R13 google-services.json 36h+ 정체 — 강사 5/13 16:05 직접 지시 사항. 즉시 검증 + 데드라인 명시 권유
2. 🚨 R-burn-1 분담 — 정섭 결석 + 손지희 "너무 힘들어요" 발언. 작업 일부 이제이 분담 권유
3. 🟢 이제이 PR #27 회의 직전 머지 — 본인 발화 기회 (R-quiet 강화)
(carry-over 잔여 PO 역할 / 부팀장 / Must 압축은 별도 1:1 통지 권장)
```

#### 2팀 (AM 녹음 없음 → PM에서 AM 의제 자가 진술 1분 권유)
```
[2팀 PM 사전 통지]
1. (회의 첫 안건) 🚨 R-WIP 분담 — 박재민 5건 / 정원화 7건 / 적응 멤버 0건. 김태환/정재훈 페어 분담 권유
2. 🟢 정재훈 R-out-2 본격 해소 + Hilt 가이드 + PR 템플릿 격려 (좋은 점부터)
3. 🟡 김명준 정체 (1건만) — 페어 재매칭 점검
(.gitignore 패턴 추가 + fine-grained PAT 가이드는 별도 통지)
```

#### 3팀 (AM 녹음 없음 → 워터폴 톤 유지 절대 권장)
```
[3팀 PM 사전 통지]
1. (회의 첫 안건) 🚨 Must Meet 6항목 점검 0/6 — 구현 단계 본격 진입 36h+ 미점검. 별도 점검 회의 1회 (Week 3 진입 전) 합의 권유
2. 🟡 정우석 commit 0 (Issue #8/#9 무전 송수신 Must 핵심 담당) — 작업 시작 시점 청취
3. 🟢 서은신 6 commit + 이유빈 첫 commit + 장지은 머저 역할 ⭐⭐ 격려
(워터폴 톤 — MVP 다이어트·Sprint 권고 ❌ 절대 금지)
```

### 변경 2 — 🚦 신호등 1단어 회고 (PM 회의 종료 직전 30초) ⭐⭐

**언제**: PM 회의 종료 직전
**누가**: 강사 + 보조강사 + 팀장/PO 각 1단어
**무엇**: 🔴/🟡/🟢 + 1단어 (예: "🟢 진척", "🟡 부담", "🔴 정체")

→ 같은 색이면 align ✅. 다르면 다음 AM 의제 1순위 자동 격상.

오늘 PM은 보조강사 마이크 이슈 가능성 있으므로 **채팅으로 색 + 1단어 입력** 백업.

### 변경 3 — 🔁 Carry-over 자동 이월 룰 + 이행률 표 (PM 양식에 1표 추가) ⭐

**언제**: PM 양식 작성 시 (15:30)
**어디**: PM 스냅샷 §🔁 Carry-over 섹션

**양식**:

| 항목 | Owner | 마감 | AM 회의에서 다뤘나? | 데이터 상태 | 이월 횟수 |
|---|---|---|---|---|---|
| (각 항목) | (담당) | (기한) | ✅/❌ | (결과) | N회 |

**이행률**: 회의-내 N/M (%) — 1팀 오늘 AM 0/7 (0%) 기준 baseline → PM 목표 ≥30%

**자동 격상**: 이월 2회+ 항목은 §🚨 강사 사전 통지 1순위 + 회의 외 강사 1:1 별도 통지

---

## 2. 오늘 PM 적용 안 할 것 (시간 부족 — 5/15부터 적용)

- §🔍 사전 점검 자동화 명령 (B1) — git 클론 환경 점검 시간 추가 필요. 내일부터
- §🎯 Walking the Board "오늘 끝낼 수 있는 작업 3개" — AM 양식에서만 필수. PM은 §✅ 오전 액션 진척 표로 대체
- §🚧 Phase-gate Must Meet 6항목 헤더 자동 import — 3팀 양식 갱신 필요. 내일부터
- 부록 B/C/D 팀별 분기점 자동 적용 — 양식 무게 늘림. 점진 적용
- 회의 timebox 권장 (AM 7~10분 / PM 10~15분) — 강사 합의 필요. 5/15 강사 1:1 통지 후 적용

---

## 3. 회의 후 갱신 체크리스트 (3분)

1. ✅ §🚨 강사 사전 통지 3건 중 강사 발화된 항목 ✅ 표시
2. ✅ 미언급 항목은 §🌙 내일 AM carry-over에 자동 이월 + 이월 횟수 +1
3. ✅ 신호등 회고 결과 기록 (강사/보조강사/팀장 각 색 + 단어)
4. ✅ 회의 시간 기록 (실제 N분 N초)
5. ✅ 회의 결과 PM 스냅샷에 반영 (`teams-docs/<X>team/snapshots/260514_pm.md`)

---

## 4. 1주 후(5/21 수) 효과 측정

다음 지표로 v2 효과 측정 후 v3 결정:

| 지표 | 오늘 (v1 baseline) | 5/21 (v2 1주 운영 후) | 목표 |
|---|---|---|---|
| 회의-내 carry-over 이행률 | 0% (1팀 0/7) | (측정) | ≥ 30% |
| 회의 평균 시간 | 5'21" (4회) | (측정) | ≥ 7'00" (AM) |
| 24h+ 정체 🚨 항목 | 1건 (R13) | (측정) | 0건 |
| 신호등 align 비율 | — | (측정) | ≥ 70% |
| 강사 사전 통지 채팅 응답률 | — | (측정) | 100% |

→ 5/21 일요일 회고 시 본 표 채워서 v3 결정 (timebox 강제 도입 / 무기명 폼 도입 등).

---

## 5. 참조

- 정식 양식: [meeting_prep_template_v2.md](../../teams-docs/.shared/meeting_prep_template_v2.md)
- 외부 베스트 프랙티스: [external_practices.md](./research/external_practices.md)
- gap 분석: [research/current_pattern_gap.md](./research/current_pattern_gap.md)
- 시뮬레이션 결과: [research/v2_simulation_results.md](./research/v2_simulation_results.md)
- v1↔v2 diff: [research/template_v1_v2_diff.md](./research/template_v1_v2_diff.md)
- SKILL 갱신: [team-check-am](../../.claude/skills/team-check-am/SKILL.md) / [team-check-pm](../../.claude/skills/team-check-pm/SKILL.md)
- 위험 분류 갱신: [risk_taxonomy.md](../../teams-docs/.shared/risk_taxonomy.md) (R-attend / R-burn 추가)
