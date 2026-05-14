---
name: team-check-pm
description: "1/2/3팀 오후 4시 팀 미팅 사전 준비. 오늘 10:00 ~ 15:30 변화량 + 오전 액션 진척 추적 + PM 양식 v2 작성 + 신호등 1단어 회고. 사용법: /team-check-pm <팀번호> (1/2/3). 보조강사 관점."
effort: medium
---

# team-check-pm — 오후 4시 팀 미팅 사전 준비 (v2 양식)

> **양식**: [meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) (2026-05-14부 정식 적용)

## 사용법

```
/team-check-pm 1     ← 1팀 오후 점검
/team-check-pm 2     ← 2팀
/team-check-pm 3     ← 3팀
```

---

## AM과의 차이 (핵심)

| 항목 | AM (10시) | PM (4시) |
|---|---|---|
| **분석 윈도우** | 18시간 (어제 PM ~ 오늘 AM) | **6시간** (오늘 AM ~ 현재) |
| **강조점** | 오늘 어떻게 시작할지 | **오전 결정 진척 + 오후 마무리 + 신호등 회고** |
| **carry-over** | 어제 PM 액션 | 어제 PM + **오늘 AM 액션 모두** import |
| **신호등 1단어 회고** | 없음 | 회의 종료 직전 30초 (강사·보조강사·팀장 각 R/Y/G + 1단어) |

→ PM은 **"오전 회의에서 합의한 것이 실제로 진행됐는가"** + **"오늘 일과 종료 신호등 align"** 두 가지를 가장 우선 점검.

---

## 절차 (순서 엄수, v2 적용)

### Step 1. 인자 파싱

AM과 동일. 팀별 경로 매핑은 [team-check-am SKILL](../team-check-am/SKILL.md) Step 1 참조.

### Step 2. 컨텍스트 로드 (순서 엄수)

다음 7개 파일을 **이 순서대로** 읽는다:

1. `teams-docs/.shared/daily_check_method.md`
2. `teams-docs/.shared/meeting_prep_template_v2.md` — **현재 적용 양식** (v2)
3. `teams-docs/<X>team/review/context.md`
4. `teams-docs/<X>team/review/team_specific_checks.md`
5. `teams-docs/.shared/risk_taxonomy.md` — R1~R14 + R-attend·R-burn 표준 + 3팀 R-W1~W4
6. **`teams-docs/<X>team/snapshots/<오늘날짜>_am.md`** ← 오늘 AM 스냅샷 (가장 중요한 비교 기준 + carry-over 자동 import 원천)
7. (전날 PM 스냅샷이 있으면 reference로 가볍게)

⚠️ **오늘 AM 스냅샷이 없으면** 사용자에 알리고 다음 중 선택:
- (a) AM 스냅샷 없이 진행 (직전 스냅샷과 비교)
- (b) 사용자가 AM 회의 결과를 1분 정도 구두 진술 → 그 내용을 carry-over에 반영
- (c) 본 호출 종료

**팀별 메모리 참조** (AM과 동일):
- 1팀: team1_personnel_change_260512.md, team1_burn_attend_260514_am.md
- 3팀: team3_methodology.md (워터폴 톤 절대 유지)

### Step 3. 사전 점검 자동화 명령 (B1)

```bash
cd <팀 로컬 경로>
git fetch --all --prune 2>&1 | tail -3

# 3-1. 6시간 윈도우 (오늘 09:30 ~ 현재)
git log --all --since="6 hours ago" --pretty=format:"%ai | %an | %h | %s"

# 3-2. 오전 활동
git shortlog -sne --since="6 hours ago" origin/develop

# 3-3. 보안 재점검 (R13/R14 — AM에서 미해소된 항목 추적)
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$"

# 3-4. 신규 PR (오전 회의 이후)
# WebFetch: github.com/...../pulls?q=is%3Apr+created%3A>=YYYY-MM-DD

# 3-5. Figma lastModified 비교
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
curl -s -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/<fileKey>?depth=1"
# AM 스냅샷의 Figma lastModified와 비교 → 오전 회의 후 갱신 있었는지
```

### Step 4. 진단 (PM 핵심 우선순위 4가지)

