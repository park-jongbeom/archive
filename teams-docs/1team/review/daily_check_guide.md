# 1팀 일일 점검 가이드 — 인덱스

> **구조 개편 (2026-05-13)**: 본 문서의 내용 대부분은 [`teams-docs/.shared/`](../../.shared/) 공통 메서드 + [`./context.md`](./context.md) / [`./team_specific_checks.md`](./team_specific_checks.md) 팀별 컨텍스트로 분리됐습니다. 본 문서는 **빠른 진입을 위한 인덱스** 역할입니다.

---

## 1. 호출 시 진입 순서 (Claude 자동)

사용자가 "1팀 작업 현황 체크해줘" 발화 시:

1. [`../../.shared/daily_check_method.md`](../../.shared/daily_check_method.md) — 점검 방법론
2. [`./context.md`](./context.md) — 1팀 컨텍스트 환기
3. [`./team_specific_checks.md`](./team_specific_checks.md) — 1팀 고유 점검 항목
4. [`../../.shared/risk_taxonomy.md`](../../.shared/risk_taxonomy.md) — 위험 신호 정량 기준
5. [`../snapshots/`](../snapshots/) — 직전 스냅샷 (변화 감지용)
6. [`./issue_pr_matrix.md`](./issue_pr_matrix.md) — Issue↔PR↔Figma 추적 매트릭스
7. [`../figma/README.md`](../figma/README.md) — Figma 보드 구조
8. [`../../.shared/meeting_prep_template.md`](../../.shared/meeting_prep_template.md) — AM/PM 양식 작성
9. 결과를 [`../snapshots/YYMMDD_(am|pm).md`](../snapshots/) 로 저장

---

## 2. 1팀 핵심 정보 (한눈에)

| 항목 | 값 |
|---|---|
| 앱명 | **Scoffee** (카페인-수면-AI) |
| 방법론 | 애자일 |
| 인원 | **3명** (손지희/이제이/임정섭) — 송성호 2026-05-12 중도포기 |
| GitHub | https://github.com/LIKELION-Android-BOOTCAMP-6th/Snoffee |
| Figma | FigJam `NKBVG1F8BcFXzjpnJxsC4u` |
| 현재 Week | Week 2 종료 → Week 3 진입 (2026-05-13) |

상세: [`./context.md`](./context.md)

---

## 3. 1팀 최우선 점검 5건 (2026-05-12 인원 변경 반영)

[`./team_specific_checks.md § 1`](./team_specific_checks.md#1-1팀-절대-빠뜨리지-말-것-top-5) 매 회의 전 무조건 점검:

1. 손지희 신임 팀장 부담 (R-burn-1)
2. PO 공백 신호 (R-PO)
3. Issue Closes 컨벤션 (R2)
4. Sprint 1 FigJam 상태 (R11)
5. Mock-first 합의 준수 + Must-4·5 진척 (R4 + 3중 빈틈)

---

## 4. 1팀 위험 신호 (Top 발견 — 5/12 갱신)

[`./team_specific_checks.md § 6`](./team_specific_checks.md#6-1팀-특화-r-항목-공통-r1r14-외-추가) :

- **R-PO** ⭐: PO 공백 (5/12부 신규) — 우선순위 결정 누락 위험
- **R-burn-1** ⭐: 손지희 부담 1.3배 초과 (5/12부 신규)
- **R-MN**: Must-N 3중 빈틈 (Must-4 Gemini, Must-5 WearOS)
- **R-DL**: Decision Log 미작성 — 5/12 인원 변경 기록부터 시작
- **R-Must7**: Must 7개가 3명 체제에서 과부하 가능 → Week 3 중반 재평가

공통 R1~R14: [`../../.shared/risk_taxonomy.md`](../../.shared/risk_taxonomy.md)

---

## 5. 1팀 외 자료

- [`./brief_review_w1.md`](./brief_review_w1.md) — Week 1 검토 (강팀 평가, 종합 🟢)
- [`./issue_pr_matrix.md`](./issue_pr_matrix.md) — 13 PR / 6 Issue / 0 닫힘 매트릭스
- [`../figma/README.md`](../figma/README.md) — Sprint 0 (13섹션) / Sprint 1 (빈) 구조
- [`../snapshots/260513.md`](../snapshots/260513.md) — Week 2 베이스라인

---

## 6. 변경 이력

| 날짜 | 변경 |
|---|---|
| 2026-05-13 (초안) | 일일 체크 가이드 1차 작성 (14KB) — 모든 내용 본 문서에 |
| 2026-05-13 (개편) | **3-tier 구조로 개편** — 공통 메서드는 `.shared/` 로, 팀 컨텍스트는 `context.md` / `team_specific_checks.md` 로 분리. 본 문서는 인덱스 역할로 축소 |
| 2026-05-13 (인원 변경 반영) | **5/12부 송성호 PO 중도포기 / 손지희 신임 팀장 / 부팀장 공백 / 3명 체제** — Top 5·R-항목·인원 표 일괄 갱신 |

---

## 7. 참조

### 공통 (1/2/3팀 적용)
- [`.shared/daily_check_method.md`](../../.shared/daily_check_method.md)
- [`.shared/risk_taxonomy.md`](../../.shared/risk_taxonomy.md)
- [`.shared/meeting_prep_template.md`](../../.shared/meeting_prep_template.md)
- [`.shared/premortem_template.md`](../../.shared/premortem_template.md)
- [`.shared/README.md`](../../.shared/README.md) — Figma 토큰·레지스트리

### 1팀 전용
- [`./context.md`](./context.md) — 컨텍스트
- [`./team_specific_checks.md`](./team_specific_checks.md) — 고유 점검 항목
- [`./brief_review_w1.md`](./brief_review_w1.md) — Week 1 검토
- [`./issue_pr_matrix.md`](./issue_pr_matrix.md) — Issue↔PR↔Figma 매트릭스

### 다른 팀 (구조 비교용)
- [`../../2team/review/context.md`](../../2team/review/context.md) — 2팀 컨텍스트
- [`../../3team/review/context.md`](../../3team/review/context.md) — 3팀 컨텍스트 (워터폴)
