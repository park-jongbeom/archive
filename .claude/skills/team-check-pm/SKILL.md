---
name: team-check-pm
description: "1/2/3팀 오후 4시 팀 미팅 사전 준비. 오늘 10:00 ~ 15:30 변화량 + 오전 액션 진척 추적 + PM 양식 v2 작성 + 신호등 1단어 회고. 사용법: /team-check-pm <팀번호> (1/2/3). 보조강사 관점."
effort: medium
---

# team-check-pm — 오후 4시 팀 미팅 사전 준비 (v2 양식)

> **양식**: [meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) (2026-05-14부 정식 적용)
>
> **v2.3 갱신 (2026-05-14)**: 캡스톤 6주 단계별 체크리스트 추가. Step 4-(f) + Step 5-3 캡스톤 우선순위 + 신규 §캡스톤 6주 단계별 체크리스트. 근거: [ta-guides/파이널프로젝트_캡스톤_심화_레퍼런스_2026-05-14.md](../../../ta-guides/파이널프로젝트_캡스톤_심화_레퍼런스_2026-05-14.md)

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

다음 8개 파일을 **이 순서대로** 읽는다:

1. `teams-docs/.shared/daily_check_method.md`
2. `teams-docs/.shared/meeting_prep_template_v2.md` — **현재 적용 양식** (v2.1)
3. `teams-docs/<X>team/review/context.md`
4. `teams-docs/<X>team/review/team_specific_checks.md`
5. `teams-docs/.shared/risk_taxonomy.md` — R1~R14 + R-attend·R-burn 표준 + 3팀 R-W1~W4
6. **`teams-docs/<X>team/snapshots/<오늘날짜>_am.md`** ← 오늘 AM 스냅샷 (가장 중요한 비교 기준 + carry-over 자동 import 원천)
7. **`teams-docs/<X>team/mom/<오늘날짜>*`** ⭐ v2.1 신규 — **사용자가 수기로 작성한 팀 회의록** (오늘 분담 cross-check 기준. 오전 회의 후 갱신됐을 수 있음)
   - Glob: `ls teams-docs/<X>team/mom/ | grep <YYMMDD>`
   - 파일 없으면 "오늘 mom 미작성" 명시 + §🗂️ 섹션에서 mom 컬럼 비워둠
8. **`teams-docs/<X>team/backlog/backlog_<YYMMDD>_pm.pdf`** ⭐ v2.2 신규 — **GitHub Project Kanban 보드 PDF** (사용자가 PM 회의 40분 전 = 15:20 추출)
   - Read 도구로 PDF 자동 파싱
   - **오전 PDF(`backlog_<YYMMDD>_am.pdf`)도 같이 로드** → 오전 09:20 → 오후 15:20 보드 변화 비교 (5h+ 정체 카드 = R-stall 격상)
   - PDF 없으면 사용자에 1회 알림
9. (전날 PM 스냅샷이 있으면 reference로 가볍게)

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

# 3-6. GitHub Project 보드 PDF 변화 추적 (v2.2)
# AM PDF (backlog_<YYMMDD>_am.pdf) vs PM PDF (backlog_<YYMMDD>_pm.pdf) 비교
# 오전 09:20 In Progress 카드가 오후 15:20에도 In Progress = 5h+ 정체 → R-stall
# WIP 한도 위반이 오전 → 오후 악화되었는지 추적
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

**(e) 🗂️ 보드 PDF + mom + 오전 commit 3중 cross-check** ⭐ v2.2 — PM 변형:
- **AM PDF + PM PDF 비교** (Read 도구로 둘 다 로드):
  - 오전 09:20 → 오후 15:20 컬럼별 카드 수 변화 (예: In Progress 7→6 = 1건 finish / 7→8 = 1건 추가 시작)
  - 오전 In Progress 카드가 오후에도 동일 = 5h+ 정체 → **R-stall 격상**
  - WIP 한도 위반 (오전 → 오후) 악화 여부
- 오늘 mom (오전 회의 후 갱신됐을 수 있음) + 오전 commit (6h 윈도우) 매칭
- 오전 회의 합의 task가 보드에 안 올라옴 = R-action 미시작 직접 증거
- AM §🗂️ 정합성 신호와 동일 (R-quiet / R-WIP / R8 등)

**(f) 🎯 캡스톤 단계별 체크 (6주 파이널 특화)** ⭐ v2.3 신규 — 캡스톤 심화 레퍼런스 기반:

