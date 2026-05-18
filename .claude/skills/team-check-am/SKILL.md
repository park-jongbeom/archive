---
name: team-check-am
description: "1/2/3팀 오전 10시 미팅 사전 준비. 어제 16:00 ~ 오늘 09:30 변화량 점검 + AM 양식 v2 작성 + 스냅샷 저장 + 강사 사전 통지 의제 3건 압축. 사용법: /team-check-am <팀번호> (1/2/3). 보조강사 관점."
effort: medium
---

# team-check-am — 오전 10시 팀 미팅 사전 준비 (v2 양식)

> **양식**: [meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) (2026-05-14부 정식 적용)
> **v1 양식**: [meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md) (참조용만 유지)

## 사용법

```
/team-check-am 1     ← 1팀 오전 점검
/team-check-am 2     ← 2팀
/team-check-am 3     ← 3팀
```

인자가 없으면 사용자에게 어느 팀인지 묻고 응답을 받은 후 진행.

---

## 절차 (순서 엄수, v2 적용)

### Step 1. 인자 파싱

사용자 입력에서 팀 번호(1/2/3) 추출. 못 찾으면 사용자에게 질의 후 진행.

팀별 경로 매핑:
- 팀 1: `teams-docs/1team/`, repo `Snoffee` (local: `/tmp/snoffee-analysis/Snoffee`), Figma `NKBVG1F8BcFXzjpnJxsC4u`
- 팀 2: `teams-docs/2team/`, repo `Umma` (local: `/tmp/umma-analysis/Umma`), Figma `BTzbV8SDChx6ywQwycniQi`
- 팀 3: `teams-docs/3team/`, repo `FinalProject-BBipit-BBip` (local: `/tmp/bbip-analysis/BBip`), Figma `X1hPf6U7hRuUGadqBoAr77` (FigJam) + `gjhxpOboz1famP1hlLVbjq` (Design)

### Step 2. 컨텍스트 로드 (순서 엄수)

다음 7개 파일을 **이 순서대로** 읽는다:

1. `teams-docs/.shared/daily_check_method.md` — 점검 방법론 환기
2. `teams-docs/.shared/meeting_prep_template_v2.md` — **현재 적용 양식** (v2.1)
3. `teams-docs/<X>team/review/context.md` — 팀 컨텍스트
4. `teams-docs/<X>team/review/team_specific_checks.md` — 팀 고유 점검 항목 + Top 5
5. `teams-docs/.shared/risk_taxonomy.md` — 공통 R-항목 정량 기준 (R1~R14 + R-attend·R-burn 신규 + 3팀 R-W1~W6)
6. `teams-docs/<X>team/snapshots/<직전회차>.md` — 어제 PM 또는 직전 스냅샷 (carry-over 자동 import 기준)
7. **`teams-docs/<X>team/mom/<오늘날짜>*`** ⭐ v2.1 신규 — **사용자가 수기로 작성한 팀 회의록** (오늘 분담 cross-check 기준)
   - 1팀 예: `260514 - 진행중` 같은 파일명. Glob으로 찾기: `ls teams-docs/<X>team/mom/ | grep <YYMMDD>`
   - 파일 없으면 "오늘 mom 미작성" 명시 + §🗂️ 섹션에서 mom 컬럼 비워둠
