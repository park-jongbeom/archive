# operation/ — Claude 운영 지침

이 폴더에서 작업할 때 Claude는 아래 지침을 따른다. 사용자(보조강사)가 폴더 안에서 호출하면 자동 적용.

## 1. 이 폴더의 목적

부트캠프 6기 파이널 프로젝트 6주 동안 각 팀의 **GitHub 산출물 + 강사가 인쇄한 노션 PDF** 를 주 1회 수집/분석하여 마크다운 리포트 생성. cron 없음. 사용자 호출이 트리거.

## 2. 데이터 소스 — 자동/수동 분담

| 소스 | 자동? | 위치 |
|---|---|---|
| GitHub Issue/PR 메타데이터 | ✅ | `fetch_github.py` API 호출 |
| 학생 자기보고 (Weekly Status Issue) | ✅ | GitHub `weekly-status` 라벨 Issue 본문 파싱 |
| 노션 Brief / Flow / Decision Log / Retro | ❌ 강사 수동 | `snapshots/week-N/팀명_pdf/*.pdf` |

⚠️ **노션 API / Export / Share to web / Playwright 자동화 모두 사용 금지** — 회사 정책상 차단됨. 강사가 PDF로 인쇄한 자료만 사용.

## 3. 사용자 요청 → 실행 패턴

### 패턴 A: "Week N 스냅샷 떠줘"

순서대로 실행:

1. `python scripts/fetch_github.py --week N` 실행
2. `python scripts/analyze.py --week N` 실행
3. `snapshots/week-N/_summary.json` 읽기
4. 각 팀 `snapshots/week-N/팀명_pdf/` 폴더 존재 여부 확인 (있으면 신호등에 반영)
5. `reports/week-N.md` 작성 (§ 5 템플릿)
6. 사용자에게 🔴 항목만 5줄 이내로 요약 출력

### 패턴 B: "K팀 진단" 또는 "K팀 깊게 봐줘"

1. `snapshots/week-N/K팀_github.json` + `K팀_status.json` 읽기
2. (있으면) `snapshots/week-N/K팀_pdf/` 의 PDF를 Read 도구로 읽어 본문 분석
3. [`../ta-guides/애자일_예제기반_FAQ.md`](../ta-guides/애자일_예제기반_FAQ.md) § 9 안티패턴 표 기반으로 1페이지 진단
4. 가이드 § 번호 근거 명시

### 패턴 C: "리포트만 다시 써줘"

`snapshots/` 그대로 두고 `reports/week-N.md` 만 재생성.

### 패턴 D: "PDF 점검해줘 Week N"

`snapshots/week-N/*_pdf/` 폴더가 모든 팀에 존재하는지, 파일명이 표준(01_brief.pdf, 02_flow_*.pdf, 06_decision_log.pdf, 07_retro.pdf)과 일치하는지 점검.

## 4. 보안 규칙 (절대 준수)

- ❌ `.env` 내용을 사용자에게 출력 금지 (요약/일부 인용도 X)
- ❌ 토큰 코드에 하드코딩 금지
- ❌ **Notion API 호출 금지** (회사 정책 위반) — 차단된 Integration / Export / Share to web 우회 시도 절대 금지
- ⚠️ PDF 파일에 학생 이메일/전화 등 개인 식별 정보 발견 시 사용자에게 보고

## 5. 리포트 템플릿 (`reports/week-N.md`)