오늘 날짜에서 파이널 프로젝트 Week N을 계산한 뒤, 아래 단계별 체크 중 **현 주차 + 직전·다음 주차** 항목을 우선 점검:

- **공통 (매일 PM 필수)**:
  - **PR 24h SLA 위반 PR 카운트**: PR 올린 시각 기준 24h 경과 후 1차 피드백 없는 PR 수. 1건 이상 = R-pr-sla (회의 외 강사 escalate)
  - **AC fake done 위험**: 머지된 PR 중 보조강사가 AC 미확인한 건. Done 판정 = 학생 자체평가 X, **보조강사 확인 필수**(Rico & Sayani, Mahnic 2012)
  - **Walking Skeleton 무결성**: end-to-end 1줄 동작이 깨지지 않았는지 (회귀 점검, Cockburn 2004)
  - **데모 가능성**: "Week 1부터 매주 데모 가능"이 의뢰인 가이드 룰. 현 주차 데모 시나리오 작성 여부

- **Week별 특이 체크** (Tuckman F-S-N-P-A + Mahnic 2012 + ASEE 모바일 + 의뢰인 가이드):
  - **Week 1 (Forming)**: Inception Deck NOT List 작성 / Wireframes·Schema·JSON contract 3종 / Friday까지 Walking Skeleton end-to-end 동작
  - **Week 2 (Storming)**: 역할·그라운드룰 확정 / conflict 정상화 공지 / **Sprint 1–2 추정 underrun 40-60%는 정상**임을 학생에게 미리 고지
  - **Week 3 (Norming)**: CATME 5축 1차 동료평가 / 페어 매칭 안정화 / 그라운드룰 마지막 조정
  - **Week 4 (Performing 진입) ★ R-burn 최빈 시점**: 학생 자신감·추정 정확도 수렴 시작 (Mahnic 2012 sprint 4). **보조강사 stance: Coach → Impediment Remover 전환**. Storming 잔열 점검
  - **Week 5 (Performing)**: 데모 리허설 T-3 그룹 피드백 (YC 가이드) / Won't-Have 강제 결정 / 신규 must 요구는 기존 must를 won't로 강등하는 거래 형식 강제
  - **Week 6 (Adjourning/Freeze)**: 기능 추가 완전 중단 / README·QA 시나리오 / 데모 리허설 2회+ / **scrcpy 미러링 검증 + 30초 mp4 백업** (ASEE 모바일 라이브 실패율 3× 대응) / CATME 2차 + RepoSense 기여 시각화

- **scope creep 신호 (전 주차)**:
  - 새 must 추가 시 기존 must의 won't 강등 거래가 있었는지 (Issue 라벨 변경 이력 확인)
  - PMI 92% 통계: scope creep 미관리가 92% 실패 요인. AC 새 요구 추적 강제

- **모바일 특화 체크 (전 주차, Week 5+ 강화)**:
  - 데모 디바이스 단일화 vs 다양화 결정 여부
  - scrcpy USB tether 미러링 setup 확인
  - 데모 백업 mp4 30초 녹화 준비

→ 위 체크에서 발견된 항목 중 **AM에서 다루지 않은 신규 신호**는 §🚨 강사 사전 통지 의제 후보로 자동 격상.

### Step 5. PM 양식 v2 작성 (14개 섹션)

`teams-docs/.shared/meeting_prep_template_v2.md §2 PM 양식` 사용. **순서 엄수**:

1. **🔍 사전 점검 자동화 명령 결과** (B1 — Step 3 grep 결과)
2. **🚨 즉시 조치 필요** (최상단 — AM 후 새 발현 또는 미해소 24h+)
3. **🚨 강사 사전 통지 의제 3건** (회의 5분 전 강사 채팅 — 15:55):
   - **1번 의제는 "(회의 첫 안건 권유)" 명시** — B3
   - **carry-over 2회+ 이월 항목은 자동 1순위 + 회의 외 강사 1:1 별도 통지** — B2
   - **캡스톤 의제 우선순위 (v2.3 신규)** — 동급 신호가 여럿일 때 다음 순서로 격상:
     1. **PR 24h SLA 위반** (R-pr-sla) — 즉시 escalate, 회의 1순위
     2. **AC fake done 의심 PR** — 보조강사 검증 안 된 머지가 있을 때
     3. **Walking Skeleton 회귀** — Week 1 이후 end-to-end 깨짐
     4. **Week 4 부근 R-burn-1 + 보조강사 stance 전환 필요 신호** (Tuckman Storming→Performing)
     5. **scope creep + Won't-Have 거래 미실행** (Week 3+)
     6. **Week 6 데모 리허설·백업 mp4·scrcpy 미점검** (Week 5+)
