# 현재 운영 패턴 + Gap 분석 (2026-05-14)

> **목적**: Phase 1 외부 베스트 프랙티스 ↔ 현재 v1 양식·운영 ↔ 사내 가이드(애자일 가이드 / 보조강사 FAQ)의 3자 매트릭스 비교로 양식 v2에 통합할 갭 식별.
> **입력**: [external_practices.md](./external_practices.md), 4개 transcript, 6개 스냅샷, 메모리 4건, 사내 가이드 2건, v1 양식 4건.

---

## 1. 정량 측정 — 현재 회의 운영의 사실

### 1-1. 회의 시간 분포 (n=4, 최근 24h)

| 회차 | 시간 | Atlassian 권장(15분) 대비 |
|---|---|---|
| 1팀 2026-05-13 PM | 4'43" (283초) | **31%** |
| 2팀 2026-05-13 PM | 9'10" (550초) | 61% |
| 3팀 2026-05-13 PM | 3'20" (200초) | **22%** |
| 1팀 2026-05-14 AM | 4'10" (250초) | **28%** |
| **평균** | **5'21" (321초)** | **36%** |

→ **회의 평균이 권장의 1/3 수준**. 단순 timebox 미달이 아니라 **의제 분량 자체가 적음** = 형식화 신호.

### 1-2. Carry-over 회의-내 이행률 (1팀 only — AM transcript 있음)

| 5/13 PM 합의된 carry-over | 5/14 AM 회의에서 다뤘나? | 5/14 AM 09:30 데이터 상태 |
|---|---|---|
| google-services.json 제거 | ❌ 미언급 | 🚨 미해소 |
| 노출 API 키 콘솔 제한 | ❌ 미언급 | ❓ 미확인 |
| PO 역할 명시 합의 | ❌ 미언급 | 🚨 미합의 |
| 부팀장 자리 처리 | ❌ 미언급 | 🚨 미합의 |
| Must 7개 → 5~6개 압축 | ❌ 미언급 | 🚨 미합의 |
| 이제이 활동 R-quiet 확인 | ❌ 미언급 | 🟢 자동 회복 (commit/Issue 데이터로) |
| Sprint 1 백로그 진척 | △ 백로그 단위 지시 (간접) | 부분 진척 |

→ **회의-내 이행률: 0~14% (0건 또는 1건/7건)**. 외부 벤치마크 50~60%(수동 추적)에도 미달.
→ **데이터-기반 자동 진척 1건만 발생** (이제이 자발적 작업). 모든 명시 합의 항목은 회의-매개 없이 progress 0.

### 1-3. R-항목 발현 vs 회의 점검 비대칭

5/13 PM 베이스라인 대비 5/14 AM에서 **신규 발현 또는 악화된 R-항목** (5건):

| R-ID | 발현 | 회의에서 짚었나? |
|---|---|---|
| R13 (1팀 google-services 정체) | 36시간+ 미해소로 악화 | ❌ 강사 본인이 잊음 |
| R-attend (1팀 정섭 결석) | 신규 발현 | ✅ 짚음 (1팀 AM 회의의 유일한 핵심 의제) |
| R-burn-1 (1팀 손지희 부담) | 🟢 → 🟡 명시 경고 | ✅ 강사 명시 우려 |
| R-WIP (2팀 박재민/정원화 assign 12건) | 신규 발현 | ❓ 측정 불가 (2팀 AM 녹음 없음) |
| R-W1 (3팀 Must Meet 미점검) | 36시간 정체 | ❓ 측정 불가 (3팀 AM 녹음 없음) |

→ 강사가 **회의 도중 직접 인식한 항목만 짚힘** (R-attend·R-burn-1). 이전 회의 합의(R13)나 정량 데이터 기반 신호(R-WIP)는 **사전 의제 push 없으면 누락**. 메모리 [team_check_meeting_insights_260513.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team_check_meeting_insights_260513.md) 패턴 재확인.

### 1-4. Single Point of Failure — 보조강사 마이크

5/14 AM transcript 인용:
> 강사: "다른 강사님들도 말씀해 주세요. 정보 강사님? 미니 강사님? 정보 강사님은 무슨 트럭 이슈가 있나? 마이크가."

→ 보조강사 마이크 1건 고장 = **carry-over 7건 전부 누락**. 백업 채널(채팅·사전 1줄 통지 등) 부재.

---

## 2. v1 양식 ↔ 외부 베스트 프랙티스 ↔ 사내 가이드 3자 매트릭스

