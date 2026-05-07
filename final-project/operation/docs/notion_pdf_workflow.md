# 노션 PDF 추출 절차 (보조강사 본인용)

> 회사 노션이 Notion API + Export 모두 차단되어 있어 자동 수집 불가.
> 강사가 매주 일요일 직접 PDF로 인쇄해서 강사 로컬 저장소에 보관.
> **학생 추가 작업 없음** (게스트 편집 권한이라도 강사 본인이 페이지 열어 인쇄 가능).

---

## 0. 사전 점검 — 어떤 방법이 회사 노션에서 작동하는가

회사가 차단할 수 있는 기능 순서대로 시도해 작동 가능한 방법 1개 확정:

| 시도 | 방법 | 비고 |
|---|---|---|
| 1순위 | 페이지 ⋯ → **Print** → "PDF로 저장" | 가장 깔끔. 보통 차단 안 됨 |
| 2순위 | 브라우저 **Ctrl+P** → "PDF로 저장" | 1순위와 동등 |
| 3순위 | Windows **Snipping Tool** 으로 페이지 스크롤하며 캡처 → 이미지 PDF로 합치기 | 인쇄 차단 시 |
| 4순위 | **수동 복붙** → 로컬 .md 파일 | 위 모두 차단 시 |

**사전 검증** (1주차 OT 전 5분):
1. 임의의 1팀 Brief 페이지 열기
2. Ctrl+P 시도 → 인쇄 다이얼로그 뜨면 1순위 OK
3. 안 뜨면 2순위/3순위/4순위 순으로 검증

---

## 1. 매주 추출 절차 (15~25분)

### 폴더 규칙

각 팀별로 `snapshots/week-N/팀명_pdf/` 폴더에 저장.

```
operation/snapshots/week-N/
├── 1팀_pdf/
│   ├── 01_brief.pdf
│   ├── 02_flow_login.pdf       ← Flow가 여러 개면 02_flow_<flow이름>.pdf
│   ├── 02_flow_record.pdf
│   ├── 05_demo_scenario.pdf
│   ├── 06_decision_log.pdf
│   └── 07_retro.pdf
├── 2팀_pdf/
└── ...
```

### 표준 파일명 (Claude가 자동 인식)

| 파일명 | 매칭 노션 페이지 |
|---|---|
| `01_brief.pdf` | Product Brief |
| `02_flow_*.pdf` | Flow 페이지 (각 Flow별 1개) |
| `05_demo_*.pdf` | Demo Scenario 페이지 |
| `06_decision_log.pdf` | Decision Log |
| `07_retro.pdf` | Retrospective |

> 번호 규칙은 [`../../docs/애자일/example/`](../../docs/애자일/example/) 폴더 구조와 일치.

### 추출 단계

각 팀 × 각 페이지 1분 작업:

1. 회사 노션 게스트 계정으로 로그인
2. 팀의 해당 페이지 열기 (예: 1팀 Brief)
3. 우상단 ⋯ → **Print** (또는 Ctrl+P)
4. 대상: **PDF로 저장**
5. 저장 위치: `operation/snapshots/week-N/팀명_pdf/`
6. 파일명: 표준 규칙대로 (예: `01_brief.pdf`)
7. 다음 페이지로 반복

### 5팀 × 5종 = 25페이지 인쇄, 약 20~25분 소요

> 시간 절약 팁: Brief / Flow는 **Week 1에 1번 인쇄 후 변경 시에만 재인쇄**. Decision Log / Retro는 매주 누적.

---

## 2. 주차별 인쇄 범위

| 주 | 필수 인쇄 | 선택 인쇄 |
|---|---|---|
| Week 1 | 01_brief, 02_flow_*, 06_decision_log, 07_retro | 05_demo (작성됐으면) |
| Week 2~5 | 06_decision_log, 07_retro | Brief/Flow 변경 시 재인쇄, 05_demo 매주 |
| Week 6 | 전체 (제출용 아카이브) | - |

평균 매주 **15분** 안팎으로 줄어듭니다.

---

## 3. PDF 누락 시 Claude 동작

`analyze.py` 는 PDF 폴더 존재 + 표준 파일명 매칭만 점검합니다 (PDF 내용 분석 X).

| 상황 | Claude 신호 |
|---|---|
| `팀명_pdf/` 폴더 없음 | 🟡 PDF 누락 — 강사 인쇄 필요 |
| `01_brief.pdf` 없음 | 🟡 Brief PDF 미수신 |
| 표준 파일명 미일치 (예: `brief.pdf`) | 🟡 파일명 규칙 위반 |
| 모든 표준 PDF 존재 | 🟢 |

PDF **내용 분석**은 패턴 B ("K팀 깊게 봐줘") 호출 시 Claude의 Read 도구가 직접 PDF를 열어 처리. 본문 텍스트 추출 + 가이드 § 9 안티패턴 매칭.

---

## 4. 보안 / 폐기

- PDF는 강사 로컬 git 레포에만 보관. **외부 공유 금지**
- PDF에 학생 개인 식별 정보(이메일/전화/실명) 포함 시 마스킹 후 저장 또는 삭제
- 6주 종료 시 `snapshots/` 폴더 전체 삭제
- 회고용으로 보관할 경우 PDF 텍스트 추출 → 익명화 후 텍스트 파일만 유지

---

## 5. 문제 해결

### Q. Print도 차단되어 있다
1. 브라우저 Ctrl+P 직접 시도
2. 그래도 안 되면 Snipping Tool로 페이지 스크롤 캡처 → PDF 합성 (예: smallpdf, ilovepdf 같은 사이트는 사용 금지 — 회사 데이터 외부 업로드 금지. 로컬 도구로만 합성)
3. 모두 막히면 수동 복붙 → `01_brief.md` 등 마크다운 파일로 저장 (PDF 대체)

### Q. PDF 파일이 너무 커서 git에 못 올림
- 대안 1: git LFS 사용
- 대안 2: PDF 대신 텍스트만 추출(예: pdftotext)해서 `.txt`로 저장 후 PDF는 로컬에만 보관
- 대안 3: `snapshots/` 를 git ignore 후 별도 외부 백업

### Q. Flow DB 안의 row(개별 Flow 페이지) 일일이 다 인쇄해야 하나
- Flow가 1~2개면 각각 인쇄 (`02_flow_login.pdf`, `02_flow_record.pdf`)
- Flow가 3개 이상이면 가이드 § 5-2-1 위반 신호 — Status Issue 자기보고에서 이미 잡힘. PDF는 핵심 1~2개만 인쇄해도 됨.

### Q. 노션 페이지가 길어서 PDF가 50페이지가 넘는다
- Brief가 50페이지면 워터폴 신호 자체가 🔴 (가이드 § 4-4 *"1~2페이지"*)
- 그 사실 자체가 1:1에서 짚을 핵심 신호 — 일단 그대로 인쇄해서 증거 보관