4. 🟢 한 줄 요약 (오전 회의 후 변화)
5. 📊 6시간 변화 — **Finished (오전 회의 후) / Will finish by when (오늘 일과 종료 = 18:00 기준)**
6. **✅ 오전 회의 액션 아이템 진척** (Step 4-a 결과 표) ← 가장 중요
7. **🗂️ 보드 In Progress + 분담 cross-check (PM 변형)** ⭐ v2.1 — Step 4-e 결과. 오전 → 오후 보드 변화 + 5h+ 정체 카드 식별
8. **🔁 Carry-over 자동 재출현 + 이행률** (어제 PM + 오늘 AM 미해소 모두)
9. 🚦 R-항목 점검 (오전 09:30 → 오후 15:30 변화 강조)
10. 👤 인물별 오전 활동 + 오전 회의 발화 정합성
11. 💡 보조강사 권장 액션 (3건 압축, 좋은 점 1건 포함)
12. **🌙 내일 AM carry-over 자동 이월** (2회+ 격상 룰 적용 — 자동 🚨 표기)
13. **🚦 오늘 신호등 1단어 회고** (회의 종료 직전 30초)
    - 강사: 🔴/🟡/🟢 + 1단어 (예: "🟢 진척", "🟡 부담", "🔴 정체")
    - 보조강사: 동일
    - (선택) 팀장/PO: 동일
    - **같은 색이면 align ✅. 다르면 내일 AM 의제 1순위 자동 격상**
14. (3팀 한정) **🚧 Phase-gate Must Meet 6항목 충족률**
15. (선택) 📚 근거 인용 — 신규 R-항목 도입시만

### Step 6. 스냅샷 저장

```
teams-docs/<X>team/snapshots/<YYMMDD>_pm.md
```

### Step 6-1. HTML 자동 변환 (사용자 인지용) ⭐ v2.3

스냅샷 저장 직후 **반드시** HTML 자동 변환 실행. MD는 source of truth, HTML은 사용자 읽기 전용.

```bash
PYTHON="/c/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe"
cd c:/Users/ibebu/bootcamp6_final/archive/teams-docs
"$PYTHON" .shared/html/md_to_html.py <X>team/snapshots/<YYMMDD>_pm.md
# PM은 코호트 대시보드도 같이 갱신 (1주 회고 + 팀 비교용)
"$PYTHON" .shared/html/generate_dashboard.py <YYMMDD> pm
```

→ 생성: `<X>team/snapshots/<YYMMDD>_pm.html` (MD 옆) + `.shared/html/dashboard.html` (가장 최근) + `.shared/html/dashboard_<YYMMDD>_pm.html` (일자 고정).
→ 사용자는 HTML로 인지: 최상단 "🎯 이번 회의에서 확인할 사항" 박스 + 정량 지표 카드 + ✅ 오전 액션 진척 + 펼침 섹션.
→ 변환 실패 시 사용자에 1회 알림 후 MD만으로 진행. 상세: [teams-docs/.shared/html/README.md](../../../teams-docs/.shared/html/README.md).

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
✅ HTML 변환: teams-docs/<X>team/snapshots/<YYMMDD>_pm.html (브라우저로 열기) + .shared/html/dashboard.html (코호트 비교)

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

## 캡스톤 6주 단계별 체크리스트 (PM 미팅 강조 항목)

> ⭐ v2.3 신규. 출처: [ta-guides/파이널프로젝트_캡스톤_심화_레퍼런스_2026-05-14.md](../../../ta-guides/파이널프로젝트_캡스톤_심화_레퍼런스_2026-05-14.md)
>
> 사용법: 보조강사가 PM 회의 직전 현재 주차에 해당하는 항목만 점검. 직전·다음 주차 항목도 함께 훑어 §🚨 강사 사전 통지 후보 식별.

### 매 PM 회의 공통 체크 (전 주차 적용)