**(a) ✅ 오전 액션 아이템 진척** ← 가장 중요
AM 스냅샷의 §💡 보조강사 권장 액션 + §🚨 강사 사전 통지 + 회의 메모에서 추출. 각 액션의 현재 상태:
- 🟢 완료 (실제 commit/PR/Issue/Figma 변화로 검증됨)
- 🟡 진행 중 (착수 흔적 있음)
- 🚨 미시작 (5시간 지났는데 흔적 없음 — 점심 1시간 차감 = 실 작업 4시간 고려)

→ **미시작 비율 ≥ 50%** = **R-action** (PM에서 다시 한번 push 필요).

**(b) Carry-over 자동 import + 이행률** (B2 룰):
- 어제 PM + 오늘 AM 미해소 항목 모두 import
- 회의-내 이행률 = N/M 계산
- **이월 2회+ 항목은 자동 🚨 + 회의 외 강사 1:1 별도 통지 후보**

**(c) 신규 위험 신호** — 오후에 새로 발견된 R-항목 (특히 R-attend / R-burn 변화 추적):
- 오전 결석자의 오후 복귀 여부
- 오전 R-burn-1 우려가 오후 어떻게 변화했는지

**(d) 인물별 오전 활동** — AM에서 🚨/🟡 표시됐던 멤버가 오후 들어 변화 있었는지 + 오전 회의 발화 정합성

### Step 5. PM 양식 v2 작성 (14개 섹션)

`teams-docs/.shared/meeting_prep_template_v2.md §2 PM 양식` 사용. **순서 엄수**:

1. **🔍 사전 점검 자동화 명령 결과** (B1 — Step 3 grep 결과)
2. **🚨 즉시 조치 필요** (최상단 — AM 후 새 발현 또는 미해소 24h+)
3. **🚨 강사 사전 통지 의제 3건** (회의 5분 전 강사 채팅 — 15:55):
   - **1번 의제는 "(회의 첫 안건 권유)" 명시** — B3
   - **carry-over 2회+ 이월 항목은 자동 1순위 + 회의 외 강사 1:1 별도 통지** — B2
4. 🟢 한 줄 요약 (오전 회의 후 변화)
5. 📊 6시간 변화 — **Finished (오전 회의 후) / Will finish by when (오늘 일과 종료 = 18:00 기준)**
6. **✅ 오전 회의 액션 아이템 진척** (Step 4-a 결과 표) ← 가장 중요
7. **🔁 Carry-over 자동 재출현 + 이행률** (어제 PM + 오늘 AM 미해소 모두)
8. 🚦 R-항목 점검 (오전 09:30 → 오후 15:30 변화 강조)
9. 👤 인물별 오전 활동 + 오전 회의 발화 정합성
10. 💡 보조강사 권장 액션 (3건 압축, 좋은 점 1건 포함)
11. **🌙 내일 AM carry-over 자동 이월** (2회+ 격상 룰 적용 — 자동 🚨 표기)
12. **🚦 오늘 신호등 1단어 회고** (회의 종료 직전 30초)
    - 강사: 🔴/🟡/🟢 + 1단어 (예: "🟢 진척", "🟡 부담", "🔴 정체")
    - 보조강사: 동일
    - (선택) 팀장/PO: 동일
    - **같은 색이면 align ✅. 다르면 내일 AM 의제 1순위 자동 격상**
13. (3팀 한정) **🚧 Phase-gate Must Meet 6항목 충족률**
14. (선택) 📚 근거 인용 — 신규 R-항목 도입시만

### Step 6. 스냅샷 저장

```
teams-docs/<X>team/snapshots/<YYMMDD>_pm.md
```

### Step 7. 사용자에 보고 (강사 사전 통지 + 오전 액션 진척 우선 노출)

사용자 출력 순서:

1. **§🚨 강사 사전 통지 의제 3건** — 본문 최상단 별도 박스 (15:55 채팅 전달용)
2. **§✅ 오전 회의 액션 아이템 진척 표** — 본문 두 번째 (PM 핵심)
3. 🚨 즉시 조치 항목 (있으면)
4. 한 줄 요약 + 6시간 변화
5. carry-over 이행률 + 이월 2회+ 자동 격상 항목 + **회의 외 1:1 별도 통지 항목**
6. 인물별 오전 활동 비교
7. 신호등 회고 양식 (회의 종료 직전 채팅 백업 권장)
8. 내일 AM carry-over 항목 명확히
9. 출력 끝 표준 멘트

### Step 8. 회의 후 갱신 (회의 종료 후 5분)

- §🚨 강사 사전 통지 3건 ✅/❌ 표시
- 신호등 회고 결과 기록 (강사/보조강사/팀장 각 색 + 단어)
  - 같은 색이면 align ✅ / 다르면 내일 AM 1순위 자동 격상
