# 3팀 (BBip) GitHub Project Backlog PDF 보관소

> **⚠️ 워터폴 변형**: 1팀·2팀(애자일)과 달리 3팀은 워터폴이므로 Kanban 보드 해석도 워터폴 톤 유지.
> **목적**: GitHub Project v2 보드(`projects/13/views/1`) PDF 추출 → SKILL이 v2 양식 §🗂️ 데이터 source로 활용.

## 1. 출처
- **Project URL**: https://github.com/orgs/LIKELION-Android-BOOTCAMP-6th/projects/13/views/1

## 2. 명명 규약
- AM 회의용: `backlog_<YYMMDD>_am.pdf` (회의 40분 전 = 09:20 추출)
- PM 회의용: `backlog_<YYMMDD>_pm.pdf` (회의 40분 전 = 15:20 추출)

## 3. 추출 절차 (브라우저, ~40초)
1. Project URL 열기
2. Ctrl+P → PDF로 저장 → 본 폴더에 명명 규약대로 저장

## 4. SKILL 자동 읽기 — 3팀 워터폴 변형
- `/team-check-am 3` 또는 `/team-check-pm 3` 호출 시 SKILL이 본 폴더 최신 PDF 자동 파싱
- **3팀 주요 점검 항목**:
  - In Progress 카드와 [`team_specific_checks.md §2 Phase-gate`](../review/team_specific_checks.md) 단계 정합 — 현재 단계 산출물 진행 중인지
  - **R-W3 (명세-구현 비동기)**: 보드 카드와 PRD/명세서 매칭 (보드에 있는데 명세에 없음, 그 반대)
  - **R-W1 (Must Meet 미충족 단계 종료)**: 단계 종료 임박인데 Must Meet 6항목 안 채워졌으면 보드 카드 진척과 무관하게 🚨

## 5. 톤 (절대 준수)
- ❌ MVP 다이어트 / Sprint 단위 권고 / 회고 후 백로그 재조정
- ✅ 명세 단계에서 범위 잠금 → 구현 단계는 명세 준수
- 메모리 [team3_methodology.md](../../../../.claude/projects/c--Users-ibebu-bootcamp6-final-archive/memory/team3_methodology.md) 참조

## 6. 트러블슈팅
- 1팀 backlog README §6 트러블슈팅 동일
