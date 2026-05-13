# 3팀 일일 스냅샷 (BBip)

> 매일 일일 진척 체크 ([daily_check_method.md](../../.shared/daily_check_method.md)) 수행 후 결과를 이곳에 저장합니다.
> **3팀은 워터폴** 방법론이므로 1팀/2팀과 점검 톤이 다릅니다 — [team_specific_checks.md](../review/team_specific_checks.md) 참조.

## 파일명 규칙

`YYMMDD_(am|pm).md` — 예:
- `260514_am.md` = 2026년 5월 14일 오전 10시 미팅용
- `260514_pm.md` = 2026년 5월 14일 오후 4시 미팅용

## 형식

[`../../.shared/meeting_prep_template.md`](../../.shared/meeting_prep_template.md) 의 AM 또는 PM 양식 그대로 사용하되, 3팀 변형 적용:

- "Sprint" 항목 제거 → "현재 단계" 로 대체
- "Mock-first" 점검 제거
- "Phase-gate Must Meet 체크리스트" 추가 ([team_specific_checks.md § 2](../review/team_specific_checks.md#2-phase-gate-체크리스트-워터폴-핵심-))
- 인물별 활동표는 commit + Issue + Figma + 댓글 모두 동등 가중치

## 활용

다음 체크 시 직전 스냅샷을 읽어 변화량 산출:

- AM 점검: 어제 PM 스냅샷과 비교
- PM 점검: 오늘 AM 스냅샷과 비교
- **단계 전환 시점에 누적 진척도 비교**가 더 중요 (워터폴 특화)

## 워터폴 단계 전환 시 별도 보고서

각 단계 종료 시 [`./phase_handoff_<단계명>.md`](./) 형식으로 핸드오프 문서 별도 작성:
- 설계 → 구현: `phase_handoff_design.md`
- 구현 → 검증: `phase_handoff_implementation.md`
- 검증 → 데모: `phase_handoff_verification.md`

## 보관 정책

- 모든 스냅샷 영구 보관 (6주 프로젝트 종료 후 회고용)
- 단계 종료 시 phase_handoff 문서 별도 생성
- 주 1회 Pre-mortem 결과는 [`YYMMDD_premortem.md`](./) 형식으로 별도 저장

## 베이스라인

⬜ 정식 일일 점검 시작 시점에 베이스라인 스냅샷 작성. 현재는 [../review/context.md](../review/context.md) 가 베이스라인 역할.
