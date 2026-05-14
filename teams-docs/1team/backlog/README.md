# 1팀 (Scoffee) GitHub Project Backlog PDF 보관소

> **목적**: GitHub Project v2 Kanban 보드(`projects/11/views/1`)를 브라우저 인쇄 PDF로 추출하여, SKILL이 v2 양식 §🗂️ 데이터 source로 활용.

## 1. 출처
- **Project URL**: https://github.com/orgs/LIKELION-Android-BOOTCAMP-6th/projects/11/views/1
- **View**: Backlog (Kanban)
- **컬럼**: Backlog (0/5) / Ready (0) / In Progress (7/3) / In Review (3/5) / Done

## 2. 명명 규약
- AM 회의용: `backlog_<YYMMDD>_am.pdf` (회의 40분 전 = 09:20 추출)
- PM 회의용: `backlog_<YYMMDD>_pm.pdf` (회의 40분 전 = 15:20 추출)
- 예: `backlog_260514_am.pdf`, `backlog_260514_pm.pdf`

## 3. 추출 절차 (브라우저, ~40초)
1. Project URL 열기 (Snoffee Kanban view)
2. Ctrl+P (또는 Cmd+P) → 인쇄
3. 대상: **PDF로 저장**
4. 옵션: 배경 그래픽 포함, 한 페이지에 맞춤 (필요시)
5. 파일명·경로: 위 규약대로 본 폴더에 저장

## 4. SKILL 자동 읽기
- `/team-check-am 1` 또는 `/team-check-pm 1` 호출 시 SKILL이 자동으로 본 폴더의 **최신 PDF**를 Read 도구로 파싱
- 추출 정보: 컬럼별 카드 수 + 각 카드 (#번호, 제목, assignee) + WIP 한도 위반 점검
- v2 양식 §🗂️ 매칭표에 자동 채움

## 5. 보관 정책
- AM/PM PDF는 회의 회고용으로 영구 보관
- 1주차 종료 시 (매주 일요일) 압축 검토 (PDF 크기 작아 영구 보관 무리 없음)
- Week N 진척 분석 시 historical baseline으로 활용

## 6. 트러블슈팅
- PDF가 10페이지 초과 → Read 도구 `pages` 파라미터로 분할 읽기
- 카드 텍스트가 잘려서 추출 안 됨 → 인쇄 옵션 "한 페이지에 맞춤" 해제 또는 가로 방향
- assignee가 아바타로만 표시 → 사용자가 1줄 매핑 보강 (예: "#26 = 정섭")
