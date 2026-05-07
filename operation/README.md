# operation/ — 보조강사 운영 도구

부트캠프 6기 파이널 프로젝트 진행 중 각 팀의 **GitHub 산출물 + 노션 PDF**를 주 1회 수집/분석하여 마크다운 리포트 생성. cron 없음. **Claude 세션에서 호출**해서 실행.

## 데이터 소스 분담 (회사 노션 차단 정책 대응)

| 소스 | 수집 방식 | 자동화 | 강사 시간 |
|---|---|---|---|
| **GitHub Issue/PR 메타데이터** | `fetch_github.py` API 자동 수집 | ✅ | 0분 |
| **GitHub Weekly Status Issue** | 학생 PO가 매주 작성 → API 자동 파싱 | ✅ (학생 5분) | 0분 |
| **노션 Brief / Flow / Decision Log / Retro** | 강사가 PDF 인쇄 → 폴더에 저장 | ❌ 수동 | 주 15~25분 |

자세한 배경: 회사 노션이 Notion Integration + Export 모두 차단되어 있어 자동 API 수집 불가. 우회(Playwright/스크래핑) 시도는 회사 정책 위반 위험. 따라서 **GitHub은 풀 자동, 노션은 PDF 수동** 하이브리드.

## 빠른 시작 (3 step)

1. `cp .env.example .env` 후 `GITHUB_TOKEN` 채우기
2. `pip install python-dotenv pyyaml PyGithub`
3. Claude에게: *"Week 1 스냅샷 떠줘"*

## 디렉터리 구조

| 경로 | 용도 | git |
|---|---|---|
| `.env` | 시크릿 토큰 | ❌ |
| `teams.yaml` | 팀 명단 / GitHub 레포 | ✅ |
| `templates/weekly-status.md` | 학생용 Weekly Status Issue Template | ✅ |
| `scripts/fetch_github.py` | GitHub 메타데이터 + Status Issue 파싱 | ✅ |
| `scripts/analyze.py` | JSON → 지표 + 신호등 | ✅ |
| `snapshots/week-N/팀명_github.json` | GitHub 메타 raw | ✅ |
| `snapshots/week-N/팀명_status.json` | Status Issue 파싱 결과 | ✅ |
| `snapshots/week-N/팀명_pdf/*.pdf` | **강사가 인쇄한 노션 PDF** | ✅ (또는 LFS) |
| `snapshots/week-N/_summary.json` | 주차 지표 요약 | ✅ |
| `reports/week-N.md` | 주차별 마크다운 리포트 | ✅ |
| `docs/student_ot_notice.md` | 1주차 OT 학생 공지문 | ✅ |
| `docs/weekly_status_setup.md` | 학생/PO용 Status Issue 작성 안내 | ✅ |
| `docs/notion_pdf_workflow.md` | 강사용 노션 PDF 추출 절차 | ✅ |
| `CLAUDE.md` | Claude 운영 지침 | ✅ |

## 다음 보기

- **Claude 호출 패턴**: [`CLAUDE.md`](CLAUDE.md)
- **1주차 OT 공지문**: [`docs/student_ot_notice.md`](docs/student_ot_notice.md)
- **학생 PO용 Status Issue 안내**: [`docs/weekly_status_setup.md`](docs/weekly_status_setup.md)
- **강사 본인용 노션 PDF 추출 절차**: [`docs/notion_pdf_workflow.md`](docs/notion_pdf_workflow.md)
- **수집 지표 / 안티패턴**: [`../ta-guides/애자일_예제기반_FAQ.md`](../ta-guides/애자일_예제기반_FAQ.md) § 9

## 주차별 작업

| 주 | 강사 작업 | 학생 작업 |
|---|---|---|
| Week 0 (사전) | Integration 시도 → 차단 확인 → 본 도구 운영 시작 | - |
| Week 1 OT | 학생 공지 + Status Issue Template 배포 + 첫 PDF 추출 | Status Issue Template 자기 레포 적용 |
| Week 1~6 매주 일요일 | 노션 PDF 추출 (15~25분) + Claude에게 스냅샷 호출 | Weekly Status Issue 작성 (5분) |
| Week 6 종료 후 | `snapshots/` 폐기 또는 익명화, Status Issue 라벨 정리 | - |
