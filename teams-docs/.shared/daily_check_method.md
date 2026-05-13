# 일일 점검 방법론 — 보조강사 관점

> **사용자**: 안드로이드 부트캠프 6기 보조강사
> **목적**: 강사님과 진행하는 **오전 10시 / 오후 4시** 팀 미팅에서 강사가 놓치기 쉬운 부분을 보완하고, 작업 방향성을 검증
> **적용 범위**: 1팀(Scoffee, 애자일), 2팀(Umma, 애자일), 3팀(BBip, **워터폴**)
> **갱신**: 매주 일요일 또는 단계 전환 시

---

## 1. 보조강사 역할 정의

강사는 **방향·평가**를 본다. 보조강사는 **미시·과정·인물**을 본다. 두 시선이 합쳐져야 팀이 정확히 보입니다.

| 강사가 보통 보는 것 | 보조강사가 보완해야 하는 것 |
|---|---|
| 전체 진척 / 데모 가능성 | 인물별 미세 활동 (조용한 멤버) |
| 아키텍처 큰 그림 | 단위 테스트·아키텍처 위반·매직 넘버 |
| Brief/PRD 정합성 | Issue↔PR↔디자인 추적 단절 |
| 발표 시연 준비 | 보안·위생 (secrets, IDE 아티팩트) |
| 결정 사항 | 결정에 합의되지 않은 잠재 의견 |
| Must 기능 완성 | mock-first / phase-gate 규약 위반 |

→ 본 가이드의 모든 체크리스트는 **위 우측 열 관점**을 우선으로 설계.

---

## 2. 하루 2회 리듬 (10AM / 4PM)

### 2-1. 데이터 윈도우

| 미팅 | 분석 윈도우 | 강조점 |
|---|---|---|
| **10AM** | **전날 4PM ~ 오늘 9:30AM** (≈18시간, 야간 작업 포함) | "어제 종료 후 무엇이 바뀌었나" + "오늘 무엇을 봐야 하나" |
| **4PM** | **오늘 10AM ~ 3:30PM** (≈6시간) | "오전 결정사항이 실제로 진행됐나" + "오후 마무리 점검" |

### 2-2. 사전 준비 (30분 전)

각 미팅 30분 전에 다음 흐름으로 데이터 수집·정리:

1. **공통 데이터 수집** (5분, 자동 명령)
2. **팀별 컨텍스트 적용** (5분, [`Xteam/review/context.md`](../1team/review/context.md))
3. **팀별 고유 점검 항목 진단** (10분, [`Xteam/review/team_specific_checks.md`](../1team/review/team_specific_checks.md))
4. **위험 신호 점검** (5분, [`./risk_taxonomy.md`](./risk_taxonomy.md))
5. **출력 양식 작성** (5분, [`./meeting_prep_template.md`](./meeting_prep_template.md))

### 2-3. 회의 중 행동 원칙