- 회의 실제 시간 기록
- §🌙 내일 AM carry-over 확정 + 이월 횟수 +1
- 회의록(mom) 작성 트리거 조건 점검 ([team-meeting-transcribe SKILL Step 6](../team-meeting-transcribe/SKILL.md))

---

## 출력 끝 표준 멘트

```
---
✅ 스냅샷 저장: teams-docs/<X>team/snapshots/<YYMMDD>_pm.md

📋 회의 5분 전 (15:55) — §🚨 강사 사전 통지 의제 3건을 강사 채팅/1:1로 전달.

⚠️ Carry-over 2회+ 이월 N건 — 회의와 별개로 강사 1:1 별도 통지 권장 (B2).

🚦 회의 종료 직전 30초 — 신호등 1단어 회고. 마이크 이슈 시 채팅 백업.

회의 후: 신호등 결과 + 강사 발화 항목 ✅ 반영 → §🌙 내일 AM carry-over 확정.
오늘 일과 종료 — 내일 오전 `/team-check-am <팀번호>` 호출 (직전 비교 기준: 본 PM 스냅샷).
```

---

## 추가: 금요일 PM 호출 시 (Pre-mortem 권장)

금요일 PM 점검 직후 사용자에게 다음 안내:

```
💡 금요일입니다. 주1회 사전 부검 권장:
   - 정식 양식: [.shared/premortem_template.md](../../../teams-docs/.shared/premortem_template.md) (20~30분)
   - 압축 변형 (1·3·5주차): 15분 ([Phase1 §4-1 근거](../../../operation/docs/research/external_practices.md))
```

---

## 주의 사항

### 톤 / 운영 룰
- **AM 스냅샷 없으면 비교 기준 부실** — 가능한 한 매일 AM부터 절차 시작. 없으면 사용자 구두 진술로 carry-over 복원.
- 오전 합의가 5시간 지나도 흔적 0 = 🚨. 단, **점심 시간(12~13) 차감 = 실 작업 4h** 고려.
- **3팀은 워터폴** — Sprint/mock-first/MVP 다이어트 권고 ❌.
- **좋은 점부터 1건 이상** ([FAQ §10](../../../ta-guides/애자일_예제기반_FAQ.md)).

### v2 핵심 룰
- **B1**: 사전 점검 grep 결과를 §🔍에 직접 명시.
- **B2**: carry-over 2회+ 자동 격상 + 회의 외 강사 1:1 별도 통지.
- **B3**: §🚨 강사 사전 통지 1번 의제 = "(회의 첫 안건 권유)" 명시.
- **신호등 회고**: 회의 종료 직전 30초. 마이크 이슈 시 채팅 백업으로 색 + 1단어 입력.

### PM 고유 룰
- **§✅ 오전 액션 진척**이 본문 최상단 노출. AM 양식과 차별점.
- **§🌙 내일 AM carry-over**의 이월 2회+ 항목 = 내일 AM 양식의 §🚨 강사 사전 통지 1순위 자동 carry.

---

## 참조

### 양식 / 점검 항목
- [team-check-am SKILL](../team-check-am/SKILL.md) — 자매 SKILL
- [.shared/meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) — **현재 적용 양식**
- [.shared/meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md) — v1 참조용
- [.shared/daily_check_method.md](../../../teams-docs/.shared/daily_check_method.md)
- [.shared/risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md) — R-attend·R-burn 표준 포함
- [.shared/premortem_template.md](../../../teams-docs/.shared/premortem_template.md) — 주1회 사전 부검

### 사내 가이드
- [final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)
- [ta-guides/애자일_예제기반_FAQ.md](../../../ta-guides/애자일_예제기반_FAQ.md)

### v2 설계 근거
- [operation/docs/research/external_practices.md](../../../operation/docs/research/external_practices.md)
- [operation/docs/research/current_pattern_gap.md](../../../operation/docs/research/current_pattern_gap.md)
- [operation/docs/research/v2_simulation_results.md](../../../operation/docs/research/v2_simulation_results.md)
- [operation/docs/research/template_v1_v2_diff.md](../../../operation/docs/research/template_v1_v2_diff.md)
- [operation/docs/meeting_template_v2_quick_apply.md](../../../operation/docs/meeting_template_v2_quick_apply.md) — 즉시 적용 가이드