- [ ] **PR 24h SLA 위반 PR 카운트** — 의뢰인 가이드의 24h 룰. 1건+ = R-pr-sla → 회의 외 escalate
- [ ] **AC fake done 위험** — 머지된 PR 중 보조강사 AC 미확인 건 (Rico & Sayani: AC 모호 시 fake done 빈발)
- [ ] **Walking Skeleton 무결성** — Week 1 이후 end-to-end 한 줄 동작이 깨지지 않았는지 (Cockburn 2004)
- [ ] **데모 시나리오 작성** — 현 주차 매주 데모 가능한 상태인지 (의뢰인 가이드 § "Week 1부터 데모 가능")
- [ ] **scope creep 신호** — 새 must 추가 시 기존 must의 won't 강등 거래 이력 (PMI: scope creep 92% 실패 요인)
- [ ] **신호등 회고 align** — 강사·보조강사·팀장 색이 다르면 내일 AM 1순위 자동 격상

### Week 1 Forming — 기획·기반 점검

- [ ] **Product Brief 1장** 합의 (Notion) — 의뢰인 가이드 TL;DR §1
- [ ] **User Flow** 작성 (Notion) — §2
- [ ] **Inception Deck NOT List** 작성 (Rasmusson 2010 10질문 중 핵심 5)
  - Why are we here / Elevator Pitch / **NOT List(Won't-Have)** / Trade-off slider / What Keeps Us Up at Night
  - GitHub Issue 라벨로 적재 (`wont-have`, `risk-up-at-night`)
- [ ] **Wireframes + Schema designs + JSON contracts** 3종 산출 (Turing Mod 4 Inception week 표준)
- [ ] **Friday까지 Walking Skeleton end-to-end 1줄 동작** — Android UI → ViewModel → Repository → Network → Fake JSON
  - 미달 팀: 보조강사 직접 page-through (정상 sprint 1–2 underrun, Mahnic 2012)
- [ ] **GitHub Projects 보드 + Issue/PR/커밋 템플릿** 셋업 (부스트캠프 7기 룰: 백로그명 = PR명, Squash merge)
- [ ] **그라운드룰 1차 합의** (브랜치 전략 / 컨벤션 / Ready 조건 / Done 정의 / PR SLA)

### Week 2 Storming — 갈등 정상화 + 추정 학습

- [ ] **conflict가 normal임을 명시 공지** (Tuckman) — burn 신호와 무관, 단계 통과 신호
- [ ] **역할 명확화 재확인** (PM/PO/디자이너/개발자 분담 흔들림 점검)
- [ ] **그라운드룰 마지막 조정** (Norming 직전)
- [ ] **Sprint 1–2 추정 underrun 40-60%는 정상**임을 학생에게 미리 고지 (Mahnic 2012)
- [ ] **AC 명세 품질 1차 검수** — Given-When-Then(Gherkin) 양식 도입 여부 (Ron Jeffries 3 Cs)
- [ ] **3팀 한정**: Phase-gate Must Meet 6항목 충족률 점검 (워터폴 톤 유지)

### Week 3 Norming — 페어 안정화 + 1차 동료평가

- [ ] **CATME 5축 1차 동료평가** (Google Form 5문항 간이판) — Hundhausen 2023
  - Contributing to Team's Work / Interacting / Keeping on Track / Expecting Quality / Having KSAs
- [ ] **RepoSense 기여 시각화** 1차 — `npx reposense` CLI. 기여 불균등 38/50% 사전 감지
- [ ] **페어 매칭 안정화** — 페어 회전·고정 결정
- [ ] **scope change request template** 만들기 (Week 3 시점, PMI)
- [ ] **데모 가능 상태 재확인** — 매주 데모 룰 위반 없는지

### Week 4 Performing 진입 — ★ R-burn 최빈 시점 ★

- [ ] **R-burn-1 모니터링 강화** (Tuckman Storming→Performing 전이 압력 = burn 최빈)
  - 의뢰인 메모리: `team1_burn_attend_260514_am.md` — Week 4 부근 R-burn-1 + R-attend 발현 사례
  - MBI-SS Exhaustion / Cynicism / reduced Efficacy 행동 프록시 확인
- [ ] **보조강사 stance 전환: Coach → Impediment Remover** (Scrum.org 6 stances)
  - 24h PR SLA 위반 시 impediment escalate가 우선
  - task focus 줄이고 process facilitation 늘림
- [ ] **추정 정확도 수렴 확인** (Mahnic 2012 sprint 4 시점 = Week 4)
- [ ] **AC fake done 검증 빈도 증가** — 기여 격차가 본격 드러나는 주차
- [ ] **번아웃 조기 신호** — R-attend (결석/지각), 코드 정체 36h+, 회의 침묵 등

### Week 5 Performing — 리허설·Won't-Have 거래

- [ ] **데모 리허설 T-3 그룹 피드백** (YC Demo Day 가이드: T-14 script → T-3 group feedback)
- [ ] **2분30초 / 15 슬라이드 narrative 1차 구조 확정**
  - Tagline → Problem → Solution → How it works → Traction → Tech → Ask
  - Rule of Three: 핵심 one-liner를 slide 1, 9, 14에서 반복
- [ ] **Won't-Have 강제 결정** — PO 대리(보조강사)가 매주 한 줄 새 won't-have 결정 요구
- [ ] **신규 must 거래 강제** — 새 must 추가 시 기존 must의 won't 강등 (Issue 라벨 변경)
- [ ] **scrcpy USB tether 미러링 setup 점검** (안드로이드 라이브 실패율 3× 대응, ASEE)
- [ ] **데모 백업 mp4 30초 녹화** 준비 시작
- [ ] **Trade-off slider 재확인** (시간/품질/범위/예산 — Rasmusson) — freeze 결정 근거

### Week 6 Adjourning/Freeze — 기능 동결 + 데모·QA

- [ ] **기능 추가 완전 중단** (의뢰인 가이드 § 0-1 "Freeze(Week 6)")
- [ ] **README 작성·갱신** (실행 방법 / 데모 시나리오 / 스크린샷)
- [ ] **QA 시나리오 작성** — 2~5분 내 실패 없이 실행 가능 검증
- [ ] **데모 리허설 2회 이상** — T-1 polish + T-0 30분 전 대기 (YC)
- [ ] **bad-path 리허설** — slow network / power 차단 / device 호환성
- [ ] **Named-owner contingency** — presenter failure / platform outage / lead failure 책임자 명시
- [ ] **scrcpy 미러링 최종 검증** + **30초 mp4 백업 확보** (라이브 실패 시 30초 내 전환)
- [ ] **CATME 2차 동료평가** + **RepoSense 최종 기여 시각화** — Hundhausen 2023 fairness 검증
- [ ] **sandbox 멱등성 검증** — 우테캠 7기 1위 데모 직전 실패 사례 (반복 호출 시 상태 깨짐)
- [ ] **방문자 투표 / 외부 심사 / 네트워킹 데이** 형식 사전 안내 (우테코 5기, 부스트캠프 네트워킹 데이)

### 캡스톤 단계 학술·실무 근거 1차 진입점 (PM 회의 중 참조)

- **Mahnic (2012)** — IEEE TE 캡스톤 Scrum: https://peerassessment.com/wp-content/uploads/2021/08/A-Capstone-Course-on-Agile-Software-DevelopmentUsing-Scrum-Mahnic-2012.pdf
- **Hundhausen et al. (2023)** — ACM TOCE GitHub + CATME 결합: https://pconrad.github.io/files/paper034.pdf
- **Turing Mod 4 운영 룰북**: https://mod4.turing.edu/projects/capstone/
- **YC Demo Day Guide**: https://www.ycombinator.com/library/4b-how-to-pitch-your-company
- **Rasmusson Inception Deck PDF**: https://rasmusson.files.wordpress.com/2008/01/rasmusson-agileinceptiondeckbootcamp.pdf

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
- [ta-guides/교육모델_국내외_레퍼런스_2026-05-14.md](../../../ta-guides/교육모델_국내외_레퍼런스_2026-05-14.md) — 국내외 부트캠프 운영 모델 비교
- [ta-guides/파이널프로젝트_캡스톤_심화_레퍼런스_2026-05-14.md](../../../ta-guides/파이널프로젝트_캡스톤_심화_레퍼런스_2026-05-14.md) ⭐ — **PM v2.3 §(f) 캡스톤 단계별 체크의 근거 자료**

### v2 설계 근거
- [operation/docs/research/external_practices.md](../../../operation/docs/research/external_practices.md)
- [operation/docs/research/current_pattern_gap.md](../../../operation/docs/research/current_pattern_gap.md)
- [operation/docs/research/v2_simulation_results.md](../../../operation/docs/research/v2_simulation_results.md)
- [operation/docs/research/template_v1_v2_diff.md](../../../operation/docs/research/template_v1_v2_diff.md)
- [operation/docs/meeting_template_v2_quick_apply.md](../../../operation/docs/meeting_template_v2_quick_apply.md) — 즉시 적용 가이드
