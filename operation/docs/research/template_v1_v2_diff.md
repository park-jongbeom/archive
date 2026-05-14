# v1 → v2 양식 차이점 요약 (2026-05-14)

> v1: [meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md)
> v2: [meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md)
> 근거: [external_practices.md](./external_practices.md) + [current_pattern_gap.md](./current_pattern_gap.md)

---

## 1. 추가된 섹션 (v2 신규)

| 섹션 | 위치 | 목적 | 근거 |
|---|---|---|---|
| 🚨 강사 사전 통지 의제 3건 | AM/PM 최상단(§2) | 보조강사 마이크 SPOF 백업 채널 | gap §3-2 (5/14 AM 마이크 이슈로 carry-over 7건 전부 누락) |
| 🎯 오늘 끝낼 수 있는 작업 3개 (Walking the Board) | AM §5 | 사람 보고 → 작업 보고 전환 | Phase1 §1-2 |
| 📊 Finished / Will finish by when | AM/PM §4 | 노력이 아닌 완료에 초점 | Phase1 §1-1 |
| 🔁 Carry-over 자동 재출현 + 이행률 N/M% | AM/PM §6 | 회의-내 이행률 측정 가능화 | Phase1 §4-3 (44% 미완 폐기 통계) |
| ✅ 오전 회의 액션 진척 표 | PM §5 | 오전 합의 → 오후 검증 | Phase1 §4-3 |
| 🚦 신호등 1단어 회고 (R/Y/G + 1단어) | PM §13 | R-burn 감지 + 회의 형식화 깨기 | Phase1 §4-2 (요즘IT) |
| 🌙 자동 이월 + 2회+ 격상 룰 | PM §14 | 이월 항목이 사라지지 않게 강제 | Phase1 §4-3 |
| 부록 A — R-attend / R-burn 표준 R-항목 | §5 | 출결·피로 누적을 공통 R-항목으로 격상 | 5/14 1팀 정섭 결석 + 손지희 발현 |
| 부록 B/C/D — 팀별 특화 | §6/7/8 | 공통 80% + 부록 20% 구조 | gap §4 (팀 분기점 3개) |

---

## 2. 변경된 섹션 (v2 갱신)

| 항목 | v1 | v2 |
|---|---|---|
| 🚨 즉시 조치 위치 | 양식 중간 (`🟡 주의 항목` 다음) | **최상단** 이동 (강사 망각 방지) |
| Carry-over 섹션 | "🔁 어제 PM carry-over 항목 진척 점검" — 단순 텍스트 | 표 + owner + 마감 + 이월 횟수 + 이행률 % |
| 보조강사 권장 액션 | 1~2개 자유 형식 | **3건 압축** 강제 + 좋은 점 1건 포함 원칙 |
| 인물별 활동표 | 24h commit/Issue/PR | + 24h 머지 + 7일 누적 + **회의 발화 ✅/❌** + R-quiet flag |
| 회의 timebox | "회의 30분 전 작성" 만 | 양식 헤더에 권장 시간 + 회의 후 실제 시간 기록 |
| 주차별 최소 Done | team_specific_checks §8에 분리 | 양식 헤더에 자동 import (가이드 §4-3) |
| 사내 § 인용 | 없음 | "(선택) 📚 근거 인용" footer — 신규 R-항목 도입시만 |

---

## 3. 유지된 섹션 (변경 없음)

- 🟢 한 줄 요약
- 📊 정량 수치 (commit/Issue/Figma)
- 📋 Issue↔PR 정합성
- 🎨 FigJam/Design 보드 변화
- 📁 저장 경로 규약
- 작성 톤 (사실 → 평가 → 액션, 인물 비판 X, 좋은 점부터)

---

## 4. 양식 외부 변경 (운영 룰)

| 변경 | 적용 시점 | 비고 |
|---|---|---|
| 강사 사전 통지 채널 3단계 (1:1 채팅 / 카톡 / 화이트보드) | 즉시 (5/14 PM부터) | 보조강사 마이크 SPOF 백업 |
| 회의 timebox 권장 (AM 7~10분 / PM 10~15분) | 권장 (강제 X) | 평균 5'21" → 점진 보강 |
| 신호등 회고 (PM 마지막 30초) | PM 5/14부터 | 강사·보조강사·팀장 각 1단어 |

---

## 5. 양식 외부 — 후속 작업 필요 (Phase 5)

| 작업 | 파일 | 우선순위 |
|---|---|---|
| R-attend / R-burn 표준 등록 | [risk_taxonomy.md §1-A](../../../teams-docs/.shared/risk_taxonomy.md) | P1 |
| 1팀 R-burn-1 → R-burn-team1 명칭 정리 | [1팀 team_specific_checks §6](../../../teams-docs/1team/review/team_specific_checks.md) | P2 |
| 2팀 R-burn → R-burn-team2 명칭 정리 | [2팀 team_specific_checks §6](../../../teams-docs/2team/review/team_specific_checks.md) | P2 |
| Pre-mortem 압축 15분 변형 추가 | [premortem_template.md](../../../teams-docs/.shared/premortem_template.md) | P2 |
| team-check-am SKILL 양식 참조 v2로 갱신 | [skills/team-check-am/SKILL.md](../../../.claude/skills/team-check-am/SKILL.md) | P0 (Phase 5) |
| team-check-pm SKILL 양식 참조 v2로 갱신 | [skills/team-check-pm/SKILL.md](../../../.claude/skills/team-check-pm/SKILL.md) | P0 (Phase 5) |

---

## 6. v2 효과 측정 지표 (1주 후 회고용)

| 지표 | v1 (5/13 PM ~ 5/14 AM 실측) | v2 목표 (1주 후) |
|---|---|---|
| 회의-내 carry-over 이행률 | 0% (1팀 측정 가능 분, 0/7) | **≥ 30%** |
| 회의 평균 시간 | 5'21" (4회 평균) | **≥ 7'00"** (AM) |
| 24h+ 정체 🚨 항목 | 1건 (R13 google-services) | **0건** |
| 인물별 R-quiet 발생 | 측정 안 됨 | 매 회의 명시 + 1주 후 50% 감소 |
| 신호등 회고 align 비율 | — | **≥ 70%** (강사·보조강사 같은 색) |

→ 5/19 (월) 1주 회고 시점에 본 표 채워서 v3 결정.

---

## 7. 적용 안 한 P2 항목 (보수적 기본값으로 보류)

| P2 항목 | 보류 이유 | 재검토 시점 |
|---|---|---|
| 주간 무기명 폼 (멋사 모델) | 보조강사 1인 폼 응답 처리 시간 부담 | 1주 운영 후 결정 |
| 회의 timebox 양식 내 강제 | 강사 현재 운영 톤과 마찰 가능 | v3 (3주 후 결정) |
| 사내 § 인용 의무화 | 작성 시간 증가, 권장만 | v3 |
| 안티패턴 식별표 자동 매핑 | 자동화 복잡 + 매뉴얼 매핑이 더 정확 | 추후 |