8. **`teams-docs/<X>team/backlog/backlog_<YYMMDD>_am.pdf`** ⭐ v2.2 신규 — **GitHub Project Kanban 보드 PDF** (사용자가 회의 40분 전 브라우저 인쇄로 추출)
   - 폴더 README: [teams-docs/<X>team/backlog/README.md](../../../teams-docs/1team/backlog/README.md)
   - **Read 도구**로 PDF 자동 파싱 (10페이지 이하는 `pages` 생략, 큰 경우 `pages: "1-5"`)
   - PDF 없으면 사용자에 1회 알림: "회의 40분 전 Project 보드 PDF 인쇄 → backlog/ 폴더에 저장 권장. 없이 진행할까요?"
   - 파싱 결과: 컬럼별 카드 수 (Backlog/Ready/In Progress/In Review/Done) + 각 카드 (#번호, 제목, assignee) + WIP 한도 위반 (한도 vs 현재)

추가로 팀별 메모리 참조:
- 1팀: [team1_personnel_change_260512.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_personnel_change_260512.md), [team1_burn_attend_260514_am.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team1_burn_attend_260514_am.md)
- 3팀: [team3_methodology.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md) — **워터폴 톤 절대 유지 (Sprint/mock-first/MVP 다이어트 권고 ❌)**

### Step 3. 사전 점검 자동화 명령 (B1 — v2 §🔍)

다음 명령들을 **병렬로** 실행. 결과는 §🚨 즉시 조치 + §🚦 R-항목 점검 칸에 직접 반영:

```bash
cd <팀 로컬 경로>
git fetch --all --prune 2>&1 | tail -3

# 3-1. 18시간 윈도우 변화량
git log --all --since="18 hours ago" --pretty=format:"%ai | %an | %h | %s"

# 3-2. 인물별 활동 (24h + 7일)
git shortlog -sne --since="24 hours ago" origin/develop
git shortlog -sne --since="7 days ago" origin/develop   # R-out / R-quiet 후보

# 3-3. 신규 TODO 추출
git log --all --since="18 hours ago" -p | grep -E "^\+.*TODO" | head -20

# 3-4. 머지 충돌 흔적 (R6)
git log --all --since="24 hours ago" --grep="Merge branch 'develop' into"

# 3-5. 보안 점검 (R13/R14) — v2 §🔍에 결과 직접 명시
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$|\\.jks$|\\.p12$|\\.pem$|-key\\.json$"
git ls-files | xargs grep -lE "AIza[0-9A-Za-z_-]{30,}|sk_live_|figd_" 2>/dev/null
git ls-files | grep -E "^\\.idea/|^\\.vscode/|\\.DS_Store|Thumbs\\.db" | wc -l

# 3-6. Figma (변화 감지용)
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
curl -s -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/<fileKey>?depth=1"

# 3-7. GitHub Issues/PR (WebFetch)
# Open issues: https://github.com/LIKELION-Android-BOOTCAMP-6th/<repo>/issues
# Recent PRs: https://github.com/LIKELION-Android-BOOTCAMP-6th/<repo>/pulls?q=is%3Apr

# 3-8. GitHub Project 보드 — PDF 추출 방식 (v2.2 확정)
# 사용자가 회의 40분 전(09:20 / 15:20) 브라우저 인쇄로 PDF 저장:
#   teams-docs/<X>team/backlog/backlog_<YYMMDD>_<am|pm>.pdf
# Project URL (참고용):
#   1팀: https://github.com/orgs/LIKELION-Android-BOOTCAMP-6th/projects/11/views/1
#   2팀: https://github.com/orgs/LIKELION-Android-BOOTCAMP-6th/projects/12/views/1
#   3팀: https://github.com/orgs/LIKELION-Android-BOOTCAMP-6th/projects/13/views/1
# SKILL이 Read 도구로 PDF 직접 파싱 — token 불필요
```

### Step 4. 진단 (v2 우선순위)

점검 항목 우선순위:

**(a) Top 5 (반드시)** — `team_specific_checks.md §1`의 팀별 Top 5

**(b) 공통 R-항목** — `risk_taxonomy.md` 정량 기준:
- People (6건): R1, R8, R-out, R-quiet, **R-attend** (신규 표준), **R-burn** (신규 표준)
- Process (4건): R2, R3, R6, R-cycle
- Alignment (3건): R4, R9, R12
- Quality (2건): R5, R7
- Hygiene (2건): R13, R14
- Operations (3건): R10, R11, R-demo
- 3팀 워터폴 추가 (4건): R-W1~R-W4

**(c) 팀 특화 R-항목** — `team_specific_checks.md §6`:
- 1팀: R-PO, R-burn-1 (= R-burn 1팀 임계값), R-MN, R-DL, R-Must7
- 2팀: R-out-2, R-WIP, R-burn (2팀), R13-2
- 3팀: R-W5, R-W6 추가

**(d) Carry-over 자동 import + 이행률**:
- 직전 PM 스냅샷의 §🌙 내일 AM carry-over 표를 그대로 import
- 회의-내 이행률 = 회의에서 다뤄진 N / 전체 M
- **이월 2회+ 항목은 자동 🚨 격상** (§🚨 강사 사전 통지 1순위 후보)

**(e) 인물별 24h 활동표** — `context.md` 팀 구성표 기반, commit/Issue/PR/머지/댓글 + **이전 회의 발화 ✅/❌**
- **commit ≥ 1 + 발화 0** → R-quiet 자동 flag
- **commit 0 + 발화 0 + 결석 사유 없음** → R-out 후보

**(f) PRD Must 매트릭스 갱신** + **(g) Figma 변화** — `team_specific_checks.md §2`/`§4` 갱신

**(h) 🗂️ 보드 PDF + mom + commit 3중 cross-check** ⭐ v2.2:
- **GitHub Project Kanban PDF 파싱** (`teams-docs/<X>team/backlog/backlog_<YYMMDD>_<am|pm>.pdf`):
  - Read 도구로 PDF 텍스트 추출
  - 컬럼별 카드 수 + WIP 한도 비교 (예: `In progress 7/3` = 한도 3 / 현재 7 = 4건 초과 → R-WIP 즉시 발현)
  - 각 카드: `#번호`, `[Issue X-N]` 라벨, 제목, assignee 텍스트
- 오늘 팀 수기 mom (`teams-docs/<X>team/mom/<YYMMDD>*`)에서 멤버별 분담 추출
- 24h commit 영역(`git log --since="24 hours ago"` + 변경 파일 경로)과 정합
- 정합성 신호 발현 시 §🚨 강사 사전 통지 후보 추가:
  - 🚨🚨 **WIP 한도 초과** (보드 PDF의 컬럼 헤더 "N/M" 패턴에서 N > M) → **R-WIP 1순위 자동 격상**
  - ⚠️ mom 있는데 보드 카드 X → 사용자에게 Issue 등록 권유 (R2)
  - ⚠️ 보드 In Progress인데 commit 0 → R-stall 후보
  - ⚠️ commit 있는데 mom 미매핑 → R9 Scope drift 후보
  - ⚠️ mom 분담 미배정 멤버 → R-quiet 후보
  - 🚨 단일 멤버 assign ≥ 4 → R8

### Step 5. AM 양식 v2 작성 (14개 섹션)

`teams-docs/.shared/meeting_prep_template_v2.md §1 AM 양식` 사용. **순서 엄수**:

1. **🔍 사전 점검 자동화 명령 결과** (B1 — Step 3 grep 결과 직접 명시)
2. **🚨 즉시 조치 필요** (최상단 — 24h+ 정체 또는 보안/위생 critical. 없으면 "없음" 명시)
3. **🚨 강사 사전 통지 의제 3건** (회의 5분 전 강사 채팅/1:1 전달 — 보조강사 마이크 SPOF 백업):
   - **1번 의제는 "(회의 첫 안건 권유)" 명시** — B3
   - **carry-over 2회+ 이월 항목은 자동 1순위 격상** — B2
   - **회의 외 강사 1:1 별도 통지 항목** 별도 표기 (carry-over 2회+ 시) — B2
4. 🟢 한 줄 요약 — 전체 신호등 + 어제 종료 후 핵심 변화 1문장
5. 📊 18시간 변화 — **Finished (어제 PM 이후) / Will finish by when** (Patton/Gothelf 프레임)
6. **🎯 오늘 끝낼 수 있는 작업 3개** (Walking the Board — 보드 오른쪽 → 왼쪽 순회)
7. **🗂️ 보드 In Progress + 분담 cross-check** ⭐ v2.1 — 3중 매칭표 (mom 분담 / 보드 In Progress / 24h commit) + 정합성 신호 (R-stall / R-quiet / R-WIP / R8)
8. **🔁 Carry-over 자동 재출현 + 이행률 N/M%** — owner / 마감 / 회의에서 다뤘나 / 데이터 상태 / 이월 횟수 표
9. 🚦 R-항목 점검 — Step 4 (b)(c) 정량 결과 (오늘 AM 상태 + 변화)
10. 👤 인물별 24h 활동 + 회의 발화 정합성 — R-quiet / R-out / R-attend 자동 flag
11. 📋 Issue↔PR 정합성 (1·2팀 애자일 / 3팀은 §14 Phase-gate)
12. 🎨 FigJam/Design 보드 변화 — lastModified + 신규 페이지·섹션
13. 💡 보조강사 권장 액션 (3건 압축, 좋은 점 1건 포함 원칙: 🟢 격려 → 🚨 critical → 🟡 환기)
14. (3팀 한정) **🚧 Phase-gate Must Meet 6항목 헤더** — `3team/review/team_specific_checks.md §2-2` 자동 import, 충족률 N/6 명시
15. (선택) 📚 근거 인용 — 신규 R-항목 도입시만 ([가이드 §N](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) 또는 [FAQ §N](../../../ta-guides/애자일_예제기반_FAQ.md))

### Step 6. 스냅샷 저장

```
teams-docs/<X>team/snapshots/<YYMMDD>_am.md
```

파일명 예: `260514_am.md` (2026-05-14 오전).

### Step 6-1. HTML 자동 변환 (사용자 인지용) ⭐ v2.3

스냅샷 저장 직후 **반드시** HTML 자동 변환 실행. MD는 source of truth, HTML은 사용자 읽기 전용 (Claude는 계속 MD로 작업).

```bash
PYTHON="/c/Users/ibebu/AppData/Roaming/uv/python/cpython-3.14-windows-x86_64-none/python.exe"
cd c:/Users/ibebu/bootcamp6_final/archive/teams-docs
"$PYTHON" .shared/html/md_to_html.py <X>team/snapshots/<YYMMDD>_am.md
# all 모드 또는 3팀 모두 처리 시:
"$PYTHON" .shared/html/generate_dashboard.py <YYMMDD> am
```

→ 생성: `<X>team/snapshots/<YYMMDD>_am.html` (MD 옆) + `.shared/html/dashboard.html` (가장 최근).
→ 사용자는 HTML로 인지: 최상단 "🎯 이번 회의에서 확인할 사항" 박스 + 정량 지표 카드 + 펼침 섹션.
→ 변환 실패 시 사용자에 1회 알림 후 MD만으로 진행. 상세: [teams-docs/.shared/html/README.md](../../../teams-docs/.shared/html/README.md).

### Step 7. 사용자에 보고 (강사 사전 통지 우선 노출)

사용자 출력 순서:

1. **§🚨 강사 사전 통지 의제 3건** — **본문 최상단에 별도 박스로 강조** (사용자가 채팅 복사 후 회의 5분 전 강사에게 전달용)
2. 🚨 즉시 조치 항목 — 별도 박스 강조
3. 한 줄 요약 + 핵심 변화
4. carry-over 이행률 + 2회+ 이월 자동 격상 항목
5. 인물별 활동 요약
6. 보조강사 권장 액션 3건
7. 출력 끝 표준 멘트

### Step 8. 회의 후 갱신 (회의 종료 후 5분, 사용자가 별도 호출)

- §🚨 강사 사전 통지 3건 중 강사 발화한 항목 ✅ 표시
- 미언급은 §🌙 내일 AM carry-over에 자동 이월 + 이월 횟수 +1
- 회의 실제 시간 기록 (분/초)
- 회의록(mom) 작성 트리거 조건 ([team-meeting-transcribe SKILL Step 6](../team-meeting-transcribe/SKILL.md) 참조)

---

## 출력 끝 표준 멘트

```
---
✅ 스냅샷 저장: teams-docs/<X>team/snapshots/<YYMMDD>_am.md
✅ HTML 변환: teams-docs/<X>team/snapshots/<YYMMDD>_am.html (브라우저로 열기) + .shared/html/dashboard.html

📋 회의 5분 전 (09:55) — §🚨 강사 사전 통지 의제 3건을 강사 채팅/1:1로 전달.
   (보조강사 마이크 이슈 대비 백업 채널 — 마이크 가능시 회의 중 보완용으로만)

⚠️ Carry-over 2회+ 이월 항목 N건 — 회의와 별개로 강사 1:1 별도 통지 권장 (B2).

회의 후: 강사 발화 항목에 ✅ 표시 → §🌙 carry-over 이월 + /team-check-pm <팀번호> 호출.
```

---

## 주의 사항

### 톤 / 운영 룰
- **3팀은 워터폴** — Sprint/mock-first/MVP 다이어트 권고 ❌. 워터폴 R-W1~W6 우선 적용. [team3_methodology.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md) 절대 준수.
- **좋은 점부터 1건 이상** ([FAQ §10](../../../ta-guides/애자일_예제기반_FAQ.md) "❌ 칭찬만" 회피 + "좋은 점 먼저" 원칙).
- **인물 비판 X / 코드·프로세스 비판 ✅** — "OO팀원 게으르다" ❌ / "OO팀원 commit 0 + Issue 댓글 0 = R-quiet" ✅.

### v2 핵심 룰
- **B1 사전 점검 명령**: Step 3 grep 결과를 §🔍에 직접 명시. 보조강사가 매번 명령 찾는 시간 절약.
- **B2 carry-over 2회+ 격상**: 자동으로 §🚨 강사 사전 통지 1순위 + 회의 외 강사 1:1 별도 통지.
- **B3 회의 첫 안건 권유**: §🚨 강사 사전 통지 1번 의제에 명시. 시뮬레이션 결과 결정적 효과.

### 보안 / 위생
- 토큰은 절대 출력 본문에 노출 X (token-leak-guard hook이 차단하지만 사전 주의).
- 양식이 길어지면 압축하되, **인물별 활동표 + R-항목 점검 + carry-over 이행률**은 절대 생략 X.
- 직전 스냅샷이 없으면 "베이스라인 비교 없음" 명시 + Week N 최소 Done만 헤더에 자동 import.

---

## 참조

### 양식 / 점검 항목
- [.shared/meeting_prep_template_v2.md](../../../teams-docs/.shared/meeting_prep_template_v2.md) — **현재 적용 양식** (2026-05-14부)
- [.shared/meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md) — v1 참조용
- [.shared/daily_check_method.md](../../../teams-docs/.shared/daily_check_method.md) — 점검 방법론
- [.shared/risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md) — R-항목 정량 기준 (R-attend·R-burn 표준 포함)

### 사내 가이드 (v2 §📚 근거 인용 기준)
- [final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md](../../../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) — 애자일 본 가이드
- [ta-guides/애자일_예제기반_FAQ.md](../../../ta-guides/애자일_예제기반_FAQ.md) — 보조강사 FAQ (§9 안티패턴 식별표)

### v2 설계 근거 (조사·분석·시뮬레이션)
- [operation/docs/research/external_practices.md](../../../operation/docs/research/external_practices.md) — 외부 베스트 프랙티스
- [operation/docs/research/current_pattern_gap.md](../../../operation/docs/research/current_pattern_gap.md) — gap 분석
- [operation/docs/research/v2_simulation_results.md](../../../operation/docs/research/v2_simulation_results.md) — v2 효과 시뮬레이션
- [operation/docs/research/template_v1_v2_diff.md](../../../operation/docs/research/template_v1_v2_diff.md) — v1↔v2 변경 요약
- [operation/docs/meeting_template_v2_quick_apply.md](../../../operation/docs/meeting_template_v2_quick_apply.md) — 오늘 즉시 적용 가이드