| 외부 베스트 프랙티스 (Phase 1) | v1 양식([meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md))에 있나? | 사내 가이드에 있나? | Gap 종류 |
|---|---|---|---|
| **Walking the Board** (오늘 끝낼 수 있는 작업 3개) | ❌ | △ 가이드 §4-2 스크럼 사이클에 "Daily 진행 공유" 추상 표현. 보드 워킹 명시 X | **신규 추가** |
| **"Finished / Will finish by when"** 프레임 | ❌ ("📊 18시간 변화" + "🎯 키 포인트" 분리 구조만) | △ 가이드 §10 DoD "PR Merge + AC 충족"만 정의 | **신규 추가** |
| **Carry-over 자동 재출현 (owner + 마감)** | △ "🔁 어제 PM carry-over" 섹션 있음. but **자동 재출현 룰·owner·마감 미명시** | ❌ | **양식 자동화 강화** |
| **신호등 1단어 회고** (R/Y/G 1단어) | ❌ | ❌ | **신규 도입** |
| **Must/Should 분리** (Stage-Gate) | △ 3팀 [team_specific_checks.md §2](../../../teams-docs/3team/review/team_specific_checks.md) 에 phase-gate Must Meet만. PM/AM 양식 본문엔 미노출 | ✅ Brief Must/Should/Won't 명확 ([가이드 §4-4](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)) | **양식 노출 강화** |
| **Pre-mortem 압축 15분 (1·3·5주차)** | ✅ [premortem_template.md](../../../teams-docs/.shared/premortem_template.md) 별도 양식 존재 (주1회 금요일) | ❌ | **압축 변형 추가**(주차별 압축 15분 모드) |
| **강사 사전 통지 1줄 룰** | ❌ | ❌ | **운영 룰 신규** (양식 외부) |
| **주간 무기명 폼** (멋사 모델) | ❌ | ❌ | **부분 적용** (Phase 5에서 결정) |
| **Parking lot 룰** | ❌ | ❌ | **회의 진행 룰 신규** |
| **Timebox 강제** | △ "회의 30분 전 작성" 만, 회의 자체 timebox 강제 X | ❌ | **양식 헤더 추가** |
| **R-attend / R-burn 표준화** | ❌ (1팀 review에 R-burn-1 한정) | ❌ | **risk_taxonomy.md 표준화** |
| **사내 가이드 § 번호 인용** ([operation/CLAUDE.md §8 원칙](../../CLAUDE.md)) | ❌ (양식에 § 인용 칸 없음) | — | **양식 footer에 § 인용 강제** |
| **주차별 "최소 Done" 체크** ([가이드 §4-3 "주차별 최소 Done"](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)) | △ team_specific_checks §8 Week N 가중치만, AM/PM 양식과 분리 | ✅ 가이드에 명확 | **양식 메인에 노출** |
| **§9 안티패턴 식별표** ([FAQ §9](../../../ta-guides/애자일_예제기반_FAQ.md)) | ❌ (별도 문서로만 존재) | ✅ | **양식 사이드 칸에 매핑** |

→ **총 13건 갭**. 그 중 **즉시 양식 통합 가능 9건**, 운영 룰 변경 필요 3건, 메타 가이드 갱신 1건.

---

## 3. v1 양식의 구조적 문제 (Phase 1 발견 + 정량 분석 결합)

### 3-1. "어제 PM ↔ 오늘 AM" 자동 연결이 약함

v1 양식은 AM/PM 각각 독립 작성. carry-over 섹션은 **수동 복붙** 구조. → 이행률 측정 칸 없음 → 형식적 회의 보고로 흘러가는 직접 원인.

**v2 필수**: carry-over 표가 **이전 회차에서 자동 import** + **이행 N/M + % 측정** + **2회 이상 이월된 항목은 자동 격상 🚨**.

### 3-2. "🎯 오늘의 키 포인트" 칸이 보조강사 관점만, 강사 발화 트리거 없음

v1 양식은 보조강사가 작성한 키 포인트가 회의에서 **자동으로 강사 발화로 전환되지 않음**. 보조강사가 마이크 못 잡으면(=오늘 AM처럼) 통째로 사라짐.

**v2 필수**: "🚨 강사 사전 통지 의제 (1줄, 회의 5분 전)" 칸 — 보조강사가 사전에 강사 채팅 또는 1:1로 전달하는 mandatory 항목 3건 압축.

### 3-3. R-항목 표준화가 분산됨

