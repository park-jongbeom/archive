---
name: team-check-am
description: "1/2/3팀 오전 10시 미팅 사전 준비. 어제 16:00 ~ 오늘 09:30 변화량 점검 + AM 양식 작성 + 스냅샷 저장. 사용법: /team-check-am <팀번호> (1/2/3). 보조강사 관점."
effort: medium
---

# team-check-am — 오전 10시 팀 미팅 사전 준비

## 사용법

```
/team-check-am 1     ← 1팀 오전 점검
/team-check-am 2     ← 2팀
/team-check-am 3     ← 3팀
```

인자가 없으면 사용자에게 어느 팀인지 묻고 응답을 받은 후 진행.

---

## 절차 (순서 엄수)

### Step 1. 인자 파싱

사용자 입력에서 팀 번호(1/2/3) 추출. 못 찾으면 사용자에게 질의 후 진행.

팀별 경로 매핑:
- 팀 1: `teams-docs/1team/`, repo `Snoffee` (local: `/tmp/snoffee-analysis/Snoffee`), Figma `NKBVG1F8BcFXzjpnJxsC4u`
- 팀 2: `teams-docs/2team/`, repo `Umma` (local: `/tmp/umma-analysis/Umma`), Figma `BTzbV8SDChx6ywQwycniQi`
- 팀 3: `teams-docs/3team/`, repo `FinalProject-BBipit-BBip` (local: `/tmp/bbip-analysis/BBip`), Figma `X1hPf6U7hRuUGadqBoAr77` (FigJam) + `gjhxpOboz1famP1hlLVbjq` (Design)

### Step 2. 컨텍스트 로드 (순서 엄수)

다음 5개 파일을 **이 순서대로** 읽는다:

1. `teams-docs/.shared/daily_check_method.md` — 점검 방법론 환기
2. `teams-docs/<X>team/review/context.md` — 팀 컨텍스트
3. `teams-docs/<X>team/review/team_specific_checks.md` — 팀 고유 점검 항목
4. `teams-docs/.shared/risk_taxonomy.md` — 위험 신호 정량 기준
5. `teams-docs/<X>team/snapshots/` 최신 파일 (어제 PM 또는 최근) — 변화 감지용 baseline

3팀의 경우: 워터폴 톤 유지 (Sprint, mock-first, MVP 다이어트 권고 ❌).

### Step 3. 데이터 수집

다음 명령들을 **병렬로** 실행:

```bash
# 3-1. Git 활동 (해당 팀 로컬 클론)
cd <팀 로컬 경로>
git fetch --all --prune 2>&1 | tail -3

# 18시간 윈도우 (어제 16:00 ~ 오늘 09:30 근사)
git log --all --since="18 hours ago" --pretty=format:"%ai | %an | %h | %s"

# 인물별 활동 (24h)
git shortlog -sne --since="24 hours ago"

# 신규 TODO 추출
git log --all --since="18 hours ago" -p | grep -E "^\+.*TODO" | head -20

# 머지 충돌 흔적 (R6 점검)
git log --all --since="24 hours ago" --grep="Merge branch 'develop' into"

# 보안 점검 (R13/R14)
git ls-files | grep -iE "google-services|\\.env$|\\.keystore$|\\.jks$"
git ls-files | grep -E "^\\.idea/|^\\.vscode/" | wc -l
```

```
# 3-2. Figma (변화 감지용)
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
curl -s -H "X-Figma-Token: $TOKEN" "https://api.figma.com/v1/files/<fileKey>?depth=1"
```

```
# 3-3. GitHub Issues/PR (WebFetch)
# Open issues: https://github.com/LIKELION-Android-BOOTCAMP-6th/<repo>/issues
# Recent PRs: https://github.com/LIKELION-Android-BOOTCAMP-6th/<repo>/pulls?q=is%3Apr
```

### Step 4. 진단

점검 항목 우선순위:

**(a) Top 5 (반드시)** — `team_specific_checks.md § 1`의 Top 5
**(b) 공통 위험 신호** — `risk_taxonomy.md`의 R1~R14 (+3팀의 경우 R-W1~R-W4) 정량 기준 적용
**(c) 인물별 활동표** — `context.md` 팀 구성표 기반, 24h 누적 (commit/Issue/PR/댓글)
**(d) PRD Must 매트릭스 갱신**
**(e) Figma 변화** — lastModified 갱신 여부, 신규 섹션

### Step 5. AM 양식 작성

`teams-docs/.shared/meeting_prep_template.md § 1 AM 양식`을 그대로 사용하여 작성. 채워야 할 섹션:

1. 🟢 한 줄 요약
2. 📊 18시간 변화 (어제 16:00 ~ 오늘 09:30)
3. 🎯 오늘의 키 포인트
4. 🟡 주의 항목 (강사가 놓칠 가능성 — 보조강사 시각)
5. 🚨 즉시 조치 필요 (R1/R13/R-out 등 발견 시)
6. 👤 인물별 활동표
7. 📋 Issue↔PR 정합성
8. 🎨 Figma 보드 변화
9. 💡 보조강사 권장 액션 (회의에서 짚을 항목)
10. 🔁 어제 PM carry-over 진척

### Step 6. 스냅샷 저장

작성한 양식을 다음 경로로 저장:
```
teams-docs/<X>team/snapshots/<YYMMDD>_am.md
```

파일명 예: `260514_am.md` (2026-05-14 오전).

### Step 7. 사용자에 보고

저장한 스냅샷 내용을 그대로 출력하되:
- 🚨 즉시 조치 필요 항목이 있으면 **상단 강조**
- 결과 끝에 회의 시작 5분 전 알림 + 회의 후 갱신 절차 1줄 안내

---

## 출력 끝 표준 멘트

```
---
✅ 스냅샷 저장: teams-docs/<X>team/snapshots/<YYMMDD>_am.md
회의 종료 후 결정사항을 issue_pr_matrix.md / team_specific_checks.md에 반영하세요.
다음: 오후 4시 미팅 30분 전 `/team-check-pm <팀번호>` 호출.
```

---

## 주의 사항

- **3팀은 워터폴** — Sprint/mock-first/MVP 다이어트 권고 금지. 워터폴 R-W 항목 우선 적용.
- 토큰은 절대 출력 본문에 노출하지 말 것 (token-leak-guard hook이 차단하지만 사전 주의).
- 양식이 길어지면 1팀 종합 진단 보고처럼 섹션을 압축하되, 인물별 활동표와 위험 신호는 절대 생략 금지.
- 직전 스냅샷이 없으면 (첫 호출 등) "베이스라인 비교 없음" 명시.

## 참조

- [.shared/daily_check_method.md](../../../teams-docs/.shared/daily_check_method.md)
- [.shared/meeting_prep_template.md](../../../teams-docs/.shared/meeting_prep_template.md)
- [.shared/risk_taxonomy.md](../../../teams-docs/.shared/risk_taxonomy.md)