```markdown
# Week N 진행 스냅샷

> 수집 시점: YYYY-MM-DD HH:MM
> 데이터 소스: GitHub (자동) + 노션 PDF (강사 수동)

## 신호등 요약

| 팀 | 상태 | Status Issue | PDF | 핵심 신호 |
|---|---|---|---|---|
| 1팀 | 🟢 | ✅ | ✅ | - |
| 2팀 | 🟡 | ✅ | ❌ Decision Log | Won't 0개 |
| 3팀 | 🔴 | ❌ 미작성 | ✅ | Status Issue 미작성, PR 0 머지 |

## 🔴 즉시 개입 필요

(없으면 "없음" 명시)

- **3팀**: Weekly Status Issue 미작성 — 자기 점검 절차 멈춤 (가이드 § 4-4)

## 🟡 다음 1:1에서 짚을 것

- **2팀**: 자기보고 Won't 0개 — 범위 폭발 위험 (가이드 § 4-4)
- **2팀**: Decision Log PDF 누락 — 강사 인쇄 후 재실행

## 팀별 상세

### 1팀
**자기보고 (Status Issue #N)**
- Brief 작성: ✅ / Demo Scenario: ✅
- Must / Should / Won't: 4 / 2 / 3
- Flow 확정: 2개
- 이번 주 결정 수: 1
- 한 줄: "로그인 성공 경로 데모 가능"

**GitHub 메타**
- Issue: 5 / 평균 AC: 4.2개
- PR: 2 머지 / Closes # 연결률 100%
- 24h SLA: 100% (2/2)

**PDF**
- 01_brief.pdf ✅ / 02_flow_login.pdf ✅ / 06_decision_log.pdf ✅ / 07_retro.pdf ✅

### 2팀
...
```

## 6. 신호등 임계값 (analyze.py 와 일치)

### 자기보고(Status Issue) 기반

| 지표 | 🟢 | 🟡 | 🔴 |
|---|---|---|---|
| Status Issue 작성 (해당 주차) | ✅ | - | ❌ |
| Brief 작성 자기보고 | ✅ | - | ❌ (Week 1 말 기준) |
| Won't 항목 수 | >= 3 | 1~2 | 0 |
| Must 항목 수 | 3~5 | 6~7 | >= 8 |
| Flow 확정 수 | 1~2 | 3 | >= 4 |
| Demo Scenario 자기보고 | ✅ | - | ❌ |

### GitHub 기반

| 지표 | 🟢 | 🟡 | 🔴 |
|---|---|---|---|
| Issue 평균 AC 수 | 3~7 | 1~2 또는 8+ | 0 |
| Closes # 연결률 | 100% | 80~99% | < 80% |
| 24h 1차 리뷰 SLA | >= 80% | 50~79% | < 50% |
| Week >= 2 PR 머지 수 | >= 1 | - | 0 |
| WIP 위반(assignee 동시 3+) | 0 | 1팀 | 2팀+ |

### PDF (강사 수동)

| 지표 | 🟢 | 🟡 |
|---|---|---|
| 모든 표준 PDF 존재 | ✅ | ❌ (강사 인쇄 누락 → 사용자에게 알림) |

## 7. Claude가 절대 하지 말 것

- ❌ 학생 직접 "혼내는" 어조 — 리포트는 강사가 읽고 판단할 자료
- ❌ JSON에 없는데 추측해서 채우기 — "데이터 없음" 명시
- ❌ Notion 자동화 도구 호출 / 회사 노션 우회 시도 코드 작성
- ❌ teams.yaml 비어 있는데 임의 팀명 만들기 — 사용자에게 등록 요청
- ❌ 가이드 § 번호 없는 진단 — 모든 신호는 [`../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md`](../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md) § 근거 명시

## 8. 자주 쓸 인접 문서

- 규칙 본문 (가이드): [`../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md`](../final-project/docs/애자일/01_애자일_팀프로젝트_가이드.md)
- 안티패턴 식별표: [`../ta-guides/애자일_예제기반_FAQ.md`](../ta-guides/애자일_예제기반_FAQ.md) § 9
- 학생 OT 공지: [`docs/student_ot_notice.md`](docs/student_ot_notice.md)
- 학생 Status Issue 안내: [`docs/weekly_status_setup.md`](docs/weekly_status_setup.md)
- 강사 노션 PDF 절차: [`docs/notion_pdf_workflow.md`](docs/notion_pdf_workflow.md)