- 공통 [risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md): R1~R14, R-W1~R-W4
- 1팀 [team_specific_checks §6](../../../teams-docs/1team/review/team_specific_checks.md): R-PO, R-burn-1, R-MN, R-DL, R-Must7 (1팀 only)
- 2팀 [team_specific_checks §6](../../../teams-docs/2team/review/team_specific_checks.md): R-out-2, R-WIP, R-burn (2팀 only)
- 3팀 [team_specific_checks §6](../../../teams-docs/3team/review/team_specific_checks.md): R-W5, R-W6 (3팀 only)

→ **R-attend (오늘 새로 발현)**가 어느 문서에도 표준화 안 됨. R-burn은 1팀 R-burn-1 / 2팀 R-burn으로 명칭 분리.

**v2 필수**: risk_taxonomy.md에 **R-attend (출결)**, **R-burn (피로 누적, 팀 무관)** 표준 등록. 팀별 변형은 -1/-2 suffix로.

### 3-4. 회의 timebox 자체가 양식에 안 들어감

v1은 "회의 30분 전 작성" 만 강조. 회의 길이 자체에 대한 권장·강제 없음. 결과 평균 5'21" (권장 15분의 36%).

**v2 필수**: 양식 헤더에 "예상 회의 timebox" 칸 (AM 7~10분 권장 / PM 10~15분 권장) + 회의 종료 후 실제 시간 기록 → **이행률 추적의 leading indicator**.

### 3-5. 사내 가이드 § 번호 인용이 양식에 없음

[operation/CLAUDE.md §8](../../CLAUDE.md) "모든 신호는 가이드 § 근거 명시" 원칙이 있는데 v1 양식엔 § 인용 칸 없음. → 보조강사 권고가 학생에게 **"강사 권한 외 의견"으로 들리는 위험**.

**v2 필수**: 각 R-항목 / 액션에 [`가이드 §N`](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) 또는 [`FAQ §N`](../../../ta-guides/애자일_예제기반_FAQ.md) 인용 칸 — 사내 권한 근거 확보.

---

## 4. 팀별 차이 — v2 양식이 분기점 가져야 할 위치

| 팀 | 방법론 | 인원 | 핵심 분기점 |
|---|---|---|---|
| 1팀 (Scoffee) | 애자일 | 3명 (5/12부) | **인원 변경 후속** carry-over 가중치 ↑ / R-PO·R-burn-1·R-Must7 가중치 ↑ / R-attend 활성 |
| 2팀 (Umma) | 애자일 | 5명 (적응 멤버 2명 포함) | **R8 단일 의존 / R-WIP / R-out-2** 가중치 ↑ / Mock-first 컨벤션 점검 |
| 3팀 (BBip) | 워터폴 | 4명 | **Must Meet 6항목 점검**이 매 회의 헤더 / Sprint·MVP 다이어트 권고 ❌ / R-W1·W4·W5·W6 활성 |

→ **공통 80% + 팀별 부록 20% 구조**로 v2 설계. 분기점은 다음 3개:
1. **R-항목 칸**: 공통 R1~R14 + 팀별 추가 R 자동 import
2. **Top 5 매 회의 점검**: 팀별 review/team_specific_checks.md §1 자동 인용
3. **방법론 톤 가드**: 3팀 양식에 "❌ Sprint/MVP 다이어트 권고 금지" 명시 (메모리 [team3_methodology.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md))

---

## 5. v2 양식이 해소할 갭 — 우선순위표