- **강사 발언을 우선** — 보조강사는 강사가 빠뜨린 곳만 보완
- **체크리스트로 무장하되 낭독하지 말기** — 머릿속에 두고 자연스럽게 질문
- **인물 비판 ❌ 코드/프로세스 비판 ✅** ([Built In 멘토링 가이드](https://builtin.com/software-engineering-perspectives/secrets-leveling-junior-engineers))
- **좋은 점부터** — 부정 피드백 전에 모범 사례 1개 이상 발견·언급
- **회의 메모는 보조강사가** — 결정사항·액션아이템 기록

### 2-4. 회의 후 갱신 (5분)

- snapshots/YYMMDD_(am|pm).md 저장
- 결정사항 발견되면 issue_pr_matrix.md / 관련 가이드 즉시 반영
- 다음 회의로 carry-over 할 항목 명시

---

## 3. 보조강사가 강사보다 잘 잡는 5가지 (의식적으로 점검)

### 3-1. 인물별 미세 활동 차이

- "팀 전체 OK"라는 강사 인식 vs **개별 멤버 N일 무활동**
- 점검: `git shortlog -sne --since="7 days ago"` + Issue/PR 댓글 활동
- 특히 강사가 잘 모르는 **PO/팀장의 커밋 외 활동** (Issue 작성·리뷰·문서)

### 3-2. 추적 단절 (Issue ↔ PR ↔ 디자인 ↔ 코드)

- 강사는 결과만 본다 → 보조강사는 4-way 정합성 점검
- PR description에 `Closes #N` 없음 → 추적 단절 신호
- Figma엔 있는데 코드엔 없는 화면 / 그 반대

### 3-3. 코드 미시 품질

- 아키텍처 위반 (domain → data import)
- 도메인 enum 무력화 (Int 분기로 enum 우회)
- 매직 넘버 / 매직 스트링
- 신규 TODO 누적 (해소 안 됨)
- 테스트 정체

### 3-4. 보안 / 위생

- 시크릿 파일 커밋 (`google-services.json`, `.env`, keystore)
- IDE 아티팩트 (`.idea/`, `.vscode/`)
- API 키 평문
- [`./risk_taxonomy.md` R13/R14](./risk_taxonomy.md)

### 3-5. 사전 부검 (Pre-mortem)

- 강사는 현재 상태를 본다 → 보조강사는 **"실패한다면 무엇이 원인일까"** 정기적으로 점검
- 주 1회 (금요일 4PM 후) [`./premortem_template.md`](./premortem_template.md)
- [McKinsey 연구](https://www.parabol.co/resources/pre-mortem-questions/): pre-mortem 사용 팀이 일반 risk analysis 대비 overconfidence 유의미하게 감소

---

## 4. 팀별 방법론 차이 적용

### 4-1. 1팀 / 2팀 — 애자일 (Scrum-lite)

- **스프린트 목표 정렬** 기준 점검 ([Scrum.org](https://www.scrum.org/resources/blog/going-beyond-three-questions-daily-scrum))
- 변화 빈도 높음 — 매일 점검
- Mock-first / Decision Log 준수
- Issue 닫힘률 / PR 사이클 타임 / 셀프 머지 비율

### 4-2. 3팀 — 워터폴 (Phase-gate)

- **단계 종료 기준 (Must Meet / Should Meet)** 정합 ([Smartsheet phase-gate](https://www.smartsheet.com/phase-gate-process), [Wikipedia](https://en.wikipedia.org/wiki/Phase-gate_process))
- 변화 빈도 낮음 — 일일 점검은 가볍게, **단계 전환 시 집중 점검**
- 명세 완성도 / 화면-DB 정합성 / 핸드오프 문서
- ❌ Mock-first / Sprint / MVP 다이어트 권고 금지 ([team3_methodology.md](../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md))

---

## 5. 자동 데이터 수집 명령 (회의 30분 전 1줄 실행)

```bash
cd /c/Users/ibebu/bootcamp6_final/archive
TOKEN=$(cat teams-docs/.shared/.figma_token | tr -d '\r\n ')
DATE=$(date +%y%m%d)

# 팀별로 반복 (1팀 예시)
TEAM_DIR=teams-docs/1team
FILE_KEY=NKBVG1F8BcFXzjpnJxsC4u

# 1. Figma 메타 (변화 감지용)
curl -s -H "X-Figma-Token: $TOKEN" \
  "https://api.figma.com/v1/files/$FILE_KEY?depth=1" \
  -o $TEAM_DIR/figma/raw/board_${DATE}.json

# 2. Git 활동 (해당 팀 로컬 클론 필요)
# 1팀: /tmp/snoffee-analysis/Snoffee
# 2팀: /tmp/umma-analysis/Umma
# 3팀: /tmp/bbip-analysis/BBip
```

상세 팀별 명령은 각 팀의 [`figma/README.md` § 4](../1team/figma/README.md#4-매일-자동-수집-절차-보조강사용)를 참조.

---

## 6. 자료 흐름도

```
회의 30분 전
    ↓
[자동 데이터 수집]
git fetch / Figma API / WebFetch GitHub Issues+PRs
    ↓
[팀별 진단]
context.md (팀 맥락 환기)
team_specific_checks.md (팀 고유 항목)
risk_taxonomy.md (R1~R14 정량 점검)
    ↓
[양식 작성]
meeting_prep_template.md 채우기
    ↓
[회의 진행]
강사 발언 우선, 빠진 부분만 보완
    ↓
[회의 후 5분]
snapshots/YYMMDD_(am|pm).md 저장
결정사항 → issue_pr_matrix.md 반영
```

---

## 7. 호출 명령 (사용자가 Claude에 발화)

| 사용자 발화 | Claude 동작 |
|---|---|
| "1팀 작업 현황 체크해줘" / "오전 미팅 준비" | 1팀 데이터 수집 + meeting_prep_template AM 변형 작성 |
| "1팀 오후 미팅 준비" | 1팀 데이터 수집 + PM 변형 작성 |
| "2팀/3팀 ..." | 동일하게 해당 팀 적용 |
| "오늘 전체 회의 준비" | 1·2·3팀 모두 일괄 처리 |
| "이번 주 pre-mortem" | premortem_template.md 양식으로 5개 시나리오 도출 |

---

## 8. 갱신 정책

- 매주 일요일: 각 팀 `team_specific_checks.md` 재검토
- 단계 전환 시 (예: 3팀 설계 → 구현): `context.md` 갱신
- 신규 위험 신호 발견: `risk_taxonomy.md` 에 R-항목 추가
- 회의 진행 방식 변경 시: 본 문서 갱신

---

## 9. 참조

- [.shared/README.md](./README.md) — 토큰·레지스트리
- [./risk_taxonomy.md](./risk_taxonomy.md) — R1~R14 정량 기준
- [./meeting_prep_template.md](./meeting_prep_template.md) — 회의 사전 준비 양식
- [./premortem_template.md](./premortem_template.md) — 주1회 사전 부검
- 외부 참고:
  - [Atlassian Standups for agile teams](https://www.atlassian.com/agile/scrum/standups)
  - [Scrum.org — Going Beyond Three Questions](https://www.scrum.org/resources/blog/going-beyond-three-questions-daily-scrum)
  - [Parabol — New Scrum Master Daily Checklist](https://www.parabol.co/blog/new-scrum-master-daily-checklist/)
  - [PMI — Early Warning Signs in Complex Projects](https://www.pmi.org/learning/library/identifying-warning-signs-complex-projects-6259)
  - [Built In — Secrets to Leveling Up Junior Engineers](https://builtin.com/software-engineering-perspectives/secrets-leveling-junior-engineers)
  - [Smartsheet — Phase-Gate Process](https://www.smartsheet.com/phase-gate-process)
