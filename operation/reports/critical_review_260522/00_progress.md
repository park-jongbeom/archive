# 크리티컬 이슈 점검 진행 기록 (2026-05-22)

## 목적
강사진 테스트 지원 단계에서 각 팀(1팀/2팀/3팀) 소스코드의 크리티컬 이슈 탐지

## 진행 단계 (사용자 지정 순서)

1. **Step 1** — 프로젝트 도메인 및 기능 정리 (3팀 모두)
2. **Step 2** — 유사 오픈소스 및 기능 구성 참조 검색
3. **Step 3** — 검색 정보 정리 + 팀별 체크 사항 정합 확인
4. **Step 4** — 추가 필요 정보 검색 (gap 발견 시)
5. **Step 5** — 팀별 git 소스코드 크리티컬 체크 실행
6. **Step 6** — 최종 리포트 작성

## 산출물 위치
`operation/reports/critical_review_260522/`

- `00_progress.md` — 본 문서 (작업 진행 기록)
- `01_domains.md` — Step 1: 팀별 도메인/기능 정리
- `02_references.md` — Step 2-3: 유사 오픈소스 참조 + 정합
- `03_checklist.md` — Step 3-4: 팀별 체크 항목 확정
- `04_team1_findings.md` — 1팀 점검 결과
- `04_team2_findings.md` — 2팀 점검 결과
- `04_team3_findings.md` — 3팀 점검 결과
- `05_final_report.md` — 최종 통합 리포트

## 팀 식별 (메모리 기반)

- **1팀 Scoffee** — Android + Wear OS 페어 앱 (커피 관련, 손지희 팀장)
- **2팀 Umma** — Android 앱 (Mahnic 방법론, 박재민 팀장)
- **3팀** — Android + Firebase Functions (워키토키 + 친구 위치 + DM, 정우석 R-quiet 해소 sentinel)

## 현재 진행 상태

- [x] 작업 디렉터리 구조 파악
- [x] Step 1: 도메인/기능 정리
- [x] Step 2-3: 참조 검색 및 정합
- [x] Step 4: gap 없음 확인 (추가 검색 불요)
- [x] Step 5: 팀별 점검 (병렬 Agent × 3)
- [x] Step 6: 최종 리포트 ([05_final_report.md](05_final_report.md))

## 발견 합계

- 1팀: 🔴6 / 🟠3 / 🟡3
- 2팀: 🔴5 / 🟠6 / 🟡9
- 3팀: 🔴6 / 🟠3 / 🟡9
- **합계: 🔴17 / 🟠12 / 🟡21 (총 50건)**

## 핵심 코호트 신호

1. **Firestore Rules 부재 — 코호트 100%**
2. 1팀 README↔실구현 간극 (AI/Watch/Report 미구현 다수)
3. 2팀 보안 자세 미흡 (평문 API key, App Check 0, fake repo main set)
4. 3팀 서버 functions 빈 파일 — 콘솔 deploy 확인 시급