| 우선순위 | 갭 | v2 양식 어떤 항목으로 해소 | 외부 근거 | 사내 근거 |
|---|---|---|---|---|
| P0 | Carry-over 회의-내 이행률 0% | "🔁 carry-over" 표에 owner+마감+자동 재출현 룰 + 이행률 N/M % 계산 칸 | [Phase1 §0-6](./external_practices.md#0-요약--핵심-발견-7건) | — |
| P0 | 보조강사 마이크 SPOF | "🚨 강사 사전 통지 의제 3건 (회의 5분 전 강사 전달)" 칸 — 채팅/1:1 백업 채널 명시 | Phase1 §5 (백채널 분업) | — |
| P0 | R13 같은 critical 항목을 강사가 잊음 | "🚨 즉시 조치" 칸을 양식 **최상단**으로 이동 + 24h+ 미해소시 자동 강조 | Phase1 §1-3 (안티패턴) | — |
| P0 | Walking the Board 부재 | AM 양식에 "오늘 끝낼 수 있는 작업 3개 (보드 오른쪽)" 칸 신설 | Phase1 §1-2 | 가이드 §4-2 스크럼 사이클 |
| P0 | Finished/Will finish 프레임 부재 | "📊 변화" 칸을 "Finished (어제 PM 이후) / Will finish by when" 2분할 | Phase1 §1-1 | — |
| P1 | 신호등 1단어 회고 부재 | PM 양식 마지막 1줄 "🚦 오늘 신호등: R/Y/G + 한 단어" | Phase1 §4-2 | — |
| P1 | 3팀 Must Meet 점검 양식 noise | 3팀 양식에 phase-gate 6항목 헤더 자동 import | Phase1 §2 | 3팀 [team_specific_checks §2](../../../teams-docs/3team/review/team_specific_checks.md) |
| P1 | R-attend·R-burn 표준화 부재 | risk_taxonomy.md에 R-attend / R-burn (공통) 추가 후 양식에 자동 노출 | Phase1 §3-2 | — |
| P1 | 사내 § 인용 부재 | 양식 footer에 "근거 § / FAQ §" 인용 칸 | — | operation/CLAUDE.md §8 |
| P1 | 주차별 최소 Done 칸 부재 | 양식 헤더에 "Week N — 최소 Done 1줄" 자동 import (가이드 §4-3) | — | 가이드 §4-3 |
| P2 | Timebox 강제 부재 | 양식 헤더 "예상 timebox: AM 7~10분 / PM 10~15분" + 회의 후 실제 시간 기록 | Phase1 §1-1 | — |
| P2 | 안티패턴 식별표 미연동 | 양식 사이드 칸에 FAQ §9 표 자동 매핑 (해당 R-항목이 있으면 자동 표시) | — | FAQ §9 |
| P2 | Pre-mortem 압축 변형 부재 | premortem_template.md에 "주차별 15분 압축 변형" 추가 (1·3·5주차) | Phase1 §4-1 | — |
| P2 | 주간 무기명 폼 부재 | 운영 룰 신규 — **양식 외부**, 별도 운영 결정 필요 | Phase1 §3, §5 | — |

→ **P0 5건이 양식 v2의 핵심 변경**. P1 5건은 표준 통합. P2 4건은 양식 외부 또는 부수 변경.

---

## 6. 갭 분석 결과 요약 — 사용자 확인 항목

### 6-1. 명확한 갭 (확정)
1. ✅ Carry-over 0~14% 이행률 — 자동화 필요 (P0)
2. ✅ 보조강사 마이크 SPOF — 백업 채널 필요 (P0)
3. ✅ Walking the Board / Finished-by-when 프레임 부재 (P0)
4. ✅ 신호등 회고 부재 (P1)
5. ✅ R-attend / R-burn 표준화 부재 (P1)

### 6-2. 사용자 결정 필요 (Phase 3 진입 전 합의 필요)
1. ❓ **주간 무기명 폼 도입 여부** — 보조강사 1인 운영에서 폼 응답 처리 시간 추가 필요. 도입할지 / 보류할지
2. ❓ **팀별 분기점 처리 방식** — 공통 80% + 부록 20% 구조 OK? 혹은 팀별 완전 분리 양식?
3. ❓ **회의 timebox 강제 톤** — AM 7~10분 / PM 10~15분 권장이 강사에게 받아들여질지? (현재 평균 5'21")
4. ❓ **사내 § 인용 칸 강제 수준** — 보조강사 권고마다 § 인용 의무화는 작성 시간 증가. 강제 vs 권장?

---

## 7. 참조

- Phase 1: [external_practices.md](./external_practices.md)
- v1 양식: [meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md), [daily_check_method.md](../../../teams-docs/.shared/daily_check_method.md), [risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md), [premortem_template.md](../../../teams-docs/.shared/premortem_template.md)
- 팀별 점검: [1팀](../../../teams-docs/1team/review/team_specific_checks.md), [2팀](../../../teams-docs/2team/review/team_specific_checks.md), [3팀](../../../teams-docs/3team/review/team_specific_checks.md)
- 사내 가이드: [애자일 본 가이드](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md), [보조강사 FAQ](../../../ta-guides/애자일_예제기반_FAQ.md)
- 운영 지침: [operation/CLAUDE.md](../../CLAUDE.md)
- 메모리: 4건 (인원 변경 / 워터폴 / 회의 패턴 / R-burn 발현)
- 실측 데이터: 4 transcripts + 6 snapshots (2026-05-13 PM ~ 2026-05-14 AM)
