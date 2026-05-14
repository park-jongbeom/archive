# 2팀 (Umma) GitHub Project Backlog PDF 보관소

> **목적**: GitHub Project v2 Kanban 보드(`projects/12/views/1`)를 브라우저 인쇄 PDF로 추출하여, SKILL이 v2 양식 §🗂️ 데이터 source로 활용.

## 1. 출처
- **Project URL**: https://github.com/orgs/LIKELION-Android-BOOTCAMP-6th/projects/12/views/1

## 2. 명명 규약
- AM 회의용: `backlog_<YYMMDD>_am.pdf` (회의 40분 전 = 09:20 추출)
- PM 회의용: `backlog_<YYMMDD>_pm.pdf` (회의 40분 전 = 15:20 추출)
- 예: `backlog_260514_am.pdf`, `backlog_260514_pm.pdf`

## 3. 추출 절차 (브라우저, ~40초)
1. Project URL 열기
2. Ctrl+P → PDF로 저장 → 본 폴더에 명명 규약대로 저장

## 4. SKILL 자동 읽기
- `/team-check-am 2` 또는 `/team-check-pm 2` 호출 시 SKILL이 본 폴더 최신 PDF 자동 파싱
- v2 양식 §🗂️ 매칭표에 자동 채움
- **2팀 주요 점검 항목**: 박재민 assign 카드 수 (R-WIP 기준 ≥ 4) + 적응 멤버(김명준/정재훈) assign 0 여부 (R-out-2)

## 5. 보관 정책
- 영구 보관, 1주차 종료 시 검토

## 6. 트러블슈팅
- 1팀 backlog README의 §6 트러블슈팅 동일
